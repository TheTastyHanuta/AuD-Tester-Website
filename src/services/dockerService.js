const { randomUUID } = require('crypto');
const path = require('path');
const fs = require('fs-extra');
const config = require('../config/config');
const { createScopedLogger } = require('../utils/loggerHelper');
const { spawnPromise } = require('./processService');

function getDockerResourceArgs() {
  return [
    `--memory=${config.DOCKER_MEMORY_LIMIT}`,
    `--cpus=${config.DOCKER_CPU_LIMIT}`,
    `--pids-limit=${config.DOCKER_PIDS_LIMIT}`,
    '--network=none',
  ];
}

function getDockerResourceLogContext() {
  return {
    memory: config.DOCKER_MEMORY_LIMIT,
    cpus: config.DOCKER_CPU_LIMIT,
    pidsLimit: config.DOCKER_PIDS_LIMIT,
    network: 'none',
  };
}

function createDockerContainerName(prefix = 'aud-tester') {
  return `${prefix}-${randomUUID()}`;
}

async function removeDockerContainer(containerName, log) {
  const cleanupResult = await spawnPromise(
    'docker',
    ['rm', '-f', containerName],
    { timeout: 10000 }
  );

  if (cleanupResult.error) {
    log?.warn('Could not remove timed out Docker container', {
      containerName,
      error: cleanupResult.stderr || cleanupResult.error.message,
    });
  } else {
    log?.info('Removed timed out Docker container', { containerName });
  }
}

async function runDockerCommand(args, options = {}) {
  const {
    containerName,
    log,
    timeout = config.DOCKER_TEST_TIMEOUT_MS,
    ...spawnOptions
  } = options;
  const result = await spawnPromise('docker', args, {
    ...spawnOptions,
    timeout,
  });

  if (result.timedOut && containerName) {
    log?.warn('Docker command timed out', {
      containerName,
      timeoutMs: timeout,
    });
    await removeDockerContainer(containerName, log);
  }

  return result;
}

async function runDockerTests(workingDir, exercise, exerciseConfig, sessionId) {
  const log = createScopedLogger({ sessionId, exercise, workingDir });

  try {
    log.info('Starting Docker tests');

    const dockerImage = config.DOCKER_TEST_IMAGE_MAPPING[exercise];
    if (!dockerImage) {
      log.warn('No Docker image configured for exercise', {
        configuredExercises: Object.keys(config.DOCKER_TEST_IMAGE_MAPPING),
      });
      return {
        success: false,
        status: '⚠️',
        message: 'Test configuration error',
        details:
          'Es wurde kein Test für diese Übung konfiguriert. Das sollte nicht passieren. Wenn das Problem weiterhin besteht, melde Dich bitte im Forum.',
      };
    }

    // Create result directory for Docker output
    const resultDir = path.join(workingDir, 'result');
    await fs.ensureDir(resultDir);

    const containerName = createDockerContainerName('aud-tester-tests');
    log.info('Resolved Docker test sandbox', {
      dockerImage,
      containerName,
      timeoutMs: config.DOCKER_TEST_TIMEOUT_MS,
      resources: getDockerResourceLogContext(),
    });

    const dockerArgs = [
      'run',
      '--rm',
      '--name',
      containerName,
      ...getDockerResourceArgs(),
      '-v',
      `${path.resolve(workingDir)}:/user`,
      '-v',
      `${path.resolve(resultDir)}:/result`,
      dockerImage,
    ];

    log.debug('Executing Docker command', {
      command: 'docker',
      args: dockerArgs,
      dockerImage,
      resultDir,
    });

    // Execute Docker command with timeout
    const dockerResult = await runDockerCommand(dockerArgs, {
      cwd: workingDir,
      timeout: config.DOCKER_TEST_TIMEOUT_MS,
      containerName,
      log,
    });

    // If the Docker process failed to return a result, handle it
    if (!dockerResult) {
      log.error('Docker execution failed to return a result', {
        dockerImage,
        resultDir,
      });
      return {
        success: false,
        status: '⚠️',
        message: 'Überprüfung fehlgeschlagen',
        details:
          'Es ist ein Fehler bei der Ausführung aufgetreten. Bitte versuche es später erneut und überprüfe Deinen Code. Wenn das Problem weiterhin besteht, melde Dich bitte im Forum.',
      };
    }

    log.debug('Docker execution completed', {
      dockerImage,
      returnCode: dockerResult.error?.code || 0,
      timedOut: dockerResult.timedOut,
      stdout: dockerResult.stdout,
      stderr: dockerResult.stderr,
    });

    // Check if Docker execution failed
    if (dockerResult.error && dockerResult.error.code !== 0) {
      log.error('Docker execution failed', {
        dockerImage,
        error: dockerResult.error.message,
        stdout: dockerResult.stdout,
        stderr: dockerResult.stderr,
      });

      // If Docker crashes, return compile error
      return {
        success: false,
        status: '⚠️',
        message: 'Überprüfung fehlgeschlagen',
        details:
          'Es ist ein Fehler bei der Ausführung aufgetreten. Bitte versuche es später erneut und überprüfe Deinen Code. Wenn das Problem weiterhin besteht, melde Dich bitte im Forum.',
      };
    }

    // Try to read results.json from result directory
    const resultsJsonPath = path.join(resultDir, 'results.json');

    if (!(await fs.pathExists(resultsJsonPath))) {
      log.warn('Results file not found after Docker execution', {
        dockerImage,
        resultsPath: resultsJsonPath,
      });
      return {
        success: false,
        status: '⚠️',
        message:
          'Es gab ein Problem bei der Überprüfung. Details konnten nicht gelesen werden.',
        details:
          'Tests wurden ausgeführt, aber keine Ergebnisse gefunden. Bitte überprüfe Deinen Code. Wenn das Problem weiterhin besteht, melde Dich bitte im Forum.',
      };
    }

    // Parse results.json
    const resultsData = await fs.readJson(resultsJsonPath);

    log.debug('Docker test results parsed', {
      dockerImage,
      instant_status: resultsData.instant_status,
      protected_status: resultsData.protected_status,
      points: resultsData.points,
      successful_run: resultsData.success,
      feedback: resultsData.protected_feedback_text,
      instantMessage: resultsData.instant_message,
    });

    // Format feedback
    const points = resultsData.points || '0';
    let instantStatus = resultsData.instant_status || '❌';
    let feedbackText = resultsData.protected_feedback_text || '';

    // Handle compile errors specifically
    if (feedbackText.includes('Compile error')) {
      const instantMessage = resultsData.instant_message || '';
      feedbackText = `Compile error\n\nReason from auto-feedback:\n\n${instantMessage}`;
    }

    // Clean feedback text
    const cleanFeedbackText = feedbackText.replace(/,/g, '');

    // Determine success based on status
    let isSuccess = instantStatus === '✔' || instantStatus.includes('✔');

    if (instantStatus === '✔') {
      instantStatus = '✅';
      isSuccess = true;
    } else if (instantStatus === '⚠️') {
      isSuccess = false;
      log.warn('Docker test returned an internal error', {
        dockerImage,
        instantStatus,
      });
    } else {
      instantStatus = '❌';
      isSuccess = false;
    }

    // Build message based on status
    let message;
    if (instantStatus === '✅') {
      message =
        'Alles supi. Du kannst die Dateien so auf StudOn hochladen. Genaueres Feedback wird angezeigt, wenn die Deadline vorbei ist.';
    } else if (instantStatus === '⚠️') {
      message =
        'Internal Error. Das ist gar nicht gut. Wenn das öfter passiert, melde Dich bitte im Forum.';
    } else {
      message =
        'Compile Error. Bitte überprüfe Deinen Code. Bei einer Abgabe über StudOn wird dies 0 Punkte ergeben. (Ausnahme sind die ersten zwei Übungen)';
    }

    return {
      success: isSuccess,
      status: instantStatus,
      message: message,
      details: cleanFeedbackText,
      points: points,
    };
  } catch (error) {
    log.error('Error running Docker tests', {
      error: error.message,
      stack: error.stack,
    });

    return {
      success: false,
      status: '⚠️',
      message: 'Es gab ein Problem bei der Überprüfung',
      details: `Es ist ein Fehler aufgetreten. Wenn das Problem weiterhin besteht, melde Dich bitte im Forum: ${error.message}`,
    };
  }
}

module.exports = {
  runDockerTests,
  createDockerContainerName,
  getDockerResourceArgs,
  getDockerResourceLogContext,
  runDockerCommand,
};

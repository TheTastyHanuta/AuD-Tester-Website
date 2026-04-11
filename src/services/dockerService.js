const { exec } = require('child_process');
const path = require('path');
const fs = require('fs-extra');
const { createScopedLogger } = require('../utils/loggerHelper');

function execPromise(command, options) {
  return new Promise(resolve => {
    exec(command, options, (error, stdout, stderr) => {
      resolve({
        error: error,
        stdout: stdout || '',
        stderr: stderr || '',
      });
    });
  });
}

async function runDockerTests(workingDir, exercise, exerciseConfig, sessionId) {
  const log = createScopedLogger({ sessionId, exercise, workingDir });

  try {
    log.info('Starting Docker tests');

    // Docker image mapping
    const testDockerImageMapping = {
      arrays: 'aufgabe2-arrays',
      caesarchiffre: 'aufgabe3-caesarchiffre',
      signalplotter: 'aufgabe3-signalplotter',
      color: 'aufgabe4-color',
      snakegame: 'aufgabe5-snake',
      sortedset: 'aufgabe7-sortedset',
      contactdb: 'aufgabe8-contactdatabase',
      binarytree: 'aufgabe9-binarysearchtree',
    };

    const dockerImage = testDockerImageMapping[exercise];
    if (!dockerImage) {
      log.warn('No Docker image configured for exercise');
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

    // Docker command
    const dockerCommand = [
      'docker',
      'run',
      '--rm',
      '-v',
      `${path.resolve(workingDir)}:/user`,
      '-v',
      `${path.resolve(resultDir)}:/result`,
      dockerImage,
    ];

    log.debug('Executing Docker command', {
      command: dockerCommand.join(' '),
      dockerImage,
      resultDir,
    });

    // Execute Docker command with timeout
    const dockerResult = await execPromise(dockerCommand.join(' '), {
      cwd: workingDir,
      timeout: 120000, // 2 minute timeout for Docker tests
    });

    // If the execPromise failed to return a result, handle it
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

    log.debug('Docker execution completed successfully', {
      dockerImage,
      returnCode: dockerResult.error?.code || 0,
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
      const comment =
        'Compile error\naudoscore crash (probably due to file misspelling or wrong encoding)';
      return {
        success: false,
        status: '❌',
        message:
          'Compile Error. Bitte überprüfe Deinen Code. Bei einer Abgabe über StudOn wird dies 0 Punkte ergeben. (Ausnahme sind die ersten zwei Übungen)',
        details: comment,
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
      dockerImage: dockerImage,
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
  execPromise,
};

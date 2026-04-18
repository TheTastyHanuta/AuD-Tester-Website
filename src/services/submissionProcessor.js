const path = require('path');
const fs = require('fs-extra');
const logger = require('../../logger');
const { createScopedLogger } = require('../utils/loggerHelper');
const { runDockerTests } = require('./dockerService');
const { compileJavaFiles } = require('./compilationService');
const {
  validateUSASCIIEncoding,
  validateRequiredFiles,
} = require('./validationService');
const config = require('../config/config');

const EXERCISES_FILE = path.join(__dirname, '../../exercises.json');

async function cleanupTempDir(tempDir, log = logger) {
  try {
    await fs.remove(tempDir);
    log.debug('Cleaned up temp directory', { tempDir });
  } catch (error) {
    if (error.code === 'EBUSY' || error.code === 'ENOTEMPTY') {
      log.debug('Files locked, attempting cleanup with delay', { tempDir });

      await new Promise(resolve => setTimeout(resolve, 1000));

      try {
        await fs.remove(tempDir);
        log.debug('Cleanup successful after delay', { tempDir });
      } catch (retryError) {
        log.warn('Could not clean up temp directory after retry', {
          tempDir,
          error: retryError.message,
        });
        setTimeout(async () => {
          try {
            await fs.remove(tempDir);
            log.debug('Delayed cleanup successful', { tempDir });
          } catch (delayedError) {
            log.warn('Delayed cleanup failed:', {
              tempDir,
              error: delayedError.message,
            });
          }
        }, 5000);
      }
    } else {
      throw error;
    }
  }
}

async function getExerciseConfig(exerciseId) {
  const exercises = await fs.readJson(EXERCISES_FILE);
  return exercises.find(ex => ex.id === exerciseId);
}

async function processSubmissionJob(job) {
  const {
    tempDir,
    exercise,
    fileNames,
    sessionId = 'no-session',
  } = job.payload;
  const log = createScopedLogger({ sessionId, exercise, jobId: job.id });

  log.info('Processing queued submission', {
    files: fileNames,
    fileCount: fileNames.length,
  });

  const exerciseConfig = await getExerciseConfig(exercise);
  if (!exerciseConfig) {
    return {
      success: false,
      status: '❌',
      message: 'Ungültige Übung ausgewählt',
      details: 'Die ausgewählte Übung wurde nicht gefunden.',
    };
  }

  const requiredFilesCheck = validateRequiredFiles(
    fileNames,
    exerciseConfig.required_files || [],
    log
  );
  if (!requiredFilesCheck.valid) {
    log.info('File validation failed in queued submission', {
      uploadedFiles: fileNames,
      requiredFiles: exerciseConfig.required_files || [],
    });
    return {
      success: false,
      status: '❌',
      message: 'Ungültige Dateiauswahl',
      details: requiredFilesCheck.details,
      deadline: exerciseConfig.deadline,
      deadlinePassed: new Date() > new Date(exerciseConfig.deadline),
    };
  }

  const encodingCheck = await validateUSASCIIEncoding(tempDir, fileNames, log);
  if (!encodingCheck.valid) {
    log.info('Encoding issues detected, but continuing with tests/compilation');
  }

  if (exerciseConfig.hasTests) {
    const dockerTestResult = await runDockerTests(
      tempDir,
      exercise,
      exerciseConfig,
      sessionId
    );

    log.info('Docker test execution completed', {
      success: dockerTestResult.success,
      status: dockerTestResult.status,
      points: dockerTestResult.points,
    });

    if (
      (new Date() > new Date(exerciseConfig.deadline) &&
        dockerTestResult.success &&
        config.SHOW_SECRET_TESTS) ||
      config.FORCE_SHOW_SECRET_TESTS
    ) {
      return {
        success: dockerTestResult.success,
        status: dockerTestResult.status,
        message: dockerTestResult.message,
        details: dockerTestResult.details,
        points: dockerTestResult.points,
        deadline: exerciseConfig.deadline,
        encodingWarning: !encodingCheck.valid ? encodingCheck.details : null,
      };
    }

    if (!dockerTestResult.success && config.SHOW_SECRET_TESTS) {
      return {
        success: dockerTestResult.success,
        status: dockerTestResult.status,
        message: dockerTestResult.message,
        details: dockerTestResult.details,
        deadline: exerciseConfig.deadline,
        encodingWarning: !encodingCheck.valid ? encodingCheck.details : null,
      };
    }

    return {
      success: dockerTestResult.success,
      status: dockerTestResult.status,
      message: dockerTestResult.message,
      deadline: exerciseConfig.deadline,
      encodingWarning: !encodingCheck.valid ? encodingCheck.details : null,
    };
  }

  const compilationResult = await compileJavaFiles(
    tempDir,
    fileNames,
    exerciseConfig,
    sessionId
  );

  if (!compilationResult.success) {
    log.info('Compilation failed without tests');
    return {
      success: false,
      status: '❌',
      message:
        'Compile Error. Bitte überprüfe Deinen Code. Bei einer Abgabe über StudOn wird dies 0 Punkte ergeben. (Ausnahme sind die ersten zwei Übungen)',
      details: compilationResult.error,
      deadline: exerciseConfig.deadline,
      encodingWarning: !encodingCheck.valid ? encodingCheck.details : null,
    };
  }

  log.info('Compilation successful without tests');
  return {
    success: true,
    status: '✅',
    message: 'Erfolgreich kompiliert',
    details:
      'Dein Code wurde erfolgreich kompiliert. Du kannst ihn so abgeben!',
    deadline: exerciseConfig.deadline,
    encodingWarning: !encodingCheck.valid ? encodingCheck.details : null,
  };
}

module.exports = {
  cleanupTempDir,
  getExerciseConfig,
  processSubmissionJob,
};

const path = require('path');
const fs = require('fs-extra');
const logger = require('../../logger');
const { createScopedLogger } = require('../utils/loggerHelper');
const { runDockerTests } = require('../services/dockerService');
const { compileJavaFiles } = require('../services/compilationService');
const {
  validateUSASCIIEncoding,
  validateRequiredFiles,
} = require('../services/validationService');
const upload = require('../middleware/uploadMiddleware');
const config = require('../config/config');

const EXERCISES_FILE = path.join(__dirname, '../../exercises.json');

async function cleanupTempDir(tempDir) {
  try {
    await fs.remove(tempDir);
    logger.debug('Cleaned up temp directory', { tempDir });
  } catch (error) {
    if (error.code === 'EBUSY' || error.code === 'ENOTEMPTY') {
      logger.debug('Files locked, attempting cleanup with delay', { tempDir });

      await new Promise(resolve => setTimeout(resolve, 1000));

      try {
        await fs.remove(tempDir);
        logger.debug('Cleanup successful after delay', { tempDir });
      } catch (retryError) {
        logger.warn('Could not clean up temp directory after retry', {
          tempDir,
          error: retryError.message,
        });
        setTimeout(async () => {
          try {
            await fs.remove(tempDir);
            logger.debug('Delayed cleanup successful', { tempDir });
          } catch (delayedError) {
            logger.warn('Delayed cleanup failed:', {
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

async function handleSubmission(req, res) {
  const uploadMiddleware = upload.array('javaFiles', 20);

  uploadMiddleware(req, res, async err => {
    if (err) {
      logger.error('File upload error', {
        sessionId: req.session?.id || 'no-session',
        error: err.message,
        stack: err.stack,
      });
      return res.status(400).json({
        error: 'File upload error',
        status: '❌',
        message: `Upload failed: ${err.message}`,
      });
    }

    try {
      if (!req.files || req.files.length === 0) {
        logger.warn('No Java files uploaded in submission', {
          sessionId: req.session?.id || 'no-session',
        });
        return res
          .status(400)
          .json({ error: 'Keine Java-Dateien hochgeladen' });
      }

      const { exercise } = req.body;
      const uploadedFiles = req.files;
      const sessionId = req.session.id;
      const log = createScopedLogger({ sessionId, exercise });

      log.info('Processing submission', {
        files: uploadedFiles.map(f => f.filename),
        fileCount: uploadedFiles.length,
      });

      const tempDir = path.join('temp', `${Date.now()}_${Math.random()}`);
      await fs.ensureDir(tempDir);

      try {
        for (const file of uploadedFiles) {
          const tempFilePath = path.join(tempDir, file.filename);
          await fs.copy(file.path, tempFilePath);
        }

        // If no exercise selected, return early
        const exerciseConfig = await getExerciseConfig(exercise);
        if (!exerciseConfig) {
          return res.json({
            success: false,
            status: '❌',
            message: 'Ungültige Übung ausgewählt',
            details: 'Die ausgewählte Übung wurde nicht gefunden.',
          });
        }

        // Validate required files
        const requiredFilesCheck = validateRequiredFiles(
          uploadedFiles.map(f => f.filename),
          exerciseConfig.required_files || []
        );
        // If required files are missing or extra files are present, return early
        if (!requiredFilesCheck.valid) {
          log.info('File validation failed in submission', {
            uploadedFiles: uploadedFiles.map(f => f.filename),
            requiredFiles: exerciseConfig.required_files || [],
          });
          return res.json({
            success: false,
            status: '❌',
            message: 'Ungültige Dateiauswahl',
            details: requiredFilesCheck.details,
            deadline: exerciseConfig.deadline,
            deadlinePassed: new Date() > new Date(exerciseConfig.deadline),
          });
        }

        // Check for non-ASCII characters in all files
        const encodingCheck = await validateUSASCIIEncoding(
          tempDir,
          uploadedFiles.map(f => f.filename)
        );
        // Log encoding issues but continue processing
        if (!encodingCheck.valid) {
          log.info(
            'Encoding issues detected, but continuing with tests/compilation'
          );
        }

        // Main testing will be handled below

        /* If exercise has tests run the correct docker image for the exercise
        This will return a status if the submission passed the tests or not.
        */
        if (exerciseConfig.hasTests) {
          // Run Docker tests for exercises with test configuration
          const dockerTestResult = await runDockerTests(
            tempDir,
            exercise,
            exerciseConfig,
            sessionId
          );

          log.info('Docker test execution completed', {
            dockerImage: dockerTestResult.dockerImage,
            success: dockerTestResult.success,
            status: dockerTestResult.status,
            points: dockerTestResult.points,
          });

          // If deadline is passed and submission passed return full feedback with points
          if (
            (new Date() > new Date(exerciseConfig.deadline) &&
              dockerTestResult.success &&
              config.SHOW_SECRET_TESTS) ||
            config.FORCE_SHOW_SECRET_TESTS
          ) {
            return res.json({
              success: dockerTestResult.success,
              status: dockerTestResult.status,
              message: dockerTestResult.message,
              details: dockerTestResult.details,
              points: dockerTestResult.points,
              deadline: exerciseConfig.deadline,
              encodingWarning: !encodingCheck.valid
                ? encodingCheck.details
                : null,
            });
            // If submission failed return with details but no points
          } else if (!dockerTestResult.success && config.SHOW_SECRET_TESTS) {
            return res.json({
              success: dockerTestResult.success,
              status: dockerTestResult.status,
              message: dockerTestResult.message,
              details: dockerTestResult.details,
              deadline: exerciseConfig.deadline,
              encodingWarning: !encodingCheck.valid
                ? encodingCheck.details
                : null,
            });
          } else {
            // If everything is fine and deadline is not passed return without detailed feedback
            return res.json({
              success: dockerTestResult.success,
              status: dockerTestResult.status,
              message: dockerTestResult.message,
              deadline: exerciseConfig.deadline,
              encodingWarning: !encodingCheck.valid
                ? encodingCheck.details
                : null,
            });
          }
        } else {
          // If no tests, just try to compile the code
          /* Compile submission Java files
          This will fail if the code does use non Java 8 features and has syntax errors.
          */
          const compilationResult = await compileJavaFiles(
            tempDir,
            uploadedFiles.map(f => f.filename),
            exerciseConfig,
            sessionId
          );
          // If submission compilation fails return early
          if (!compilationResult.success) {
            log.info('Compilation failed without tests');
            return res.json({
              success: false,
              status: '❌',
              message:
                'Compile Error. Bitte überprüfe Deinen Code. Bei einer Abgabe über StudOn wird dies 0 Punkte ergeben. (Ausnahme sind die ersten zwei Übungen)',
              details: compilationResult.error,
              deadline: exerciseConfig.deadline,
              encodingWarning: !encodingCheck.valid
                ? encodingCheck.details
                : null,
            });
          } else {
            // If compilation is successful, return success message
            log.info('Compilation successful without tests');
            return res.json({
              success: true,
              status: '✅',
              message: 'Erfolgreich kompiliert',
              details:
                'Dein Code wurde erfolgreich kompiliert. Du kannst ihn so abgeben!',
              deadline: exerciseConfig.deadline,
              encodingWarning: !encodingCheck.valid
                ? encodingCheck.details
                : null,
            });
          }
        }
      } catch (processError) {
        // If any error occurs during processing, log it and return a 500 error
        log.error('Error during submission processing', {
          error: processError.message,
          stack: processError.stack,
          tempDir,
        });
        if (!res.headersSent) {
          return res.status(500).json({
            error: 'Processing error',
            status: '⚠️',
            message: 'An error occurred while processing your submission',
          });
        }
      } finally {
        try {
          await cleanupTempDir(tempDir);
        } catch (cleanupError) {
          log.warn('Could not clean up temp directory', {
            tempDir,
            error: cleanupError.message,
          });
        }
      }
    } catch (error) {
      // Catch any unexpected errors
      logger.error('Error processing submission', {
        sessionId: req.session?.id || 'no-session',
        error: error.message,
        stack: error.stack,
        exercise: req.body?.exercise,
      });
      if (!res.headersSent) {
        res.status(500).json({
          error: 'Internal server error',
          status: '⚠️',
          message:
            'An internal error occurred while processing your submission. Please try again later.',
        });
      }
    } finally {
      if (req.files) {
        for (const file of req.files) {
          await fs.remove(file.path).catch(err =>
            logger.error('Error removing uploaded file', {
              file: file.path,
              error: err.message,
            })
          );
        }
      }
    }
  });
}

module.exports = {
  handleSubmission,
};

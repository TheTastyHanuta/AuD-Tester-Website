const crypto = require('crypto');
const path = require('path');
const fs = require('fs-extra');
const logger = require('../../logger');
const { createScopedLogger } = require('../utils/loggerHelper');
const upload = require('../middleware/uploadMiddleware');
const {
  createSubmissionJob,
  getSubmissionJob,
} = require('../services/submissionQueue');
const { cleanupTempDir } = require('../services/submissionProcessor');

async function removeUploadedFiles(files) {
  if (!files) {
    return;
  }

  for (const file of files) {
    await fs.remove(file.path).catch(err =>
      logger.error('Error removing uploaded file', {
        file: file.path,
        error: err.message,
      })
    );
  }
}

function formatFailedJob(job) {
  return {
    success: false,
    status: '⚠️',
    message: 'Überprüfung fehlgeschlagen',
    details:
      job.error ||
      'Die Überprüfung konnte nicht abgeschlossen werden. Bitte versuche es später erneut.',
  };
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
        return res.status(400).json({
          success: false,
          status: '❌',
          message: 'Keine Java-Dateien hochgeladen',
        });
      }

      const { exercise } = req.body;
      if (!exercise) {
        return res.status(400).json({
          success: false,
          status: '❌',
          message: 'Bitte wähle eine Übung aus',
        });
      }

      const jobId = crypto.randomUUID();
      const uploadedFiles = req.files;
      const fileNames = uploadedFiles.map(file => file.filename);
      const sessionId = req.session?.id || 'no-session';
      const log = createScopedLogger({ sessionId, exercise, jobId });
      const tempDir = path.join(process.cwd(), 'temp', 'submissions', jobId);

      try {
        await fs.ensureDir(tempDir);
        for (const file of uploadedFiles) {
          const tempFilePath = path.join(tempDir, file.filename);
          await fs.copy(file.path, tempFilePath);
        }

        const job = createSubmissionJob(jobId, {
          exercise,
          fileNames,
          sessionId,
          tempDir,
        });

        log.info('Queued submission for background processing', {
          files: fileNames,
          fileCount: fileNames.length,
        });

        return res.status(202).json({
          jobId: job.id,
          status: job.status,
          statusUrl: `/api/status/${job.id}`,
          message: 'Abgabe wurde angenommen und wird überprüft.',
        });
      } catch (queueError) {
        await cleanupTempDir(tempDir).catch(cleanupError => {
          log.warn('Could not clean up temp directory after queue error', {
            tempDir,
            error: cleanupError.message,
          });
        });
        throw queueError;
      }
    } catch (error) {
      logger.error('Error queueing submission', {
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
            'An internal error occurred while queueing your submission. Please try again later.',
        });
      }
    } finally {
      await removeUploadedFiles(req.files);
    }
  });
}

function getSubmissionStatus(req, res) {
  try {
    const job = getSubmissionJob(req.params.jobId);

    if (!job) {
      return res.status(404).json({
        error: 'Job not found',
        status: 'failed',
        message: 'Die angefragte Überprüfung wurde nicht gefunden.',
      });
    }

    const response = {
      jobId: job.id,
      status: job.status,
      createdAt: job.createdAt,
      updatedAt: job.updatedAt,
      startedAt: job.startedAt,
      completedAt: job.completedAt,
    };

    if (job.status === 'completed') {
      response.result = job.result;
    } else if (job.status === 'failed') {
      response.error = job.error;
      response.result = formatFailedJob(job);
    }

    return res.json(response);
  } catch (error) {
    logger.error('Error reading submission job status', {
      jobId: req.params.jobId,
      error: error.message,
      stack: error.stack,
    });
    return res.status(500).json({
      error: 'Internal server error',
      status: 'failed',
      message: 'Der Jobstatus konnte nicht gelesen werden.',
    });
  }
}

module.exports = {
  getSubmissionStatus,
  handleSubmission,
};

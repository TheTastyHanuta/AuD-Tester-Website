const logger = require('./logger');
const config = require('./src/config/config');
const { createScopedLogger } = require('./src/utils/loggerHelper');
const { checkJavaVersion } = require('./src/services/compilationService');
const {
  cleanupTempDir,
  processSubmissionJob,
} = require('./src/services/submissionProcessor');
const {
  claimNextSubmissionJob,
  closeQueue,
  cleanupExpiredSubmissionJobs,
  completeSubmissionJob,
  failSubmissionJob,
  requeueActiveSubmissionJobs,
} = require('./src/services/submissionQueue');

let stopping = false;
let lastCleanupAt = 0;

function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

async function processJob(job) {
  const log = createScopedLogger({
    jobId: job.id,
    sessionId: job.payload.sessionId,
    exercise: job.payload.exercise,
  });

  log.info('Submission worker started job');

  try {
    const result = await processSubmissionJob(job);
    completeSubmissionJob(job.id, result);
    log.info('Submission worker completed job', {
      status: result.status,
    });
  } catch (error) {
    failSubmissionJob(job.id, error);
    log.error('Submission worker failed job', {
      error: error.message,
      stack: error.stack,
    });
  } finally {
    await cleanupTempDir(job.payload.tempDir, log).catch(cleanupError => {
      log.warn('Could not clean up job temp directory', {
        tempDir: job.payload.tempDir,
        error: cleanupError.message,
      });
    });
  }
}

function cleanupOldJobsIfDue(force = false) {
  const now = Date.now();

  if (!force && now - lastCleanupAt < config.JOB_CLEANUP_INTERVAL_MS) {
    return;
  }

  lastCleanupAt = now;
  cleanupExpiredSubmissionJobs(config.JOB_RETENTION_HOURS);
}

async function workLoop() {
  await checkJavaVersion();
  requeueActiveSubmissionJobs();
  cleanupOldJobsIfDue(true);

  logger.info('Submission worker running', {
    pollIntervalMs: config.JOB_WORKER_POLL_INTERVAL_MS,
    jobRetentionHours: config.JOB_RETENTION_HOURS,
    cleanupIntervalMs: config.JOB_CLEANUP_INTERVAL_MS,
  });

  while (!stopping) {
    cleanupOldJobsIfDue();

    const job = claimNextSubmissionJob();

    if (!job) {
      await sleep(config.JOB_WORKER_POLL_INTERVAL_MS);
      continue;
    }

    await processJob(job);
  }
}

async function shutdown(signal) {
  logger.info('Submission worker shutting down', { signal });
  stopping = true;
  closeQueue();
}

process.on('SIGINT', () => shutdown('SIGINT'));
process.on('SIGTERM', () => shutdown('SIGTERM'));

workLoop().catch(error => {
  logger.error('Submission worker crashed', {
    error: error.message,
    stack: error.stack,
  });
  closeQueue();
  process.exitCode = 1;
});

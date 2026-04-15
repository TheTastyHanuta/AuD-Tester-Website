const logger = require('./logger');
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

const POLL_INTERVAL_MS = Number(
  process.env.JOB_WORKER_POLL_INTERVAL_MS || 1000
);
const JOB_RETENTION_HOURS = Number(process.env.JOB_RETENTION_HOURS || 72);
const CLEANUP_INTERVAL_MS = Number(
  process.env.JOB_CLEANUP_INTERVAL_MS || 60 * 60 * 1000
);

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
      success: result.success,
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

  if (!force && now - lastCleanupAt < CLEANUP_INTERVAL_MS) {
    return;
  }

  lastCleanupAt = now;
  cleanupExpiredSubmissionJobs(JOB_RETENTION_HOURS);
}

async function workLoop() {
  await checkJavaVersion();
  requeueActiveSubmissionJobs();
  cleanupOldJobsIfDue(true);

  logger.info('Submission worker running', {
    pollIntervalMs: POLL_INTERVAL_MS,
    jobRetentionHours: JOB_RETENTION_HOURS,
    cleanupIntervalMs: CLEANUP_INTERVAL_MS,
  });

  while (!stopping) {
    cleanupOldJobsIfDue();

    const job = claimNextSubmissionJob();

    if (!job) {
      await sleep(POLL_INTERVAL_MS);
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

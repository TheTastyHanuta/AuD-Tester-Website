const path = require('path');
const fs = require('fs-extra');
const Database = require('better-sqlite3');
const logger = require('../../logger');
const config = require('../config/config');

const DB_PATH = config.JOB_QUEUE_DB_PATH;

let db;

function getDb() {
  if (db) {
    return db;
  }

  fs.ensureDirSync(path.dirname(DB_PATH));
  db = new Database(DB_PATH);
  db.pragma('journal_mode = WAL');
  db.pragma('busy_timeout = 5000');
  db.exec(`
    CREATE TABLE IF NOT EXISTS submission_jobs (
      id TEXT PRIMARY KEY,
      status TEXT NOT NULL CHECK (status IN ('pending', 'active', 'completed', 'failed')),
      payload TEXT NOT NULL,
      result TEXT,
      error TEXT,
      attempts INTEGER NOT NULL DEFAULT 0,
      created_at TEXT NOT NULL,
      updated_at TEXT NOT NULL,
      started_at TEXT,
      completed_at TEXT
    );

    CREATE INDEX IF NOT EXISTS idx_submission_jobs_status_created_at
      ON submission_jobs (status, created_at);
  `);

  return db;
}

function parseJob(row) {
  if (!row) {
    return null;
  }

  return {
    id: row.id,
    status: row.status,
    payload: JSON.parse(row.payload),
    result: row.result ? JSON.parse(row.result) : null,
    error: row.error,
    attempts: row.attempts,
    createdAt: row.created_at,
    updatedAt: row.updated_at,
    startedAt: row.started_at,
    completedAt: row.completed_at,
  };
}

function createSubmissionJob(id, payload) {
  const now = new Date().toISOString();

  getDb()
    .prepare(
      `INSERT INTO submission_jobs
        (id, status, payload, attempts, created_at, updated_at)
       VALUES
        (@id, 'pending', @payload, 0, @now, @now)`
    )
    .run({
      id,
      payload: JSON.stringify(payload),
      now,
    });

  logger.info('Created new submission job', {
    jobId: id,
    exercise: payload.exercise,
    sessionId: payload.sessionId,
  });
  return getSubmissionJob(id);
}

function getSubmissionJob(id) {
  const row = getDb()
    .prepare('SELECT * FROM submission_jobs WHERE id = ?')
    .get(id);
  return parseJob(row);
}

const claimNextSubmissionJob = () => {
  const db = getDb();
  const transaction = db.transaction(() => {
    const row = db
      .prepare(
        `SELECT *
         FROM submission_jobs
         WHERE status = 'pending'
         ORDER BY created_at ASC
         LIMIT 1`
      )
      .get();

    if (!row) {
      return null;
    }

    const now = new Date().toISOString();
    const result = db
      .prepare(
        `UPDATE submission_jobs
         SET status = 'active',
             attempts = attempts + 1,
             started_at = @now,
             updated_at = @now,
             error = NULL
         WHERE id = @id AND status = 'pending'`
      )
      .run({ id: row.id, now });

    if (result.changes === 0) {
      return null;
    }

    const job = getSubmissionJob(row.id);
    logger.info('Claimed submission job for processing', {
      jobId: job.id,
      attempts: job.attempts,
      sessionId: job.payload.sessionId,
    });
    return job;
  });

  return transaction();
};

function completeSubmissionJob(id, result) {
  const now = new Date().toISOString();
  getDb()
    .prepare(
      `UPDATE submission_jobs
       SET status = 'completed',
           result = @result,
           error = NULL,
           completed_at = @now,
           updated_at = @now
       WHERE id = @id`
    )
    .run({
      id,
      result: JSON.stringify(result),
      now,
    });

  logger.info('Marked submission job as completed in queue', {
    jobId: id,
    status: result?.status,
  });
}

function failSubmissionJob(id, error) {
  const now = new Date().toISOString();
  const message = error instanceof Error ? error.message : String(error);

  getDb()
    .prepare(
      `UPDATE submission_jobs
       SET status = 'failed',
           error = @error,
           completed_at = @now,
           updated_at = @now
       WHERE id = @id`
    )
    .run({
      id,
      error: message,
      now,
    });

  logger.error('Marked submission job as failed in queue', {
    jobId: id,
    error: message,
  });
}

function cleanupExpiredSubmissionJobs(retentionHours = 72) {
  const retentionMs = retentionHours * 60 * 60 * 1000;
  const cutoff = new Date(Date.now() - retentionMs).toISOString();
  const result = getDb()
    .prepare(
      `DELETE FROM submission_jobs
       WHERE status IN ('completed', 'failed')
         AND COALESCE(completed_at, updated_at) < @cutoff`
    )
    .run({ cutoff });

  if (result.changes > 0) {
    logger.info('Cleaned up expired submission jobs', {
      count: result.changes,
      retentionHours,
      cutoff,
    });
  }

  return result.changes;
}

function requeueActiveSubmissionJobs() {
  const now = new Date().toISOString();
  const result = getDb()
    .prepare(
      `UPDATE submission_jobs
       SET status = 'pending',
           updated_at = @now,
           error = 'Worker stopped before this job completed; retrying.'
       WHERE status = 'active'`
    )
    .run({ now });

  if (result.changes > 0) {
    logger.warn('Requeued active submission jobs from a previous worker run', {
      count: result.changes,
    });
  }
}

function closeQueue() {
  if (db) {
    db.close();
    db = null;
  }
}

module.exports = {
  DB_PATH,
  closeQueue,
  cleanupExpiredSubmissionJobs,
  completeSubmissionJob,
  createSubmissionJob,
  failSubmissionJob,
  getSubmissionJob,
  claimNextSubmissionJob,
  requeueActiveSubmissionJobs,
};

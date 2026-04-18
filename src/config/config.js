const dotenv = require('dotenv');
const crypto = require('crypto');
const path = require('path');

dotenv.config();

function getSessionSecret() {
  const provided = process.env.SESSION_SECRET;
  const insecureDefault = 'secret-change-in-production';
  const isProduction = process.env.NODE_ENV === 'production';

  if (isProduction && (!provided || provided === insecureDefault)) {
    throw new Error(
      'SESSION_SECRET must be set to a strong, non-default value in production'
    );
  }

  if (!provided || provided === insecureDefault) {
    return crypto.randomBytes(48).toString('hex');
  }

  return provided;
}

function getPositiveIntegerEnv(name, fallback) {
  const value = Number(process.env[name]);
  return Number.isInteger(value) && value > 0 ? value : fallback;
}

const config = {
  PORT: process.env.PORT || 3000,
  HOST: process.env.BIND_HOST || '127.0.0.1',
  IS_PRODUCTION: process.env.NODE_ENV === 'production',
  SESSION_SECRET: getSessionSecret(),
  LOG_VIEWER_PASSWORD: process.env.LOG_VIEWER_PASSWORD,
  SESSION_DB_PATH:
    process.env.SESSION_DB_PATH ||
    path.join(__dirname, '../../data/admin-sessions.sqlite'),
  JOB_QUEUE_DB_PATH:
    process.env.JOB_QUEUE_DB_PATH ||
    path.join(__dirname, '../../data/submission-jobs.sqlite'),
  JOB_WORKER_POLL_INTERVAL_MS: getPositiveIntegerEnv(
    'JOB_WORKER_POLL_INTERVAL_MS',
    1000
  ),
  JOB_RETENTION_HOURS: getPositiveIntegerEnv('JOB_RETENTION_HOURS', 72),
  JOB_CLEANUP_INTERVAL_MS: getPositiveIntegerEnv(
    'JOB_CLEANUP_INTERVAL_MS',
    60 * 60 * 1000
  ),
  SHOW_SECRET_TESTS: process.env.SHOW_SECRET_TESTS === 'true',
  FORCE_SHOW_SECRET_TESTS: process.env.FORCE_SHOW_SECRET_TESTS === 'true',
  JAVA_COMPILER_DOCKER_IMAGE:
    process.env.JAVA_COMPILER_DOCKER_IMAGE || 'eclipse-temurin:17-jdk',
  JAVA_COMPILE_TIMEOUT_MS: getPositiveIntegerEnv(
    'JAVA_COMPILE_TIMEOUT_MS',
    60000
  ),
  DOCKER_TEST_TIMEOUT_MS: getPositiveIntegerEnv(
    'DOCKER_TEST_TIMEOUT_MS',
    120000
  ),
  DOCKER_MEMORY_LIMIT: process.env.DOCKER_MEMORY_LIMIT || '512m',
  DOCKER_CPU_LIMIT: process.env.DOCKER_CPU_LIMIT || '0.5',
  DOCKER_PIDS_LIMIT: process.env.DOCKER_PIDS_LIMIT || '100',
  DOCKER_TEST_IMAGE_MAPPING: {
    arrays: 'aufgabe2-arrays',
    caesarchiffre: 'aufgabe3-caesarchiffre',
    signalplotter: 'aufgabe3-signalplotter',
    color: 'aufgabe4-color',
    snakegame: 'aufgabe5-snake',
    sortedset: 'aufgabe7-sortedset',
    contactdb: 'aufgabe8-contactdatabase',
    binarytree: 'aufgabe9-binarysearchtree',
  },

  // Allowed origins for CORS and client logging
  getAllowedOrigins: function () {
    return this.IS_PRODUCTION
      ? ['https://aud-mt.de']
      : ['http://localhost:3000', 'http://127.0.0.1:3000', 'https://aud-mt.de'];
  },
};

module.exports = config;

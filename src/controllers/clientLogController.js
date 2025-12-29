const logger = require('../../logger');
const config = require('../config/config');

const allowedOrigins = config.getAllowedOrigins();

async function handleClientLogOptions(req, res) {
  const origin = req.get('Origin');
  if (allowedOrigins.includes(origin)) {
    res.header('Access-Control-Allow-Origin', origin);
    res.header('Access-Control-Allow-Methods', 'POST, OPTIONS');
    res.header('Access-Control-Allow-Headers', 'Content-Type');
    res.sendStatus(204);
  } else {
    logger.warn('Client-log OPTIONS blocked', { origin, ip: req.ip });
    res.sendStatus(403);
  }
}

async function handleClientLog(req, res) {
  const origin = req.get('Origin');
  if (origin && !allowedOrigins.includes(origin)) {
    logger.warn('Client-log POST blocked', { origin, ip: req.ip });
    return res.status(403).json({ error: 'Origin not allowed' });
  }
  res.header('Access-Control-Allow-Origin', origin);
  const { level = 'info', message = '', meta = {}, url } = req.body || {};
  // Sanitize level
  const allowedLevels = ['info', 'warn', 'error', 'debug'];
  const logLevel = allowedLevels.includes(level) ? level : 'info';
  // Compose log meta
  const logMeta = {
    ...meta,
    url,
    ip: req.ip,
    sessionId: req.session?.id || 'no-session',
  };
  try {
    logger[logLevel](message, logMeta);
  } catch (e) {
    logger.error('Failed to log client message', {
      error: e.message,
      message,
      logLevel,
      logMeta,
    });
  }
  res.json({ ok: true });
}

module.exports = {
  handleClientLogOptions,
  handleClientLog,
};

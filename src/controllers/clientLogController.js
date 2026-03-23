const logger = require('../../logger');
const config = require('../config/config');

const allowedOrigins = config.getAllowedOrigins();
const MAX_MESSAGE_LENGTH = 2000;
const MAX_META_KEYS = 40;
const MAX_META_VALUE_LENGTH = 500;

function sanitizeMeta(meta) {
  if (!meta || typeof meta !== 'object' || Array.isArray(meta)) {
    return {};
  }

  const safeMeta = {};
  const entries = Object.entries(meta).slice(0, MAX_META_KEYS);

  for (const [rawKey, rawValue] of entries) {
    const key = String(rawKey).slice(0, 80);

    if (
      typeof rawValue === 'string' ||
      typeof rawValue === 'number' ||
      typeof rawValue === 'boolean' ||
      rawValue === null
    ) {
      safeMeta[key] =
        typeof rawValue === 'string'
          ? rawValue.slice(0, MAX_META_VALUE_LENGTH)
          : rawValue;
    } else {
      safeMeta[key] = String(rawValue).slice(0, MAX_META_VALUE_LENGTH);
    }
  }

  return safeMeta;
}

async function handleClientLogOptions(req, res) {
  const origin = req.get('Origin');
  if (allowedOrigins.includes(origin)) {
    res.header('Access-Control-Allow-Origin', origin);
    res.header('Access-Control-Allow-Credentials', 'true');
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

  if (origin && allowedOrigins.includes(origin)) {
    res.header('Access-Control-Allow-Origin', origin);
    res.header('Access-Control-Allow-Credentials', 'true');
  }

  const { level = 'info', message = '', meta = {}, url } = req.body || {};

  // Sanitize level
  const allowedLevels = ['info', 'warn', 'error', 'debug'];
  const logLevel = allowedLevels.includes(level) ? level : 'info';

  // Sanitize message and metadata to prevent oversized/untrusted log payloads.
  const safeMessage =
    typeof message === 'string'
      ? message.slice(0, MAX_MESSAGE_LENGTH)
      : '[invalid-message]';
  const safeMeta = sanitizeMeta(meta);
  const safeUrl = typeof url === 'string' ? url.slice(0, 2048) : undefined;

  const logMeta = {
    ...safeMeta,
    url: safeUrl,
    ip: req.ip,
    sessionId: req.session?.id || 'no-session',
  };

  try {
    logger[logLevel](safeMessage, logMeta);
  } catch (e) {
    logger.error('Failed to log client message', {
      error: e.message,
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

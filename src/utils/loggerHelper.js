const logger = require('../../logger');

/**
 * Creates a scoped logger with predefined context
 * @param {Object} context - Base context to include in all log messages
 * @returns {Object} Logger with info, warn, error, debug methods
 */
function createScopedLogger(context = {}) {
  return {
    info: (message, meta = {}) => logger.info(message, { ...context, ...meta }),
    warn: (message, meta = {}) => logger.warn(message, { ...context, ...meta }),
    error: (message, meta = {}) =>
      logger.error(message, { ...context, ...meta }),
    debug: (message, meta = {}) =>
      logger.debug(message, { ...context, ...meta }),
  };
}

module.exports = { createScopedLogger };

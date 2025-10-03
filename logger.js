const winston = require('winston');
const DailyRotateFile = require('winston-daily-rotate-file');
const path = require('path');

// Create logs directory if it doesn't exist
const fs = require('fs');
const logsDir = path.join(__dirname, 'logs');
if (!fs.existsSync(logsDir)) {
  fs.mkdirSync(logsDir);
}

// Define log format
const logFormat = winston.format.combine(
  winston.format.timestamp({
    format: 'YYYY-MM-DD HH:mm:ss',
  }),
  winston.format.errors({ stack: true }),
  winston.format.json()
);

// Define console format for development
const consoleFormat = winston.format.combine(
  winston.format.colorize(),
  winston.format.timestamp({
    format: 'YYYY-MM-DD HH:mm:ss',
  }),
  winston.format.printf(({ timestamp, level, message, ...meta }) => {
    return `${timestamp} [${level}]: ${message} ${Object.keys(meta).length ? JSON.stringify(meta, null, 2) : ''}`;
  })
);

// Define file format for log files
const fileFormat = winston.format.combine(
  winston.format.timestamp({ format: 'YYYY-MM-DD HH:mm:ss' }),
  winston.format.errors({ stack: true }),
  winston.format.printf(({ timestamp, level, message, ...meta }) => {
    return `${timestamp} [${level}]: ${message} ${Object.keys(meta).length ? JSON.stringify(meta) : ''}`;
  })
);

// Create the logger
const logger = winston.createLogger({
  level: 'debug',
  format: logFormat,
  defaultMeta: { service: 'aud-tester' },
  transports: [
    // Error log - only errors
    new DailyRotateFile({
      filename: path.join(logsDir, 'error-%DATE%.log'),
      datePattern: 'YYYY-MM-DD',
      level: 'error',
      maxSize: '20m',
      maxFiles: '14d',
      zippedArchive: true,
      format: fileFormat,
    }),

    // Warning log - only warnings
    new DailyRotateFile({
      filename: path.join(logsDir, 'warn-%DATE%.log'),
      datePattern: 'YYYY-MM-DD',
      level: 'warn',
      maxSize: '20m',
      maxFiles: '14d',
      zippedArchive: true,
      format: fileFormat,
    }),

    // Combined log - all levels
    new DailyRotateFile({
      filename: path.join(logsDir, 'combined-%DATE%.log'),
      datePattern: 'YYYY-MM-DD',
      maxSize: '20m',
      maxFiles: '30d',
      zippedArchive: true,
      format: fileFormat,
    }),

    // Application log - info and above (excluding debug)
    new DailyRotateFile({
      filename: path.join(logsDir, 'app-%DATE%.log'),
      datePattern: 'YYYY-MM-DD',
      level: 'info',
      maxSize: '20m',
      maxFiles: '30d',
      zippedArchive: true,
      format: fileFormat,
    }),
  ],
});

// Add console transport for development
if (process.env.NODE_ENV !== 'production') {
  logger.add(
    new winston.transports.Console({
      format: consoleFormat,
      level: 'debug',
    })
  );
} else {
  // In production, still log to console but with less verbosity
  logger.add(
    new winston.transports.Console({
      format: consoleFormat,
      level: 'info',
    })
  );
}

// Create a stream object for Morgan HTTP logging middleware
logger.stream = {
  write: message => {
    logger.info(message.trim());
  },
};

module.exports = logger;

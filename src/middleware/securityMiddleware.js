const helmet = require('helmet');
const cors = require('cors');
const config = require('../config/config');
const logger = require('../../logger');

const allowedOrigins = config.getAllowedOrigins();

function configureHelmet(app) {
  // Main Helmet configuration for security headers
  app.use(
    helmet({
      crossOriginResourcePolicy: { policy: 'same-site' },
      contentSecurityPolicy: {
        directives: {
          defaultSrc: ["'self'"],
          scriptSrc: ["'self'", 'https://www.googletagmanager.com'],
          styleSrc: [
            "'self'",
            "'unsafe-inline'",
            'https://fonts.googleapis.com',
          ],
          fontSrc: ["'self'", 'https://fonts.gstatic.com', 'data:'],
          imgSrc: ["'self'", 'data:'],
          connectSrc: config.IS_PRODUCTION
            ? [
                "'self'",
                'https://aud-mt.de',
                'wss:',
                'https://region1.google-analytics.com',
                'https://www.google-analytics.com',
              ]
            : [
                "'self'",
                'https://aud-mt.de',
                'http://localhost:3000',
                'ws:',
                'wss:',
                'https://region1.google-analytics.com',
                'https://www.google-analytics.com',
              ],
          objectSrc: ["'none'"],
          frameAncestors: ["'self'"],
          upgradeInsecureRequests: config.IS_PRODUCTION ? [] : null,
        },
      },
    })
  );

  // CSP for /admin/logs route
  app.use(
    '/admin/logs',
    helmet.contentSecurityPolicy({
      directives: {
        defaultSrc: ["'self'"],
        scriptSrc: [
          "'self'",
          'https://www.googletagmanager.com',
          "'unsafe-inline'",
        ],
        styleSrc: ["'self'", "'unsafe-inline'", 'https://fonts.googleapis.com'],
        imgSrc: ["'self'", 'data:'],
        connectSrc: config.IS_PRODUCTION
          ? ["'self'", 'https://aud-mt.de', 'wss:']
          : [
              "'self'",
              'https://aud-mt.de',
              'http://localhost:3000',
              'ws:',
              'wss:',
            ],
        fontSrc: ["'self'", 'https://fonts.gstatic.com', 'data:'],
        objectSrc: ["'none'"],
        frameAncestors: ["'self'"],
        upgradeInsecureRequests: config.IS_PRODUCTION ? [] : null,
      },
    })
  );
}

function configureCors(app) {
  app.use(
    cors({
      origin: function (origin, callback) {
        // Allow requests with no origin (like mobile apps, Postman, same-origin, or direct navigation)
        // Some browsers send "null" as a string instead of undefined
        if (!origin || origin === 'null') {
          return callback(null, true);
        }
        if (allowedOrigins.includes(origin)) {
          callback(null, true);
        } else {
          logger.warn('CORS blocked request', { origin, ip: this?.ip });
          callback(new Error('Not allowed by CORS'));
        }
      },
      credentials: true,
    })
  );
}

module.exports = {
  configureHelmet,
  configureCors,
};

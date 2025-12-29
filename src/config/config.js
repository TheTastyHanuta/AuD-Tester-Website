const dotenv = require('dotenv');

dotenv.config();

const config = {
  PORT: process.env.PORT || 3000,
  HOST: process.env.BIND_HOST || '127.0.0.1',
  IS_PRODUCTION: process.env.NODE_ENV === 'production',
  SESSION_SECRET: process.env.SESSION_SECRET || 'secret-change-in-production',
  LOG_VIEWER_PASSWORD: process.env.LOG_VIEWER_PASSWORD,
  SHOW_SECRET_TESTS: process.env.SHOW_SECRET_TESTS === 'true',
  FORCE_SHOW_SECRET_TESTS: process.env.FORCE_SHOW_SECRET_TESTS === 'true',

  // Allowed origins for CORS and client logging
  getAllowedOrigins: function () {
    return this.IS_PRODUCTION
      ? ['https://aud-mt.de']
      : ['http://localhost:3000', 'http://127.0.0.1:3000', 'https://aud-mt.de'];
  },
};

module.exports = config;

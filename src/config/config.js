const dotenv = require('dotenv');
const crypto = require('crypto');

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

const config = {
  PORT: process.env.PORT || 3000,
  HOST: process.env.BIND_HOST || '127.0.0.1',
  IS_PRODUCTION: process.env.NODE_ENV === 'production',
  SESSION_SECRET: getSessionSecret(),
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

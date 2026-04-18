const helmet = require('helmet');
const cors = require('cors');
const config = require('../config/config');
const logger = require('../../logger');

const allowedOrigins = config.getAllowedOrigins();

function normalizeOrigin(value) {
  if (typeof value !== 'string') return '';
  return value.trim().replace(/\/$/, '').toLowerCase();
}

function getHostCandidates(req) {
  const candidates = new Set();
  const host = req.get('x-forwarded-host') || req.get('host');

  if (!host) {
    return candidates;
  }

  const protocols = new Set();
  const forwardedProto = req.get('x-forwarded-proto');

  if (forwardedProto) {
    forwardedProto
      .split(',')
      .map(p => p.trim().toLowerCase())
      .filter(Boolean)
      .forEach(p => protocols.add(p));
  }

  if (req.protocol) {
    protocols.add(req.protocol.toLowerCase());
  }

  // Keep both as fallback in case proxy protocol headers are inconsistent.
  protocols.add('https');
  protocols.add('http');

  for (const proto of protocols) {
    candidates.add(normalizeOrigin(`${proto}://${host}`));
  }

  return candidates;
}

function isLikelyTopLevelNavigation(req) {
  const mode = (req.get('sec-fetch-mode') || '').toLowerCase();
  const dest = (req.get('sec-fetch-dest') || '').toLowerCase();
  const site = (req.get('sec-fetch-site') || '').toLowerCase();
  const accept = (req.get('accept') || '').toLowerCase();

  const safeMethod = ['GET', 'HEAD', 'POST'].includes(req.method);
  const navigateSignal =
    mode === 'navigate' ||
    dest === 'document' ||
    (accept.includes('text/html') && !accept.includes('application/json'));
  const siteSignal =
    !site || ['same-origin', 'same-site', 'none'].includes(site);

  return safeMethod && navigateSignal && siteSignal;
}

function isAllowedOrigin(req, origin) {
  const normalizedOrigin = normalizeOrigin(origin);

  if (!normalizedOrigin) {
    return true;
  }

  if (normalizedOrigin === 'null') {
    // Some browsers/privacy modes send Origin: null on top-level navigations/forms.
    return isLikelyTopLevelNavigation(req);
  }

  if (allowedOrigins.map(normalizeOrigin).includes(normalizedOrigin)) {
    return true;
  }

  return getHostCandidates(req).has(normalizedOrigin);
}

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

  // Stricter CSP for the unified admin shell and admin APIs.
  app.use(
    '/admin',
    helmet.contentSecurityPolicy({
      directives: {
        defaultSrc: ["'self'"],
        scriptSrc: ["'self'"],
        styleSrc: ["'self'"],
        imgSrc: ["'self'", 'data:'],
        connectSrc: ["'self'"],
        fontSrc: ["'self'", 'data:'],
        objectSrc: ["'none'"],
        frameAncestors: ["'self'"],
        upgradeInsecureRequests: config.IS_PRODUCTION ? [] : null,
      },
    })
  );
}

function configureCors(app) {
  app.use((req, res, next) => {
    const origin = req.get('Origin');

    if (isAllowedOrigin(req, origin)) {
      return next();
    }

    logger.warn('CORS blocked request', {
      origin,
      method: req.method,
      path: req.originalUrl,
      host: req.get('host'),
      forwardedHost: req.get('x-forwarded-host'),
      forwardedProto: req.get('x-forwarded-proto'),
      fetchMode: req.get('sec-fetch-mode'),
      fetchDest: req.get('sec-fetch-dest'),
      fetchSite: req.get('sec-fetch-site'),
      ip: req.ip,
    });

    return res.status(403).send('Not allowed by CORS');
  });

  app.use(
    cors({
      origin: true,
      credentials: true,
    })
  );
}

module.exports = {
  configureHelmet,
  configureCors,
};

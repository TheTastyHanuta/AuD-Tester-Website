const crypto = require('crypto');

function tokensMatch(left, right) {
  if (typeof left !== 'string' || typeof right !== 'string') {
    return false;
  }

  const leftBuffer = Buffer.from(left, 'utf8');
  const rightBuffer = Buffer.from(right, 'utf8');

  if (leftBuffer.length !== rightBuffer.length) {
    return false;
  }

  return crypto.timingSafeEqual(leftBuffer, rightBuffer);
}

function requireAdminCsrf(req, res, next) {
  const sessionToken = req.session?.adminCsrfToken;
  const requestToken = req.get('X-CSRF-Token');

  if (!tokensMatch(sessionToken, requestToken)) {
    return res.status(403).json({ error: 'Invalid admin CSRF token' });
  }

  return next();
}

module.exports = requireAdminCsrf;

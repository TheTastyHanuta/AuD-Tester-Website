const rateLimit = require('express-rate-limit');

const adminLoginLimiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 10,
  standardHeaders: true,
  legacyHeaders: false,
  message: 'Too many login attempts. Please try again in a few minutes.',
});

const submissionLimiter = rateLimit({
  windowMs: 10 * 60 * 1000,
  max: 120,
  standardHeaders: true,
  legacyHeaders: false,
  handler: (req, res) => {
    return res.status(429).json({
      success: false,
      status: '⚠️',
      message:
        'Zu viele Abgaben in kurzer Zeit. Bitte warte kurz und versuche es erneut.',
    });
  },
});

const clientLogLimiter = rateLimit({
  windowMs: 5 * 60 * 1000,
  max: 200,
  standardHeaders: true,
  legacyHeaders: false,
  handler: (req, res) => {
    return res.status(429).json({ error: 'Too many log requests' });
  },
});

module.exports = {
  adminLoginLimiter,
  submissionLimiter,
  clientLogLimiter,
};

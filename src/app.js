const express = require('express');
const session = require('express-session');
const morgan = require('morgan');
const config = require('./config/config');
const logger = require('../logger');
const {
  configureHelmet,
  configureCors,
} = require('./middleware/securityMiddleware');
const routes = require('./routes');

const app = express();

app.set('trust proxy', 1);

// Session configuration
app.use(
  session({
    secret: config.SESSION_SECRET,
    resave: false,
    saveUninitialized: true,
    cookie: {
      secure: config.IS_PRODUCTION,
      httpOnly: true,
      maxAge: 24 * 60 * 60 * 1000,
      sameSite: 'lax',
    },
  })
);

// Security middleware (Helmet and CORS)
configureHelmet(app);

// Body parsers
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(express.static('public', { dotfiles: 'deny' }));

// CORS configuration
configureCors(app);

// HTTP request logging with session ID
morgan.token('sessionId', function (req, res) {
  return req.session ? req.session.id : 'no-session';
});

app.use(
  morgan(
    ':remote-addr [:sessionId] - ":method :url HTTP/:http-version" :status :res[content-length] ":referrer" ":user-agent"',
    {
      stream: logger.stream,
    }
  )
);

// Mount all routes
app.use('/', routes);

module.exports = app;

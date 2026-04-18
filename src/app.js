const express = require('express');
const session = require('express-session');
const morgan = require('morgan');
const fs = require('fs-extra');
const path = require('path');
const Database = require('better-sqlite3');
const config = require('./config/config');
const logger = require('../logger');
const SqliteSessionStore = require('./services/sqliteSessionStore');
const {
  configureHelmet,
  configureCors,
} = require('./middleware/securityMiddleware');
const routes = require('./routes');

const app = express();

app.set('trust proxy', 1);

fs.ensureDirSync(path.dirname(config.SESSION_DB_PATH));
const sessionDb = new Database(config.SESSION_DB_PATH);
sessionDb.pragma('journal_mode = WAL');
sessionDb.pragma('busy_timeout = 5000');

// Session configuration
app.use(
  session({
    name: 'aud.sid',
    store: new SqliteSessionStore({
      client: sessionDb,
      cleanupIntervalMs: 15 * 60 * 1000,
    }),
    secret: config.SESSION_SECRET,
    resave: false,
    saveUninitialized: false,
    unset: 'destroy',
    proxy: config.IS_PRODUCTION,
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
app.use(express.json({ limit: '300kb' }));
app.use(express.urlencoded({ extended: false, limit: '300kb' }));
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

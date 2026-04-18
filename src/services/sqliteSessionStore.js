const session = require('express-session');

const DEFAULT_MAX_AGE_MS = 24 * 60 * 60 * 1000;
const DEFAULT_CLEANUP_INTERVAL_MS = 15 * 60 * 1000;

function safeCallback(callback, error, result) {
  if (typeof callback === 'function') {
    callback(error, result);
  }
}

function getExpiryMs(sess) {
  const cookie = sess?.cookie || {};

  if (cookie.expires) {
    const expires = new Date(cookie.expires).getTime();
    if (!Number.isNaN(expires)) {
      return expires;
    }
  }

  if (typeof cookie.maxAge === 'number' && cookie.maxAge > 0) {
    return Date.now() + cookie.maxAge;
  }

  return Date.now() + DEFAULT_MAX_AGE_MS;
}

class SqliteSessionStore extends session.Store {
  constructor(options = {}) {
    super();

    if (!options.client) {
      throw new Error('SqliteSessionStore requires a better-sqlite3 client');
    }

    this.client = options.client;
    this.tableName = options.tableName || 'admin_sessions';
    this.cleanupIntervalMs =
      options.cleanupIntervalMs || DEFAULT_CLEANUP_INTERVAL_MS;

    this.createTable();
    this.cleanupExpiredSessions();
    this.cleanupTimer = setInterval(
      () => this.cleanupExpiredSessions(),
      this.cleanupIntervalMs
    );
    this.cleanupTimer.unref?.();
  }

  createTable() {
    this.client
      .prepare(
        `CREATE TABLE IF NOT EXISTS ${this.tableName} (
          sid TEXT PRIMARY KEY,
          sess TEXT NOT NULL,
          expire INTEGER NOT NULL
        )`
      )
      .run();

    this.client
      .prepare(
        `CREATE INDEX IF NOT EXISTS idx_${this.tableName}_expire
         ON ${this.tableName} (expire)`
      )
      .run();
  }

  cleanupExpiredSessions() {
    try {
      this.client
        .prepare(`DELETE FROM ${this.tableName} WHERE expire <= ?`)
        .run(Date.now());
    } catch (_) {
      // Session cleanup must not crash the app.
    }
  }

  get(sid, callback) {
    try {
      const row = this.client
        .prepare(
          `SELECT sess FROM ${this.tableName}
           WHERE sid = ? AND expire > ?`
        )
        .get(sid, Date.now());

      safeCallback(callback, null, row ? JSON.parse(row.sess) : null);
    } catch (error) {
      safeCallback(callback, error);
    }
  }

  set(sid, sess, callback) {
    try {
      this.client
        .prepare(
          `INSERT INTO ${this.tableName} (sid, sess, expire)
           VALUES (@sid, @sess, @expire)
           ON CONFLICT(sid) DO UPDATE SET
             sess = excluded.sess,
             expire = excluded.expire`
        )
        .run({
          sid,
          sess: JSON.stringify(sess),
          expire: getExpiryMs(sess),
        });

      safeCallback(callback, null);
    } catch (error) {
      safeCallback(callback, error);
    }
  }

  touch(sid, sess, callback) {
    try {
      this.client
        .prepare(
          `UPDATE ${this.tableName}
           SET expire = ?
           WHERE sid = ? AND expire > ?`
        )
        .run(getExpiryMs(sess), sid, Date.now());

      safeCallback(callback, null);
    } catch (error) {
      safeCallback(callback, error);
    }
  }

  destroy(sid, callback) {
    try {
      this.client
        .prepare(`DELETE FROM ${this.tableName} WHERE sid = ?`)
        .run(sid);

      safeCallback(callback, null);
    } catch (error) {
      safeCallback(callback, error);
    }
  }

  clear(callback) {
    try {
      this.client.prepare(`DELETE FROM ${this.tableName}`).run();
      safeCallback(callback, null);
    } catch (error) {
      safeCallback(callback, error);
    }
  }

  length(callback) {
    try {
      const row = this.client
        .prepare(`SELECT COUNT(*) AS count FROM ${this.tableName}`)
        .get();

      safeCallback(callback, null, row.count);
    } catch (error) {
      safeCallback(callback, error);
    }
  }
}

module.exports = SqliteSessionStore;

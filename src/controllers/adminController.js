const path = require('path');
const fs = require('fs-extra');
const { spawn } = require('child_process');
const crypto = require('crypto');
const logger = require('../../logger');
const config = require('../config/config');
const {
  getLogDir,
  listLogFiles,
  getLatestLogFile,
} = require('../services/logService');
const {
  getAllExercises,
  updateExerciseDeadline,
  updateExerciseDeadlines,
} = require('../services/exerciseService');

function sanitizeReturnPath(ret) {
  if (typeof ret !== 'string') return '/admin';
  if (!ret.startsWith('/')) return '/admin';
  if (ret.startsWith('//')) return '/admin';
  if (ret.includes('\n') || ret.includes('\r')) return '/admin';
  if (!ret.startsWith('/admin')) return '/admin';
  return ret;
}

function isValidPassword(input, expected) {
  if (typeof input !== 'string' || typeof expected !== 'string') return false;

  const inputBuffer = Buffer.from(input, 'utf8');
  const expectedBuffer = Buffer.from(expected, 'utf8');

  if (inputBuffer.length !== expectedBuffer.length) {
    return false;
  }

  return crypto.timingSafeEqual(inputBuffer, expectedBuffer);
}

function ensureAdminCsrfToken(req) {
  if (!req.session.adminCsrfToken) {
    req.session.adminCsrfToken = crypto.randomBytes(32).toString('hex');
  }

  return req.session.adminCsrfToken;
}

async function showLoginPage(req, res) {
  if (!config.LOG_VIEWER_PASSWORD) {
    return res
      .status(503)
      .send(
        '<h1>Log viewer disabled</h1><p>Set LOG_VIEWER_PASSWORD in environment to enable.</p>'
      );
  }
  res.sendFile(path.join(__dirname, '../../public', 'admin-login.html'));
}

async function handleLogin(req, res) {
  if (!config.LOG_VIEWER_PASSWORD) {
    return res
      .status(503)
      .send('Log viewer disabled. Missing LOG_VIEWER_PASSWORD');
  }

  const { password } = req.body || {};
  const returnTo = sanitizeReturnPath(
    (req.body && req.body.return) || req.query.return
  );
  const ok = isValidPassword(password, config.LOG_VIEWER_PASSWORD);

  logger.info('Admin login attempt', {
    ip: req.ip,
    userAgent: req.get('User-Agent'),
    success: ok,
  });

  if (!ok) {
    return res.status(401).send('Unauthorized');
  }

  if (!req.session) {
    logger.error('Session missing during admin login', { ip: req.ip });
    return res.status(500).send('Session unavailable');
  }

  req.session.regenerate(error => {
    if (error) {
      logger.error('Failed to regenerate admin session', {
        ip: req.ip,
        error: error.message,
      });
      return res.status(500).send('Login failed');
    }

    req.session.isLogAdmin = true;
    ensureAdminCsrfToken(req);
    return res.redirect(returnTo);
  });
}

async function handleLogout(req, res) {
  if (req.session) {
    req.session.destroy(() => {
      res.redirect('/');
    });
  } else {
    res.redirect('/');
  }
}

async function showAdminPage(req, res) {
  res.sendFile(path.join(__dirname, '../../private', 'admin.html'));
}

async function showLogsPage(req, res) {
  res.redirect('/admin?tab=logs');
}

async function showExercisesPage(req, res) {
  res.redirect('/admin?tab=exercises');
}

async function getAdminSession(req, res) {
  res.json({
    authenticated: true,
    csrfToken: ensureAdminCsrfToken(req),
  });
}

function getValidatedLogType(req) {
  const type = (req.query.type || 'app').toString();
  if (!['app', 'combined', 'error'].includes(type)) {
    const error = new Error('Invalid log type');
    error.statusCode = 400;
    throw error;
  }
  return type;
}

async function listLogs(req, res) {
  try {
    const type = getValidatedLogType(req);
    const files = await listLogFiles(type);
    res.json({ type, files });
  } catch (e) {
    logger.error('Error listing log files', {
      error: e.message,
      type: req.query.type,
    });
    res
      .status(e.statusCode || 500)
      .json({ error: e.statusCode ? e.message : 'Could not list log files' });
  }
}

async function getLogFile(req, res) {
  try {
    const type = getValidatedLogType(req);
    const name = (req.query.name || '').toString();
    const raw = req.query.raw === '1';
    if (!/^[A-Za-z0-9][A-Za-z0-9._-]*\.log(\.gz)?$/.test(name)) {
      return res.status(400).send('Invalid filename');
    }
    const dir = path.resolve(getLogDir(type));
    const full = path.resolve(dir, name);
    if (!full.startsWith(`${dir}${path.sep}`)) {
      return res.status(400).send('Invalid path');
    }

    const st = await fs.lstat(full).catch(() => null);
    if (!st || !st.isFile() || st.isSymbolicLink()) {
      return res.status(404).send('Not found');
    }

    const exists = await fs.pathExists(full);
    if (!exists) return res.status(404).send('Not found');
    res.setHeader('Content-Type', 'text/plain; charset=utf-8');
    if (raw) {
      res.setHeader(
        'Content-Disposition',
        `attachment; filename="${name.replace(/"/g, '')}"`
      );
    }
    if (name.endsWith('.gz')) {
      const zlib = require('zlib');
      fs.createReadStream(full).pipe(zlib.createGunzip()).pipe(res);
    } else {
      fs.createReadStream(full).pipe(res);
    }
  } catch (e) {
    res.status(e.statusCode || 500).send(e.statusCode ? e.message : 'Error');
  }
}

async function streamLogSSE(req, res) {
  let type;
  try {
    type = getValidatedLogType(req);
  } catch (e) {
    return res.status(e.statusCode || 400).send(e.message);
  }

  const dir = getLogDir(type);
  await fs.ensureDir(dir);
  let currentName = (await getLatestLogFile(type)) || '';

  res.writeHead(200, {
    'Content-Type': 'text/event-stream',
    'Cache-Control': 'no-cache',
    'X-Accel-Buffering': 'no',
    Connection: 'keep-alive',
  });

  function sseEvent(eventName, payload) {
    res.write(`event: ${eventName}\n`);
    res.write(`data: ${JSON.stringify(payload)}\n\n`);
  }

  function sseSend(line) {
    res.write(`data: ${line.replace(/\r/g, '\\r').replace(/\n/g, '\\n')}\n\n`);
  }

  // Helper to start tail on a file
  let tailProc = null;
  function startTail(filePath) {
    if (tailProc) {
      try {
        tailProc.kill();
      } catch (_) {}
    }
    const full = path.resolve(dir, filePath);
    sseEvent('status', {
      state: 'connected',
      type,
      file: filePath,
      reset: true,
      time: new Date().toISOString(),
    });

    // Stream the full current log file first, then continue following it.
    tailProc = spawn('tail', ['-n', '+1', '-F', full]);
    tailProc.stdout.setEncoding('utf8');
    let pendingLine = '';
    tailProc.stdout.on('data', chunk => {
      const lines = `${pendingLine}${chunk}`.split(/\r?\n/);
      pendingLine = lines.pop() || '';
      for (const l of lines) {
        if (l.length) sseSend(l);
      }
    });
    tailProc.stderr.on('data', err => {
      logger.warn('tail stderr', { err: String(err) });
    });
    tailProc.on('error', err => {
      logger.error('tail failed', { error: err.message });
      sseEvent('status', {
        state: 'error',
        message: err.message,
        time: new Date().toISOString(),
      });
      sseSend(`Tail error: ${err.message}`);
    });
  }

  if (currentName) {
    startTail(currentName);
  } else {
    sseEvent('status', {
      state: 'waiting',
      type,
      message: 'No log file yet',
      time: new Date().toISOString(),
    });
  }

  const heartbeat = setInterval(() => {
    sseEvent('heartbeat', { time: new Date().toISOString() });
  }, 25000);

  const rotationCheck = setInterval(async () => {
    try {
      const latest = await getLatestLogFile(type);
      if (latest && latest !== currentName) {
        currentName = latest;
        sseEvent('status', {
          state: 'switching',
          file: latest,
          time: new Date().toISOString(),
        });
        sseSend(`[switching to ${latest}]`);
        startTail(latest);
      }
    } catch (e) {
      logger.warn('Failed to check latest log file', { error: e.message });
    }
  }, 30000);

  req.on('close', () => {
    clearInterval(heartbeat);
    clearInterval(rotationCheck);
    if (tailProc) {
      try {
        tailProc.kill();
      } catch (_) {}
    }
  });
}

async function getExercises(req, res) {
  try {
    const exercises = await getAllExercises();
    res.json({ exercises });
  } catch (error) {
    logger.error('Error fetching exercises for admin', {
      error: error.message,
    });
    res.status(500).json({ error: 'Failed to fetch exercises' });
  }
}

async function updateDeadline(req, res) {
  const { id } = req.params;
  const { deadline } = req.body;

  if (!deadline) {
    return res.status(400).json({ error: 'Deadline is required' });
  }

  try {
    const updatedExercise = await updateExerciseDeadline(id, deadline);
    logger.info('Admin updated exercise deadline', {
      exerciseId: id,
      deadline,
      ip: req.ip,
      userAgent: req.get('User-Agent'),
    });
    res.json({
      success: true,
      exercise: updatedExercise,
      message: 'Deadline updated successfully',
    });
  } catch (error) {
    logger.error('Failed to update exercise deadline', {
      error: error.message,
      exerciseId: id,
      deadline,
      ip: req.ip,
    });
    res.status(400).json({ error: error.message });
  }
}

async function updateDeadlines(req, res) {
  const { updates } = req.body || {};

  try {
    const exercises = await updateExerciseDeadlines(updates);
    logger.info('Admin updated exercise deadlines in bulk', {
      count: updates?.length || 0,
      ip: req.ip,
      userAgent: req.get('User-Agent'),
    });
    res.json({
      success: true,
      exercises,
    });
  } catch (error) {
    logger.error('Failed to update exercise deadlines in bulk', {
      error: error.message,
      updates,
      ip: req.ip,
    });
    res.status(400).json({ error: error.message });
  }
}

module.exports = {
  showAdminPage,
  showLoginPage,
  handleLogin,
  handleLogout,
  getAdminSession,
  showLogsPage,
  showExercisesPage,
  listLogs,
  getLogFile,
  streamLogSSE,
  getExercises,
  updateDeadline,
  updateDeadlines,
};

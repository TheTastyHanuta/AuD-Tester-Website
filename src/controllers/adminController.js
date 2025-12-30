const path = require('path');
const fs = require('fs-extra');
const { spawn } = require('child_process');
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
} = require('../services/exerciseService');

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
  const ok =
    typeof password === 'string' && password === config.LOG_VIEWER_PASSWORD;
  logger.info('Admin login attempt', {
    ip: req.ip,
    userAgent: req.get('User-Agent'),
    success: ok,
  });
  if (!ok) {
    return res.status(401).send('Unauthorized');
  }
  req.session.isLogAdmin = true;
  // Prefer body.return, fallback to query, ensure same-origin relative path
  let ret = (req.body && req.body.return) || req.query.return || '/admin/logs';
  if (typeof ret !== 'string' || ret.startsWith('http')) ret = '/admin/logs';
  return res.redirect(ret);
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

async function showLogsPage(req, res) {
  res.sendFile(path.join(__dirname, '../../private', 'admin-logs.html'));
}

async function showExercisesPage(req, res) {
  res.sendFile(path.join(__dirname, '../../private', 'admin-exercises.html'));
}

async function listLogs(req, res) {
  const type = (req.query.type || 'app').toString();
  try {
    const files = await listLogFiles(type);
    res.json({ type, files });
  } catch (e) {
    logger.error('Error listing log files', { error: e.message, type });
    res.status(500).json({ error: 'Could not list log files' });
  }
}

async function getLogFile(req, res) {
  const type = (req.query.type || 'app').toString();
  const name = (req.query.name || '').toString();
  const raw = req.query.raw === '1';
  if (!/^[A-Za-z0-9][A-Za-z0-9._-]*\.log(\.gz)?$/.test(name)) {
    return res.status(400).send('Invalid filename');
  }
  const dir = getLogDir(type);
  const full = path.join(dir, name);
  if (!full.startsWith(dir)) {
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
}

async function streamLogSSE(req, res) {
  const type = (req.query.type || 'app').toString();
  const dir = getLogDir(type);
  await fs.ensureDir(dir);
  let currentName = (await getLatestLogFile(type)) || '';
  if (!currentName) {
    return res.status(404).send('No log file yet');
  }

  res.writeHead(200, {
    'Content-Type': 'text/event-stream',
    'Cache-Control': 'no-cache',
    'X-Accel-Buffering': 'no',
    Connection: 'keep-alive',
  });

  function sseSend(line) {
    res.write(`data: ${line.replace(/\n/g, '\\n')}\n\n`);
  }

  // Helper to start tail on a file
  let tailProc = null;
  function startTail(filePath) {
    if (tailProc) {
      try {
        tailProc.kill();
      } catch (_) {}
    }
    const full = path.join(dir, filePath);
    // -n 300 to send last lines
    tailProc = spawn('tail', ['-n', '300', '-F', full]);
    tailProc.stdout.setEncoding('utf8');
    tailProc.stdout.on('data', chunk => {
      const lines = chunk.split(/\r?\n/);
      for (const l of lines) {
        if (l.length) sseSend(l);
      }
    });
    tailProc.stderr.on('data', err => {
      logger.warn('tail stderr', { err: String(err) });
    });
    tailProc.on('error', err => {
      logger.error('tail failed', { error: err.message });
      sseSend(`Tail error: ${err.message}`);
    });
  }

  startTail(currentName);

  try {
    const latest = await getLatestLogFile(type);
    if (latest && latest !== currentName) {
      currentName = latest;
      sseSend(`[switching to ${latest}]`);
      startTail(latest);
    }
  } catch (e) {
    logger.warn('Failed to check latest log file', { error: e.message });
  }

  req.on('close', () => {
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

module.exports = {
  showLoginPage,
  handleLogin,
  handleLogout,
  showLogsPage,
  showExercisesPage,
  listLogs,
  getLogFile,
  streamLogSSE,
  getExercises,
  updateDeadline,
};

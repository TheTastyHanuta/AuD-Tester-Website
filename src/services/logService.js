const path = require('path');
const fs = require('fs-extra');

const LOG_TYPES = new Set(['app', 'combined', 'error']);

function assertValidLogType(type) {
  if (!LOG_TYPES.has(type)) {
    throw new Error('Invalid log type');
  }
}

function getLogDir(type) {
  assertValidLogType(type);
  const base = path.join(__dirname, '../../logs');
  if (type === 'error') return path.join(base, 'error');
  if (type === 'app') return path.join(base, 'app');
  return path.join(base, 'combined');
}

async function listLogFiles(type) {
  const dir = getLogDir(type);
  await fs.ensureDir(dir);
  const entries = await fs.readdir(dir);
  const files = [];
  for (const name of entries) {
    const full = path.join(dir, name);
    const stat = await fs.stat(full).catch(() => null);
    // Only include rotated Winston log files (.log or .log.gz), skip audit .json and others
    if (stat && stat.isFile() && /\.log(\.gz)?$/.test(name)) {
      files.push({ name, size: stat.size, mtime: stat.mtimeMs });
    }
  }
  files.sort((a, b) => b.mtime - a.mtime);
  return files;
}

async function getLatestLogFile(type) {
  const files = await listLogFiles(type);
  // Prefer current (non-gz) file only for tailing
  const log = files.find(f => f.name.endsWith('.log'));
  return log ? log.name : null;
}

module.exports = {
  assertValidLogType,
  getLogDir,
  listLogFiles,
  getLatestLogFile,
};

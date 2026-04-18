const { spawn } = require('child_process');

const DEFAULT_MAX_OUTPUT_BYTES = 1024 * 1024;

function appendChunk(existing, chunk, maxBytes) {
  if (existing.length >= maxBytes) {
    return existing;
  }

  const next = `${existing}${chunk.toString()}`;
  if (next.length <= maxBytes) {
    return next;
  }

  return next.slice(0, maxBytes);
}

function spawnPromise(command, args = [], options = {}) {
  return new Promise(resolve => {
    const {
      timeout,
      maxOutputBytes = DEFAULT_MAX_OUTPUT_BYTES,
      ...spawnOptions
    } = options;
    let stdout = '';
    let stderr = '';
    let timedOut = false;
    let settled = false;

    const child = spawn(command, args, {
      ...spawnOptions,
      shell: false,
    });

    const timer = timeout
      ? setTimeout(() => {
          timedOut = true;
          child.kill('SIGKILL');
        }, timeout)
      : null;

    child.stdout?.on('data', chunk => {
      stdout = appendChunk(stdout, chunk, maxOutputBytes);
    });

    child.stderr?.on('data', chunk => {
      stderr = appendChunk(stderr, chunk, maxOutputBytes);
    });

    child.on('error', error => {
      if (settled) {
        return;
      }
      settled = true;
      if (timer) clearTimeout(timer);
      resolve({
        error,
        stdout,
        stderr,
        timedOut,
      });
    });

    child.on('close', (code, signal) => {
      if (settled) {
        return;
      }
      settled = true;
      if (timer) clearTimeout(timer);

      const error =
        code === 0
          ? null
          : Object.assign(
              new Error(
                timedOut
                  ? `${command} timed out`
                  : `${command} exited with code ${code}`
              ),
              { code, signal, timedOut }
            );

      resolve({
        error,
        stdout,
        stderr,
        timedOut,
      });
    });
  });
}

module.exports = {
  spawnPromise,
};

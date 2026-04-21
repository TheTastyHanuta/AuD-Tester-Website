const path = require('path');
const fs = require('fs-extra');
const logger = require('../../logger');
const config = require('../config/config');
const { createScopedLogger } = require('../utils/loggerHelper');
const {
  createDockerContainerName,
  getDockerResourceLogContext,
  getDockerResourceArgs,
  runDockerCommand,
} = require('./dockerService');
const { spawnPromise } = require('./processService');
const SubmissionStatus = require('../utils/submissionStatus');

function normalizeJavacOutput(output) {
  if (typeof output !== 'string') {
    return output;
  }

  return output.replace(/(^|\n)\.\/([^:\n]+\.java:)/g, '$1$2');
}

function compileJavaFiles(workingDir, fileNames, exerciseConfig, sessionId) {
  return new Promise(resolve => {
    const log = createScopedLogger({
      sessionId,
      exercise: exerciseConfig?.id,
      workingDir,
    });

    try {
      log.info('Starting compilation', { files: fileNames });

      // Copy required provided files based on exercise
      try {
        const providedDir = path.join(__dirname, '../../provided');
        const exerciseId = exerciseConfig?.id;

        if (exerciseId === 'paintcan') {
          // Copy Paint.java for PaintCan exercise
          const providedFile = path.join(providedDir, 'Paint.java');
          const destFile = path.join(workingDir, 'Paint.java');
          if (fs.existsSync(providedFile) && !fs.existsSync(destFile)) {
            fs.copyFileSync(providedFile, destFile);
            log.debug('Copied provided file for paintcan exercise', {
              providedFile: 'Paint.java',
            });
          }
        } else if (exerciseId === 'sierpinski') {
          // Copy SierpinskiTriangleAbstract.java for Sierpinski exercise
          const providedFile = path.join(
            providedDir,
            'SierpinskiTriangleAbstract.java'
          );
          const destFile = path.join(
            workingDir,
            'SierpinskiTriangleAbstract.java'
          );
          if (fs.existsSync(providedFile) && !fs.existsSync(destFile)) {
            fs.copyFileSync(providedFile, destFile);
            log.debug('Copied provided file for sierpinski exercise', {
              providedFile: 'SierpinskiTriangleAbstract.java',
            });
          }
        }
      } catch (providedError) {
        log.warn('Error handling provided files during compilation', {
          error: providedError.message,
        });
      }

      // Prepare compilation command, include all Java files in working directory
      const allJavaFiles = fs
        .readdirSync(workingDir)
        .filter(file => file.endsWith('.java'));
      const classpath = '.'; // Set classpath to current directory
      const containerName = createDockerContainerName('aud-tester-compile');
      log.debug('Resolved Docker compilation sandbox', {
        dockerImage: config.JAVA_COMPILER_DOCKER_IMAGE,
        containerName,
        timeoutMs: config.JAVA_COMPILE_TIMEOUT_MS,
        fileCount: allJavaFiles.length,
        resources: getDockerResourceLogContext(),
      });

      const dockerArgs = [
        'run',
        '--rm',
        '--name',
        containerName,
        ...getDockerResourceArgs(),
        '-v',
        `${path.resolve(workingDir)}:/user`,
        '-w',
        '/user',
        config.JAVA_COMPILER_DOCKER_IMAGE,
        'javac',
        '-source',
        '8',
        '-target',
        '8',
        '-Xlint:-options',
        '-proc:none',
        '-cp',
        classpath,
        ...allJavaFiles.map(name => `./${name}`),
      ];
      log.debug('Executing compilation command', {
        command: 'docker',
        args: dockerArgs,
        allJavaFiles,
      });

      // Execute compilation command
      runDockerCommand(dockerArgs, {
        cwd: workingDir,
        timeout: config.JAVA_COMPILE_TIMEOUT_MS,
        containerName,
        log,
      }).then(({ error, stdout, stderr, timedOut }) => {
        if (error) {
          const normalizedStderr = normalizeJavacOutput(stderr);

          if (timedOut || error.code === 125) {
            log.error('Docker compilation sandbox failed', {
              dockerImage: config.JAVA_COMPILER_DOCKER_IMAGE,
              containerName,
              timedOut,
              exitCode: error.code,
              stderr: stderr || null,
            });
            resolve({
              status: SubmissionStatus.SYSTEM_ERROR,
              details:
                'Die Kompilierung konnte nicht abgeschlossen werden. Bitte versuche es später erneut.',
            });
            return;
          }

          log.debug('Compilation failed', {
            error: normalizedStderr || error.message,
            stdout,
            files: allJavaFiles,
          });

          resolve({
            status: SubmissionStatus.COMPILATION_ERROR,
            details: normalizedStderr || error.message,
          });
        } else {
          const normalizedStdout = normalizeJavacOutput(stdout);

          log.debug('Compilation successful', {
            stdout: normalizedStdout,
            files: allJavaFiles,
          });
          resolve({
            status: SubmissionStatus.SUCCESS,
            details: normalizedStdout,
          });
        }
      });
    } catch (err) {
      log.error('Error setting up compilation', {
        error: err.message,
        stack: err.stack,
      });
      resolve({
        status: SubmissionStatus.SYSTEM_ERROR,
        details: `Error setting up compilation: ${err.message}`,
      });
    }
  });
}

async function checkJavaVersion() {
  const dockerVersion = await spawnPromise('docker', ['--version'], {
    timeout: 10000,
  });

  if (dockerVersion.error) {
    logger.warn('Docker not available for Java compilation', {
      error: dockerVersion.stderr || dockerVersion.error.message,
    });
    return;
  }

  logger.info('Docker version detected for Java compilation', {
    version: dockerVersion.stdout.trim(),
  });

  const imageCheck = await spawnPromise(
    'docker',
    ['image', 'inspect', config.JAVA_COMPILER_DOCKER_IMAGE],
    { timeout: 10000 }
  );

  if (imageCheck.error) {
    logger.warn('Java compiler Docker image not available locally', {
      dockerImage: config.JAVA_COMPILER_DOCKER_IMAGE,
      error: imageCheck.stderr || imageCheck.error.message,
    });
    return;
  }

  logger.info('Java compiler Docker image available', {
    dockerImage: config.JAVA_COMPILER_DOCKER_IMAGE,
  });
}

module.exports = {
  compileJavaFiles,
  checkJavaVersion,
  normalizeJavacOutput,
};

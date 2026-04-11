const { exec } = require('child_process');
const path = require('path');
const fs = require('fs-extra');
const logger = require('../../logger');
const { createScopedLogger } = require('../utils/loggerHelper');

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
      const command = `javac -source 8 -target 8 -Xlint:-options -cp ${classpath} ${allJavaFiles.map(name => `"${name}"`).join(' ')}`;
      log.debug('Executing compilation command', {
        command,
        allJavaFiles,
      });

      // Execute compilation command
      exec(command, { cwd: workingDir }, (error, stdout, stderr) => {
        if (error) {
          log.debug('Compilation failed', {
            error: stderr || error.message,
            stdout,
            files: allJavaFiles,
          });
          resolve({
            success: false,
            error: stderr || error.message,
          });
        } else {
          log.debug('Compilation successful', {
            stdout,
            files: allJavaFiles,
          });
          resolve({
            success: true,
            output: stdout,
          });
        }
      });
    } catch (err) {
      log.error('Error setting up compilation', {
        error: err.message,
        stack: err.stack,
      });
      resolve({
        success: false,
        error: `Error setting up compilation: ${err.message}`,
      });
    }
  });
}

async function checkJavaVersion() {
  return new Promise(resolve => {
    exec('java -version', (error, stdout, stderr) => {
      const output = stderr || stdout;
      const versionLine = output.split('\n')[0];
      logger.info('Java version detected', { version: versionLine });

      // Check if Java 8 is available
      exec('javac -version', (error, stdout, stderr) => {
        const javacOutput = stderr || stdout;
        logger.info('Java compiler version detected', {
          version: javacOutput.trim(),
        });
        resolve();
      });
    });
  });
}

module.exports = {
  compileJavaFiles,
  checkJavaVersion,
};

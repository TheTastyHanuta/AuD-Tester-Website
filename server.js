const express = require('express');
const multer = require('multer');
const path = require('path');
const fs = require('fs-extra');
const { exec } = require('child_process');
const cors = require('cors');
const dotenv = require('dotenv');
const morgan = require('morgan');
const session = require('express-session');
const logger = require('./logger');

dotenv.config();

const exercises = require('./exercises.json');
const { Logger } = require('winston');

const app = express();
const PORT = process.env.PORT || 3000;

app.set('trust proxy', 1);

// Session configuration
app.use(
  session({
    secret: process.env.SESSION_SECRET || 'secret-change-in-production',
    resave: false,
    saveUninitialized: true,
    cookie: {
      secure: process.env.NODE_ENV === 'production',
      httpOnly: true,
      maxAge: 24 * 60 * 60 * 1000,
      sameSite: 'lax',
    },
  })
);

app.use(cors());
app.use(express.json());
app.use(express.static('public'));

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

const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    const uploadDir = 'uploads';
    fs.ensureDirSync(uploadDir);
    cb(null, uploadDir);
  },
  filename: (req, file, cb) => {
    cb(null, file.originalname);
  },
});

const upload = multer({
  storage: storage,
  fileFilter: (req, file, cb) => {
    if (
      file.mimetype === 'text/x-java-source' ||
      file.originalname.endsWith('.java')
    ) {
      cb(null, true);
    } else {
      cb(new Error('Nur Java-Dateien sind erlaubt!'), false);
    }
  },
});

app.get('/', (req, res) => {
  logger.info('Main page accessed', {
    sessionId: req.session.id,
    userAgent: req.get('User-Agent'),
    ip: req.ip,
  });
  res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

app.post('/submit', (req, res) => {
  const uploadMiddleware = upload.array('javaFiles', 20);

  uploadMiddleware(req, res, async err => {
    if (err) {
      logger.error('File upload error', {
        sessionId: req.session?.id || 'no-session',
        error: err.message,
        stack: err.stack,
      });
      return res.status(400).json({
        error: 'File upload error',
        status: '❌',
        message: `Upload failed: ${err.message}`,
      });
    }

    try {
      if (!req.files || req.files.length === 0) {
        logger.warn('No Java files uploaded in submission', {
          sessionId: req.session?.id || 'no-session',
        });
        return res
          .status(400)
          .json({ error: 'Keine Java-Dateien hochgeladen' });
      }

      const { exercise } = req.body;
      const uploadedFiles = req.files;
      const sessionId = req.session.id;

      logger.info('Processing submission', {
        sessionId: sessionId,
        files: uploadedFiles.map(f => f.filename),
        exercise: exercise,
        fileCount: uploadedFiles.length,
      });

      const tempDir = path.join('temp', `${Date.now()}_${Math.random()}`);
      await fs.ensureDir(tempDir);

      try {
        for (const file of uploadedFiles) {
          const tempFilePath = path.join(tempDir, file.filename);
          await fs.copy(file.path, tempFilePath);
        }

        // Check for non-ASCII characters in all files
        const encodingCheck = await validateUSASCIIEncoding(
          tempDir,
          uploadedFiles.map(f => f.filename)
        );
        // If encoding check fails, return early
        if (!encodingCheck.valid) {
          logger.info('Encoding check failed', {
            sessionId: req.session.id,
            details: encodingCheck.details,
          });
          return res.json({
            success: false,
            status: '⚠️',
            message: 'Encoding Error: Non-ASCII characters detected',
            details: encodingCheck.details,
          });
        }
        // If no exercise selected, return early
        const exerciseConfig = getExerciseConfig(exercise);
        if (!exerciseConfig) {
          return res.json({
            success: false,
            status: '❌',
            message: 'Ungültige Übung ausgewählt',
            details: 'Die ausgewählte Übung wurde nicht gefunden.',
          });
        }

        // Validate required files
        const requiredFilesCheck = validateRequiredFiles(
          uploadedFiles.map(f => f.filename),
          exerciseConfig.required_files || []
        );
        // If required files are missing, return early
        if (!requiredFilesCheck.valid) {
          logger.info('Missing required files in submission', {
            sessionId: sessionId,
            exercise: exercise,
            missingFiles: requiredFilesCheck.details,
          });
          return res.json({
            success: false,
            status: '❌',
            message: 'Fehlende erforderliche Dateien',
            details: requiredFilesCheck.details,
            deadline: exerciseConfig.deadline,
            deadlinePassed: new Date() > new Date(exerciseConfig.deadline),
          });
        }

        // Main testing will be handled below

        /* If exercise has tests run the correct docker image for the exercise
        This will return a status if the submission passed the tests or not.
        */
        if (exerciseConfig.hasTests) {
          // Run Docker tests for exercises with test configuration
          const dockerTestResult = await runDockerTests(
            tempDir,
            exercise,
            exerciseConfig,
            sessionId
          );

          logger.info('Docker test execution completed', {
            sessionId: sessionId,
            exercise: exercise,
            dockerImage: dockerTestResult.dockerImage,
            success: dockerTestResult.success,
            status: dockerTestResult.status,
            points: dockerTestResult.points,
          });

          // If deadline is passed or submission had no success, return with detailed feedback
          if (
            ((dockerTestResult.success === false ||
              new Date() > new Date(exerciseConfig.deadline)) &&
              process.env.showSecretTests === 'true') ||
            process.env.FORCE_SHOW_SECRET_TESTS === 'true'
          ) {
            return res.json({
              success: dockerTestResult.success,
              status: dockerTestResult.status,
              message: dockerTestResult.message,
              details: dockerTestResult.details,
              deadline: exerciseConfig.deadline,
              deadlinePassed: new Date() > new Date(exerciseConfig.deadline),
            });
          } else {
            // If deadline is not passed and submission was successful, return without detailed feedback
            return res.json({
              success: dockerTestResult.success,
              status: dockerTestResult.status,
              message: dockerTestResult.message,
              deadline: exerciseConfig.deadline,
              deadlinePassed: new Date() > new Date(exerciseConfig.deadline),
            });
          }
        } else {
          // If no tests, just try to compile the code
          /* Compile submission Java files
          This will fail if the code does use non Java 8 features and has syntax errors.
          */
          const compilationResult = await compileJavaFiles(
            tempDir,
            uploadedFiles.map(f => f.filename),
            exerciseConfig,
            sessionId
          );
          // If submission compilation fails return early
          if (!compilationResult.success) {
            logger.info('Compilation failed', {
              sessionId: sessionId,
              exercise: exercise,
              error: compilationResult.error,
            });
            return res.json({
              success: false,
              status: '💀',
              message:
                'Compile Error. Bitte überprüfe deinen Code. Bei einer Abgabe über StudOn wird dies 0 Punkte ergeben. (Ausnahme sind die ersten zwei Übungen.)',
              details: compilationResult.error,
            });
          } else {
            // If compilation is successful, return success message
            logger.info('Compilation successful without tests', {
              sessionId: sessionId,
              exercise: exercise,
            });
            return res.json({
              success: true,
              status: '✅',
              message: 'Erfolgreich kompiliert',
              details:
                'Dein Code wurde erfolgreich kompiliert. Du kannst ihn so abgeben!',
              deadline: exerciseConfig.deadline,
              deadlinePassed: new Date() > new Date(exerciseConfig.deadline),
            });
          }
        }
      } catch (processError) {
        // If any error occurs during processing, log it and return a 500 error
        logger.error('Error during submission processing', {
          sessionId: sessionId,
          error: processError.message,
          stack: processError.stack,
          exercise: exercise,
          tempDir: tempDir,
        });
        if (!res.headersSent) {
          return res.status(500).json({
            error: 'Processing error',
            status: '⚠️',
            message: 'An error occurred while processing your submission',
          });
        }
      } finally {
        try {
          await cleanupTempDir(tempDir);
        } catch (cleanupError) {
          logger.warn('Could not clean up temp directory', {
            tempDir: tempDir,
            error: cleanupError.message,
          });
        }
      }
    } catch (error) {
      // Catch any unexpected errors
      logger.error('Error processing submission', {
        sessionId: req.session?.id || 'no-session',
        error: error.message,
        stack: error.stack,
        exercise: req.body?.exercise,
      });
      if (!res.headersSent) {
        res.status(500).json({
          error: 'Internal server error',
          status: '⚠️',
          message:
            'An internal error occurred while processing your submission',
        });
      }
    } finally {
      if (req.files) {
        for (const file of req.files) {
          await fs.remove(file.path).catch(err =>
            logger.error('Error removing uploaded file', {
              file: file.path,
              error: err.message,
            })
          );
        }
      }
    }
  });
});

function compileJavaFiles(workingDir, fileNames, exerciseConfig, sessionId) {
  return new Promise(resolve => {
    try {
      logger.info('Starting compilation', {
        sessionId: sessionId,
        files: fileNames,
        workingDir: workingDir,
        exercise: exerciseConfig?.id,
      });

      // Copy required provided files based on exercise
      try {
        const providedDir = path.join(__dirname, 'provided');
        const exerciseId = exerciseConfig?.id;

        if (exerciseId === 'paintcan') {
          // Copy Paint.java for PaintCan exercise
          const providedFile = path.join(providedDir, 'Paint.java');
          const destFile = path.join(workingDir, 'Paint.java');
          if (fs.existsSync(providedFile) && !fs.existsSync(destFile)) {
            fs.copyFileSync(providedFile, destFile);
            logger.debug('Copied provided file for paintcan exercise', {
              sessionId: sessionId,
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
            logger.debug('Copied provided file for sierpinski exercise', {
              sessionId: sessionId,
              providedFile: 'SierpinskiTriangleAbstract.java',
            });
          }
        }
      } catch (providedError) {
        logger.warn('Error handling provided files during compilation', {
          sessionId: sessionId,
          exerciseId: exerciseConfig?.id,
          error: providedError.message,
        });
      }

      // Prepare compilation command - include all Java files in working directory
      const allJavaFiles = fs
        .readdirSync(workingDir)
        .filter(file => file.endsWith('.java'));
      const classpath = '.'; // Set classpath to current directory
      const command = `javac -source 8 -target 8 -Xlint:-options -cp ${classpath} ${allJavaFiles.map(name => `"${name}"`).join(' ')}`;
      logger.debug('Executing compilation command', {
        sessionId: sessionId,
        command: command,
        workingDir: workingDir,
        allJavaFiles: allJavaFiles,
      });

      // Execute compilation command
      exec(command, { cwd: workingDir }, (error, stdout, stderr) => {
        if (error) {
          logger.debug('Compilation failed', {
            sessionId: sessionId,
            error: error.message,
            stderr: stderr,
            stdout: stdout,
            files: allJavaFiles,
            workingDir: workingDir,
          });
          resolve({
            success: false,
            error: stderr || error.message,
          });
        } else {
          logger.debug('Compilation successful', {
            sessionId: sessionId,
            files: allJavaFiles,
            workingDir: workingDir,
          });
          resolve({
            success: true,
            output: stdout,
          });
        }
      });
    } catch (err) {
      logger.error('Error setting up compilation', {
        sessionId: sessionId,
        error: err.message,
        stack: err.stack,
        workingDir: workingDir,
        files: allJavaFiles,
      });
      resolve({
        success: false,
        error: `Error setting up compilation: ${err.message}`,
      });
    }
  });
}

function execPromise(command, options) {
  return new Promise(resolve => {
    exec(command, options, (error, stdout, stderr) => {
      resolve({
        error: error,
        stdout: stdout || '',
        stderr: stderr || '',
      });
    });
  });
}

async function runDockerTests(workingDir, exercise, exerciseConfig, sessionId) {
  try {
    logger.info('Starting Docker tests', {
      sessionId: sessionId,
      exercise: exercise,
      workingDir: workingDir,
    });

    // Docker image mapping
    const testDockerImageMapping = {
      arrays: 'aufgabe2-arrays',
      caesarchiffre: 'aufgabe3-caesarchiffre',
      signalplotter: 'aufgabe3-signalplotter',
      color: 'aufgabe4-color',
      snakegame: 'aufgabe5-snake',
      sortedset: 'aufgabe7-sortedset',
      contactdb: 'aufgabe8-contactdatabase',
      binarytree: 'aufgabe9-binarysearchtree',
    };

    const dockerImage = testDockerImageMapping[exercise];
    if (!dockerImage) {
      logger.warn('No Docker image configured for exercise', {
        sessionId: sessionId,
        exercise: exercise,
      });
      return {
        success: false,
        status: '⚠️',
        message: 'Test configuration error',
        details: 'Es wurde kein Test für diese Übung konfiguriert.',
      };
    }

    // Create result directory for Docker output
    const resultDir = path.join(workingDir, 'result');
    await fs.ensureDir(resultDir);

    // Docker command similar to the Python script
    const dockerCommand = [
      'docker',
      'run',
      '--rm',
      '-v',
      `${path.resolve(workingDir)}:/user`,
      '-v',
      `${path.resolve(resultDir)}:/result`,
      dockerImage,
    ];

    logger.debug('Executing Docker command', {
      sessionId: sessionId,
      command: dockerCommand.join(' '),
      dockerImage: dockerImage,
      workingDir: workingDir,
      resultDir: resultDir,
    });

    // Execute Docker command with timeout
    const dockerResult = await execPromise(dockerCommand.join(' '), {
      cwd: workingDir,
      timeout: 120000, // 2 minute timeout for Docker tests
    });

    // If the execPromise failed to return a result, handle it
    if (!dockerResult) {
      logger.error('Docker execution failed to return a result', {
        sessionId: sessionId,
        dockerImage: dockerImage,
      });
      return {
        success: false,
        status: '💀',
        message: 'Überprüfung fehlgeschlagen',
        details:
          'Es ist ein Fehler bei der Ausführung aufgetreten. Bitte versuche es später erneut und überprüfe Deinen Code.',
      };
    }

    logger.debug('Docker execution completed', {
      sessionId: sessionId,
      dockerImage: dockerImage,
      returnCode: dockerResult.error?.code || 0,
      stdout: dockerResult.stdout,
      stderr: dockerResult.stderr,
    });

    // Check if Docker execution failed
    if (dockerResult.error && dockerResult.error.code !== 0) {
      logger.error('Docker execution failed', {
        sessionId: sessionId,
        dockerImage: dockerImage,
        error: dockerResult.error.message,
        stdout: dockerResult.stdout,
        stderr: dockerResult.stderr,
      });

      // If Docker crashes, return compile error similar to Python script
      const comment =
        'Compile error\naudoscore crash (probably due to file misspelling or wrong encoding)';
      return {
        success: false,
        status: '💀',
        message: 'Ausführung fehlgeschlagen. Bitte überprüfe Deinen Code.',
        details: comment,
      };
    }

    // Try to read results.json from result directory
    const resultsJsonPath = path.join(resultDir, 'results.json');

    if (!(await fs.pathExists(resultsJsonPath))) {
      logger.warn('Results file not found after Docker execution', {
        sessionId: sessionId,
        dockerImage: dockerImage,
        resultsPath: resultsJsonPath,
      });
      return {
        success: false,
        status: '⚠️',
        message:
          'Es gab ein Problem bei der Überprüfung. Details konnten nicht gelesen werden.',
        details:
          'Tests wurden ausgeführt, aber keine Ergebnisse gefunden. Bitte überprüfe Deinen Code.',
      };
    }

    // Parse results.json
    const resultsData = await fs.readJson(resultsJsonPath);

    logger.debug('Docker test results parsed', {
      sessionId: sessionId,
      dockerImage: dockerImage,
      instant_status: resultsData.instant_status,
      points: resultsData.points,
      success: resultsData.success,
      feedback: resultsData.protected_feedback_text,
    });

    // Format feedback similar to Python script
    const points = resultsData.points || '0';
    let instantStatus = resultsData.instant_status || '💀';
    let feedbackText = resultsData.protected_feedback_text || '';

    // Handle compile errors specifically
    if (feedbackText.includes('Compile error')) {
      const instantMessage = resultsData.instant_message || '';
      feedbackText = `Compile error\n\nReason from auto-feedback:\n\n${instantMessage}`;
    }

    // Clean feedback text (remove commas to keep CSV structure intact if needed)
    const cleanFeedbackText = feedbackText.replace(/,/g, '');

    // Determine success based on status
    let isSuccess = instantStatus === '✔' || instantStatus.includes('✔');

    if (instantStatus === '✔') instantStatus = '✅';

    return {
      success: isSuccess,
      status: instantStatus,
      message: isSuccess
        ? 'Alles supi. Du kannst die Dateien so auf StudOn hochladen. Genaueres Feedback wird angezeigt, wenn die Deadline vorbei ist.'
        : 'Das hat nicht geklappt. Bitte schau dir das Feedback an. Wenn Du den Code so abgibst, wird es wahrscheinlich 0 Punkte geben.',
      details: cleanFeedbackText,
      points: points,
      dockerImage: dockerImage,
    };
  } catch (error) {
    logger.error('Error running Docker tests', {
      sessionId: sessionId,
      error: error.message,
      stack: error.stack,
      exercise: exercise,
      workingDir: workingDir,
    });

    return {
      success: false,
      status: '⚠️',
      message: 'Es gab ein Problem bei der Überprüfung',
      details: `Es ist ein Fehler aufgetreten. Wenn das Problem weiterhin besteht, melde Dich bitte im Forum: ${error.message}`,
    };
  }
}

async function cleanupTempDir(tempDir) {
  try {
    await fs.remove(tempDir);
    logger.debug('Cleaned up temp directory', { tempDir: tempDir });
  } catch (error) {
    if (error.code === 'EBUSY' || error.code === 'ENOTEMPTY') {
      logger.debug('Files locked, attempting cleanup with delay', {
        tempDir: tempDir,
      });

      await new Promise(resolve => setTimeout(resolve, 1000));

      try {
        await fs.remove(tempDir);
        logger.debug('Cleanup successful after delay', { tempDir: tempDir });
      } catch (retryError) {
        logger.warn('Could not clean up temp directory after retry', {
          tempDir: tempDir,
          error: retryError.message,
        });
        setTimeout(async () => {
          try {
            await fs.remove(tempDir);
            console.log('Delayed cleanup successful');
          } catch (delayedError) {
            console.warn('Delayed cleanup failed:', delayedError.message);
          }
        }, 5000);
      }
    } else {
      throw error;
    }
  }
}

async function validateUSASCIIEncoding(workingDir, fileNames) {
  const nonASCIIFiles = [];
  const problematicChars = [];

  try {
    for (const fileName of fileNames) {
      const filePath = path.join(workingDir, fileName);
      const content = await fs.readFile(filePath, 'utf8');

      // Check each character in the file
      let lineNumber = 1;
      let columnNumber = 1;
      const fileProblems = [];

      for (let i = 0; i < content.length; i++) {
        const char = content[i];
        const charCode = char.charCodeAt(0);

        // US-ASCII is 0-127, anything above is non-ASCII
        if (charCode > 127) {
          fileProblems.push({
            char: char,
            charCode: charCode,
            line: lineNumber,
            column: columnNumber,
            hexCode: '0x' + charCode.toString(16).toUpperCase(),
          });
        }

        // Track line and column numbers
        if (char === '\n') {
          lineNumber++;
          columnNumber = 1;
        } else {
          columnNumber++;
        }
      }

      if (fileProblems.length > 0) {
        nonASCIIFiles.push({
          fileName: fileName,
          problems: fileProblems,
        });
      }
    }

    if (nonASCIIFiles.length === 0) {
      return { valid: true };
    }

    // Build detailed error message
    let details =
      'Deine Java-Dateien enthalten nicht-ASCII-Zeichen. Bitte verwende nur US-ASCII-Codierung.\n\n';
    details +=
      'Du musst die Projekt- und Dateicodierung auf US-ASCII einstellen. Andernfalls wird der Code nicht kompiliert.\n';
    details += 'Schau dir unsere Anweisungen zu Beginn des Semesters an.\n\n';
    details += 'Häufige nicht-ASCII-Zeichen, die vermieden werden sollten:\n';
    details += '• Deutsche Umlaute: ä, ö, ü, Ä, Ö, Ü, ß\n';
    details += '• Akzentuierte Zeichen: é, è, à, ç, etc.\n';
    details += '• Besondere Anführungszeichen: " " \' \'\n';
    details += '• Em/en-Dash: — –\n\n';
    details += 'Gefundene Probleme:\n\n';

    for (const file of nonASCIIFiles) {
      details += `File: ${file.fileName}\n`;

      // Group similar characters to avoid spam
      const charCounts = {};
      for (const problem of file.problems) {
        const key = `${problem.char} (${problem.hexCode})`;
        if (!charCounts[key]) {
          charCounts[key] = [];
        }
        charCounts[key].push(`Line ${problem.line}, Column ${problem.column}`);
      }

      for (const [charInfo, locations] of Object.entries(charCounts)) {
        details += `  • Character ${charInfo} found at: ${locations.slice(0, 5).join(', ')}`;
        if (locations.length > 5) {
          details += ` and ${locations.length - 5} more locations`;
        }
        details += '\n';
      }
      details += '\n';
    }

    details += 'Lösungen:\n';
    details += '• Ersetze ä, ö, ü durch ae, oe, ue\n';
    details += '• Ersetze ß durch ss\n';
    details += '• Verwende normale Anführungszeichen: " statt " "\n';
    details += "• Verwende normale Apostrophe: \\' statt \\'\n";
    details += '• Verwende normale Bindestriche: - statt — oder –\n';
    details +=
      '• Vermeide alle Zeichen mit Akzenten oder Sonderzeichen im Code\n';

    return {
      valid: false,
      details: details,
    };
  } catch (error) {
    console.error('Error validating encoding:', error);
    return {
      valid: false,
      details: `Error checking file encoding: ${error.message}`,
    };
  }
}

function validateRequiredFiles(uploadedFileNames, requiredFiles) {
  if (!requiredFiles || requiredFiles.length === 0) {
    return { valid: true };
  }

  const missingFiles = [];
  const uploadedNames = uploadedFileNames.map(name => name.toLowerCase());

  for (const requiredFile of requiredFiles) {
    const requiredLower = requiredFile.toLowerCase();
    if (!uploadedNames.includes(requiredLower)) {
      missingFiles.push(requiredFile);
    }
  }

  if (missingFiles.length === 0) {
    return { valid: true };
  }

  let details = `Diese Übung erfordert das Hochladen spezifischer Dateien. Dir fehlen:\n\n`;
  missingFiles.forEach(file => {
    details += `• ${file}\n`;
  });

  details += `\nBenötigte Dateien für diese Übung:\n`;
  requiredFiles.forEach(file => {
    const uploaded = uploadedNames.includes(file.toLowerCase());
    details += `${uploaded ? '✅' : '❌'} ${file}\n`;
  });

  details += `\nHochgeladene Dateien:\n`;
  uploadedFileNames.forEach(file => {
    details += `• ${file}\n`;
  });

  details += `\nBitte stelle sicher, dass du alle erforderlichen Dateien mit den genau angegebenen Dateinamen hochlädst.`;

  return {
    valid: false,
    details: details,
  };
}

function getExerciseConfig(exerciseId) {
  return exercises.find(ex => ex.id === exerciseId);
}

async function checkJavaVersion() {
  return new Promise(resolve => {
    exec('java -version', (error, stdout, stderr) => {
      const output = stderr || stdout;
      const versionLine = output.split('\n')[0];
      console.log('Java version:', versionLine);

      // Check if Java 8 is available
      exec('javac -version', (error, stdout, stderr) => {
        const javacOutput = stderr || stdout;
        console.log('Java compiler version:', javacOutput.trim());
        resolve();
      });
    });
  });
}

app.get('/exercises', (req, res) => {
  res.json(exercises);
});

// Check Java version and start server
(async () => {
  await checkJavaVersion();
  app.listen(PORT, () => {
    logger.info('AuD Tester Website started', {
      port: PORT,
      environment: process.env.NODE_ENV || 'development',
      javaVersion: 'Java 8 (enforced with -source 8 -target 8)',
      logLevel: logger.level,
    });
    console.log(`AuD Tester Website running on http://localhost:${PORT}`);
  });
})();

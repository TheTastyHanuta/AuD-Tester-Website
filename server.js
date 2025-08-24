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
          Logger.info('Encoding check failed', {
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

        if (exerciseConfig.hasTests) {
          const testDirMapping = {
            caesarchiffre: 'CaesarChiffreTests',
            signalplotter: 'SignalPlotterTests',
            color: 'ColorTests',
            snakegame: 'SnakeTests',
            sortedset: 'SortedSetTests',
            contactdb: 'ContactDatabaseTests',
            binarytree: 'BinarySearchTreeTests',
          };

          const testDirName = testDirMapping[exercise];
          if (testDirName) {
            const testDir = path.join('tests', testDirName);
            const testerDir = path.join('tests', 'tester');

            if (await fs.pathExists(testDir)) {
              await fs.copy(testDir, tempDir);
            }

            if (await fs.pathExists(testerDir)) {
              await fs.copy(testerDir, path.join(tempDir, 'tester'));
            }
          }
        }

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
          return res.json({
            success: false,
            status: '💀',
            message: 'Compilation failed',
            details: compilationResult.error,
          });
        }

        /* If exercise has tests, try to compile and run public tests
        These will fail if methods are not named correctly or if the code does not follow the expected structure.
        */
        if (exerciseConfig.hasTests) {
          const testResult = await runPublicTests(
            tempDir,
            exercise,
            exerciseConfig,
            sessionId
          );
          // If public tests compilation fails, return early
          if (!testResult.success) {
            return res.json({
              success: false,
              status: testResult.status,
              message: testResult.message,
              details: testResult.details,
              deadline: exerciseConfig.deadline,
            });
          }

          // Check if deadline has passed and is enabled in config --> run secret tests if available
          const deadlinePassed = new Date() > new Date(exerciseConfig.deadline);
          let secretTestResult = null;

          if (deadlinePassed && process.env.SHOW_SECRET_TESTS === 'true') {
            secretTestResult = await runSecretTests(
              tempDir,
              exercise,
              exerciseConfig,
              sessionId
            );
          }

          // Prepare response with public test results and secret test results if deadline passed
          const response = {
            success: true,
            status: testResult.status,
            message: testResult.message,
            details: testResult.details,
            deadline: exerciseConfig.deadline,
            deadlinePassed: deadlinePassed,
          };

          // Add secret test results if deadline has passed
          if (
            deadlinePassed &&
            secretTestResult &&
            process.env.SHOW_SECRET_TESTS === 'true'
          ) {
            response.secretTests = {
              success: secretTestResult.success,
              status: secretTestResult.status,
              message: secretTestResult.message,
              details: secretTestResult.details,
            };
          }

          return res.json(response);
        }

        // If no tests at all, just return compilation success
        res.json({
          success: true,
          status: '✅',
          message: 'Compilation successful',
          details:
            'Dein Code wurde erfolgreich kompiliert. Du kannst ihn so abgeben!',
          deadline: exerciseConfig.deadline,
          deadlinePassed: new Date() > new Date(exerciseConfig.deadline),
        });
      } catch (processError) {
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

      let classpath = '.';
      const testsDir = path.join(__dirname, 'tests');

      try {
        if (fs.existsSync(testsDir)) {
          const jarFiles = fs
            .readdirSync(testsDir)
            .filter(file => file.endsWith('.jar'));

          if (jarFiles.length > 0) {
            for (const jarFile of jarFiles) {
              const srcPath = path.join(testsDir, jarFile);
              const destPath = path.join(workingDir, jarFile);
              if (!fs.existsSync(destPath)) {
                try {
                  fs.linkSync(srcPath, destPath);
                } catch (linkError) {
                  fs.copyFileSync(srcPath, destPath);
                }
              }
            }

            const quotedJarFiles = jarFiles.map(file => `"${file}"`);
            classpath = `.${path.delimiter}${quotedJarFiles.join(path.delimiter)}`;
            logger.debug('Using classpath with JARs', {
              sessionId: sessionId,
              classpath: classpath,
              jarFiles: jarFiles,
            });
          }
        }
      } catch (jarError) {
        logger.warn('Error handling JAR files during compilation', {
          sessionId: sessionId,
          error: jarError.message,
        });
      }

      // Prepare compilation command
      const command = `javac -source 8 -target 8 -Xlint:-options -cp ${classpath} ${fileNames.map(name => `"${name}"`).join(' ')}`;
      logger.debug('Executing compilation command', {
        sessionId: sessionId,
        command: command,
        workingDir: workingDir,
      });

      // Execute compilation command
      exec(command, { cwd: workingDir }, (error, stdout, stderr) => {
        if (error) {
          logger.info('Compilation failed', {
            sessionId: sessionId,
            error: error.message,
            stderr: stderr,
            stdout: stdout,
            files: fileNames,
          });
          resolve({
            success: false,
            error: stderr || error.message,
          });
        } else {
          logger.info('Compilation successful', {
            sessionId: sessionId,
            files: fileNames,
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
        files: fileNames,
      });
      resolve({
        success: false,
        error: `Error setting up compilation: ${err.message}`,
      });
    }
  });
}

async function runPublicTests(workingDir, exercise, exerciseConfig, sessionId) {
  try {
    logger.info('Starting public tests', {
      sessionId: sessionId,
      exercise: exercise,
      workingDir: workingDir,
    });

    // Build classpath with JAR files
    let classpath = '.';
    const jarFiles = [];

    try {
      const files = await fs.readdir(workingDir);
      const jars = files.filter(file => file.endsWith('.jar'));
      if (jars.length > 0) {
        jarFiles.push(...jars.map(jar => `"${jar}"`));
        classpath = `.${path.delimiter}${jarFiles.join(path.delimiter)}`;
      }
    } catch (jarError) {
      logger.warn('Error reading JAR files for tests', {
        sessionId: sessionId,
        error: jarError.message,
        workingDir: workingDir,
      });
    }

    // Determine public test class name
    const testClassMapping = {
      caesarchiffre: 'CaesarChiffrePublicTest',
      signalplotter: 'SignalPlotterPublicTest',
      color: 'ColorPublicTest',
      snakegame: 'SnakePublicTest',
      sortedset: 'SortedSetPublicTest',
      contactdb: 'ContactDatabasePublicTest',
      binarytree: 'BinarySearchTreePublicTest',
    };

    // If no public test class name is found, return early
    const testClassName = testClassMapping[exercise];
    if (!testClassName) {
      logger.warn('No public test class mapping found', {
        sessionId: sessionId,
        exercise: exercise,
        workingDir: workingDir,
      });
      return {
        success: true,
        status: '✅',
        message: 'Compilation successful',
        details:
          'Dein Code wurde erfolgreich kompiliert. Du kannst ihn so abgeben!',
      };
    }

    // Check if test file exists otherwise return early
    const testFilePath = path.join(workingDir, `${testClassName}.java`);
    if (!(await fs.pathExists(testFilePath))) {
      logger.warn('Public Test file not found', {
        sessionId: sessionId,
        testClass: testClassName,
        workingDir: workingDir,
        exercise: exercise,
      });
      return {
        success: true,
        status: '✅',
        message: 'Compilation successful',
        details:
          'Dein Code wurde erfolgreich kompiliert. Du kannst ihn so abgeben!',
      };
    }

    // Prepare public test compilation command
    const compileTestCommand = `javac -source 8 -target 8 -Xlint:-options -cp ${classpath} "${testClassName}.java"`;
    logger.debug('Compiling public test files', {
      sessionId: sessionId,
      command: compileTestCommand,
      testClass: testClassName,
    });

    // Execute public test compilation command
    const testCompilationResult = await execPromise(compileTestCommand, {
      cwd: workingDir,
    });

    // If public test compilation fails, return early
    if (testCompilationResult.error) {
      logger.info('Public test compilation failed', {
        sessionId: sessionId,
        testClass: testClassName,
        error: testCompilationResult.stderr,
        stdout: testCompilationResult.stdout,
        exercise: exercise,
      });
      return {
        success: false,
        status: '💀',
        message: 'Test compilation failed',
        details: `Dein Code wurde erfolgreich kompiliert, aber die Tests konnten nicht kompiliert werden. Dies bedeutet normalerweise:\n- Fehlende erforderliche Methoden\n- Falsche Methodensignaturen\n- Falsche Klassen-/Methodennamen\n\nTestkompilierungsfehler:\n${testCompilationResult.stderr}`,
      };
    }

    // Run the public tests
    const runTestCommand = `java -cp ${classpath} org.junit.runner.JUnitCore ${testClassName}`;
    logger.debug('Running public tests', {
      sessionId: sessionId,
      command: runTestCommand,
      testClass: testClassName,
    });

    const testResult = await execPromise(runTestCommand, {
      cwd: workingDir,
      timeout: 30000, // 30 second timeout
    });

    if (testResult.error && testResult.error.code !== 'timeout') {
      // Tests ran but some failed
      logger.info('Public tests failed', {
        sessionId: sessionId,
        testClass: testClassName,
        error: testResult.stderr,
        stdout: testResult.stdout,
        exercise: exercise,
      });
      return {
        success: false,
        status: '💀',
        message: 'Tests failed',
        details: `Test output:\n${testResult.stdout}\n\nErrors:\n${testResult.stderr}`,
      };
    }

    // Public test passed
    logger.info('Public tests passed', {
      sessionId: sessionId,
      testClass: testClassName,
      exercise: exercise,
      workingDir: workingDir,
    });

    return {
      success: true,
      status: '✅',
      message: 'All tests passed!',
      details:
        'Dein Code wurde erfolgreich kompiliert und hat alle Tests bestanden! Du kannst ihn so abgeben!',
    };
  } catch (error) {
    logger.error('Error running public tests', {
      sessionId: sessionId,
      error: error.message,
      stack: error.stack,
      exercise: exercise,
      workingDir: workingDir,
    });
    return {
      success: false,
      status: '⚠️',
      message: 'Test execution error',
      details: `An error occurred while running tests: ${error.message}`,
    };
  }
}

async function runSecretTests(workingDir, exercise, exerciseConfig, sessionId) {
  try {
    logger.info('Starting secret tests', {
      sessionId: sessionId,
      exercise: exercise,
      workingDir: workingDir,
    });

    // Build classpath with JAR files
    let classpath = '.';
    const jarFiles = [];

    try {
      const files = await fs.readdir(workingDir);
      const jars = files.filter(file => file.endsWith('.jar'));
      if (jars.length > 0) {
        jarFiles.push(...jars.map(jar => `"${jar}"`));
        classpath = `.${path.delimiter}${jarFiles.join(path.delimiter)}`;
      }
    } catch (jarError) {
      logger.warn('Error reading JAR files for secret tests', {
        error: jarError.message,
        workingDir: workingDir,
      });
    }

    // Determine secret test class name
    const secretTestClassMapping = {
      caesarchiffre: 'CaesarChiffreSecretTest',
      signalplotter: 'SignalPlotterSecretTest',
      color: 'ColorSecretTest',
      snakegame: 'SnakeSecretTest',
      sortedset: 'SortedSetSecretTest',
      contactdb: 'ContactDatabaseSecretTest',
      binarytree: 'BinarySearchTreeSecretTest',
    };

    const secretTestClassName = secretTestClassMapping[exercise];
    if (!secretTestClassName) {
      return {
        success: false,
        status: '⚠️',
        message: 'No additional tests available',
        details: 'No additional tests are configured for this exercise.',
      };
    }

    // Check if secret test file exists
    const secretTestFilePath = path.join(
      workingDir,
      `${secretTestClassName}.java`
    );
    if (!(await fs.pathExists(secretTestFilePath))) {
      return {
        success: false,
        status: '⚠️',
        message: 'Additional test file not found',
        details: `Additional test file ${secretTestClassName}.java not found.`,
      };
    }

    // Compile secret test files
    const compileSecretTestCommand = `javac -source 8 -target 8 -Xlint:-options -cp ${classpath} "${secretTestClassName}.java"`;
    logger.debug('Compiling secret test files', {
      command: compileSecretTestCommand,
      testClass: secretTestClassName,
      exercise: exercise,
    });

    const secretTestCompilationResult = await execPromise(
      compileSecretTestCommand,
      { cwd: workingDir }
    );

    // If secret test compilation fails, return early
    if (secretTestCompilationResult.error) {
      logger.info('Secret test compilation failed', {
        sessionId: sessionId,
        testClass: secretTestClassName,
        error: secretTestCompilationResult.stderr,
        stdout: secretTestCompilationResult.stdout,
        exercise: exercise,
      });
      return {
        success: false,
        status: '💀',
        message: 'Zusätzliche Testkompilierung fehlgeschlagen.',
        details: `Dies bedeutet normalerweise:\n- Fehlende erforderliche Methoden\n- Falsche Methodensignaturen\n- Falsche Klassen-/Methodennamen\n\nZusätzliche Testkompilierungsfehler:\n${secretTestCompilationResult.stderr}`,
      };
    }

    // Run the secret tests
    const runSecretTestCommand = `java -cp ${classpath} org.junit.runner.JUnitCore ${secretTestClassName}`;
    logger.debug('Running secret tests', {
      sessionId: sessionId,
      command: runSecretTestCommand,
      testClass: secretTestClassName,
      exercise: exercise,
    });

    const secretTestResult = await execPromise(runSecretTestCommand, {
      cwd: workingDir,
      timeout: 30000, // 30 second timeout
    });

    if (secretTestResult.error && secretTestResult.error.code !== 'timeout') {
      // Secret tests ran but some failed
      logger.info('Secret tests failed', {
        sessionId: sessionId,
        testClass: secretTestClassName,
        error: secretTestResult.stderr,
        stdout: secretTestResult.stdout,
        exercise: exercise,
      });
      return {
        success: false,
        status: '💀',
        message: 'Zusätzliche Tests sind fehlgeschlagen.',
        details: `Du kannst die Dateien so abgeben, aber sie beinhalten noch Fehler.\nZusätzliche Testausgabe:\n${secretTestResult.stdout}\n\nFehler:\n${secretTestResult.stderr}`,
      };
    }

    // Secret tests passed
    logger.info('Secret tests passed', {
      sessionId: sessionId,
      testClass: secretTestClassName,
      exercise: exercise,
      workingDir: workingDir,
    });

    return {
      success: true,
      status: '✅',
      message: 'Alle zusätzlichen Tests bestanden!',
      details:
        'Dein Code hat alle zusätzlichen Tests erfolgreich bestanden! Das sollten viele Punkte werden!',
    };
  } catch (error) {
    logger.error('Error running secret tests', {
      sessionId: sessionId,
      error: error.message,
      stack: error.stack,
      exercise: exercise,
      workingDir: workingDir,
    });
    return {
      success: false,
      status: '⚠️',
      message: 'Additional test execution error',
      details: `An error occurred while running additional tests: ${error.message}`,
    };
  }
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

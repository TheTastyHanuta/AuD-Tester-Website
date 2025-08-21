const express = require('express');
const multer = require('multer');
const path = require('path');
const fs = require('fs-extra');
const { exec } = require('child_process');
const cors = require('cors');
const dotenv = require('dotenv');

dotenv.config();

const exercises = require('./exercises.json');

const app = express();
const PORT = process.env.PORT || 3000;

app.use(cors());
app.use(express.json());
app.use(express.static('public'));

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
  res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

app.post('/submit', (req, res) => {
  const uploadMiddleware = upload.array('javaFiles', 20);

  uploadMiddleware(req, res, async err => {
    if (err) {
      console.error('Multer error:', err);
      return res.status(400).json({
        error: 'File upload error',
        status: '❌',
        message: `Upload failed: ${err.message}`,
      });
    }

    try {
      if (!req.files || req.files.length === 0) {
        return res
          .status(400)
          .json({ error: 'Keine Java-Dateien hochgeladen' });
      }

      const { exercise } = req.body;
      const uploadedFiles = req.files;

      console.log(
        `Processing submission: ${uploadedFiles.map(f => f.filename).join(', ')} for exercise: ${exercise}`
      );

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
        if (!encodingCheck.valid) {
          return res.json({
            success: false,
            status: '⚠️',
            message: 'Encoding Error: Non-ASCII characters detected',
            details: encodingCheck.details,
          });
        }

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
        if (!requiredFilesCheck.valid) {
          return res.json({
            success: false,
            status: '❌',
            message: 'Fehlende erforderliche Dateien',
            details: requiredFilesCheck.details,
            points: 0,
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

        const compilationResult = await compileJavaFiles(
          tempDir,
          uploadedFiles.map(f => f.filename),
          exerciseConfig
        );

        if (!compilationResult.success) {
          return res.json({
            success: false,
            status: '💀',
            message: 'Compilation failed',
            details: compilationResult.error,
          });
        }

        // If exercise has tests, try to compile and run public tests
        if (exerciseConfig.hasTests) {
          const testResult = await runPublicTests(
            tempDir,
            exercise,
            exerciseConfig
          );

          if (!testResult.success) {
            return res.json({
              success: false,
              status: testResult.status,
              message: testResult.message,
              details: testResult.details,
              deadline: exerciseConfig.deadline,
              deadlinePassed: new Date() > new Date(exerciseConfig.deadline),
            });
          }

          // Check if deadline has passed and run secret tests if available
          const deadlinePassed = new Date() > new Date(exerciseConfig.deadline);
          let secretTestResult = null;

          if (deadlinePassed) {
            secretTestResult = await runSecretTests(
              tempDir,
              exercise,
              exerciseConfig
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
          if (deadlinePassed && secretTestResult) {
            response.secretTests = {
              success: secretTestResult.success,
              status: secretTestResult.status,
              message: secretTestResult.message,
              details: secretTestResult.details,
            };
          }

          return res.json(response);
        }

        // If no tests, just return compilation success
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
        console.error('Error during processing:', processError);
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
          console.error(
            'Warning: Could not clean up temp directory:',
            cleanupError.message
          );
        }
      }
    } catch (error) {
      console.error('Error processing submission:', error);
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
          await fs.remove(file.path).catch(console.error);
        }
      }
    }
  });
});

function compileJavaFiles(workingDir, fileNames, exerciseConfig) {
  return new Promise(resolve => {
    try {
      console.log(`Compiling student files: ${fileNames.join(', ')}`);

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
            console.log(`Using classpath with JARs: ${classpath}`);
          }
        }
      } catch (jarError) {
        console.log('Error handling JAR files:', jarError.message);
      }

      const command = `javac -source 8 -target 8 -cp ${classpath} ${fileNames.map(name => `"${name}"`).join(' ')}`;
      console.log(`Compilation command: ${command}`);

      exec(command, { cwd: workingDir }, (error, stdout, stderr) => {
        if (error) {
          resolve({
            success: false,
            error: stderr || error.message,
          });
        } else {
          resolve({
            success: true,
            output: stdout,
          });
        }
      });
    } catch (err) {
      resolve({
        success: false,
        error: `Error setting up compilation: ${err.message}`,
      });
    }
  });
}

async function runPublicTests(workingDir, exercise, exerciseConfig) {
  try {
    console.log(`Running public tests for exercise: ${exercise}`);

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
      console.log('Error reading JAR files for tests:', jarError.message);
    }

    // Determine test class name
    const testClassMapping = {
      caesarchiffre: 'CaesarChiffrePublicTest',
      signalplotter: 'SignalPlotterPublicTest',
      color: 'ColorPublicTest',
      snakegame: 'SnakePublicTest',
      sortedset: 'SortedSetPublicTest',
      contactdb: 'ContactDatabasePublicTest',
      binarytree: 'BinarySearchTreePublicTest',
    };

    const testClassName = testClassMapping[exercise];
    if (!testClassName) {
      return {
        success: true,
        status: '✅',
        message: 'Compilation successful',
        details:
          'Dein Code wurde erfolgreich kompiliert. Du kannst ihn so abgeben!',
      };
    }

    // Check if test file exists
    const testFilePath = path.join(workingDir, `${testClassName}.java`);
    if (!(await fs.pathExists(testFilePath))) {
      return {
        success: true,
        status: '✅',
        message: 'Compilation successful',
        details:
          'Dein Code wurde erfolgreich kompiliert. Du kannst ihn so abgeben!',
      };
    }

    // Compile test files
    const compileTestCommand = `javac -source 8 -target 8 -cp ${classpath} "${testClassName}.java"`;
    console.log(`Test compilation command: ${compileTestCommand}`);

    const testCompilationResult = await execPromise(compileTestCommand, {
      cwd: workingDir,
    });

    if (testCompilationResult.error) {
      return {
        success: false,
        status: '💀',
        message: 'Test compilation failed',
        details: `Dein Code wurde erfolgreich kompiliert, aber die Tests konnten nicht kompiliert werden. Dies bedeutet normalerweise:\n- Fehlende erforderliche Methoden\n- Falsche Methodensignaturen\n- Falsche Klassen-/Methodennamen\n\nTestkompilierungsfehler:\n${testCompilationResult.stderr}`,
      };
    }

    // Run the public tests
    const runTestCommand = `java -cp ${classpath} org.junit.runner.JUnitCore ${testClassName}`;
    console.log(`Test execution command: ${runTestCommand}`);

    const testResult = await execPromise(runTestCommand, {
      cwd: workingDir,
      timeout: 30000, // 30 second timeout
    });

    if (testResult.error && testResult.error.code !== 'timeout') {
      // Tests ran but some failed
      return {
        success: false,
        status: '💀',
        message: 'Tests failed',
        details: `Test output:\n${testResult.stdout}\n\nErrors:\n${testResult.stderr}`,
      };
    }

    return {
      success: true,
      status: '✅',
      message: 'All tests passed!',
      details:
        'Dein Code wurde erfolgreich kompiliert und hat alle Tests bestanden! Du kannst ihn so abgeben!',
    };
  } catch (error) {
    console.error('Error running tests:', error);
    return {
      success: false,
      status: '⚠️',
      message: 'Test execution error',
      details: `An error occurred while running tests: ${error.message}`,
    };
  }
}

async function runSecretTests(workingDir, exercise, exerciseConfig) {
  try {
    console.log(`Running secret tests for exercise: ${exercise}`);

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
      console.log(
        'Error reading JAR files for secret tests:',
        jarError.message
      );
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
    const compileSecretTestCommand = `javac -source 8 -target 8 -cp ${classpath} "${secretTestClassName}.java"`;
    console.log(`Secret test compilation command: ${compileSecretTestCommand}`);

    const secretTestCompilationResult = await execPromise(
      compileSecretTestCommand,
      { cwd: workingDir }
    );

    if (secretTestCompilationResult.error) {
      return {
        success: false,
        status: '💀',
        message: 'Zusätzliche Testkompilierung fehlgeschlagen',
        details: `Zusätzliche Testkompilierungsfehler:\n${secretTestCompilationResult.stderr}`,
      };
    }

    // Run the secret tests
    const runSecretTestCommand = `java -cp ${classpath} org.junit.runner.JUnitCore ${secretTestClassName}`;
    console.log(`Secret test execution command: ${runSecretTestCommand}`);

    const secretTestResult = await execPromise(runSecretTestCommand, {
      cwd: workingDir,
      timeout: 30000, // 30 second timeout
    });

    if (secretTestResult.error && secretTestResult.error.code !== 'timeout') {
      // Secret tests ran but some failed
      return {
        success: false,
        status: '💀',
        message: 'Zusätzliche Tests sind fehlgeschlagen',
        details: `Zusätzliche Testausgabe:\n${secretTestResult.stdout}\n\nFehler:\n${secretTestResult.stderr}`,
      };
    }

    return {
      success: true,
      status: '✅',
      message: 'Alle zusätzlichen Tests bestanden!',
      details: 'Dein Code hat alle zusätzlichen Tests erfolgreich bestanden!',
    };
  } catch (error) {
    console.error('Error running secret tests:', error);
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
  } catch (error) {
    if (error.code === 'EBUSY' || error.code === 'ENOTEMPTY') {
      console.log('Files locked, attempting cleanup with delay...');

      await new Promise(resolve => setTimeout(resolve, 1000));

      try {
        await fs.remove(tempDir);
      } catch (retryError) {
        console.warn('Could not clean up temp directory:', retryError.message);
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
    console.log(`AuD Tester Website running on http://localhost:${PORT}`);
    console.log('Enforcing Java 8 language level (-source 8 -target 8)');
  });
})();

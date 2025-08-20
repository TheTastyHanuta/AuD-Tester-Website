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
    }
});

const upload = multer({ 
    storage: storage,
    fileFilter: (req, file, cb) => {
        if (file.mimetype === 'text/x-java-source' || file.originalname.endsWith('.java')) {
            cb(null, true);
        } else {
            cb(new Error('Only Java files are allowed!'), false);
        }
    }
});

app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

app.post('/submit', (req, res) => {
    const uploadMiddleware = upload.array('javaFiles', 20);
    
    uploadMiddleware(req, res, async (err) => {
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
                return res.status(400).json({ error: 'No Java files uploaded' });
            }

            const { exercise } = req.body;
            const uploadedFiles = req.files;
            
            console.log(`Processing submission: ${uploadedFiles.map(f => f.filename).join(', ')} for exercise: ${exercise}`);

            const tempDir = path.join('temp', `${Date.now()}_${Math.random()}`);
            await fs.ensureDir(tempDir);

            try {
                for (const file of uploadedFiles) {
                    const tempFilePath = path.join(tempDir, file.filename);
                    await fs.copy(file.path, tempFilePath);
                }

                const exerciseConfig = getExerciseConfig(exercise);
                if (!exerciseConfig) {
                    return res.json({
                        success: false,
                        status: '❌',
                        message: 'Invalid exercise selected',
                        details: 'The selected exercise was not found.',
                    });
                }

                if (exerciseConfig.hasTests) {
                    const testDirMapping = {
                        'caesarchiffre': 'CaesarChiffreTests',
                        'signalplotter': 'SignalPlotterTests', 
                        'color': 'ColorTests',
                        'snakegame': 'SnakeTests',
                        'sortedset': 'SortedSetTests',
                        'contactdb': 'ContactDatabaseTests',
                        'binarytree': 'BinarySearchTreeTests'
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

                const compilationResult = await compileJavaFiles(tempDir, uploadedFiles.map(f => f.filename), exerciseConfig);
                
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
                    const testResult = await runPublicTests(tempDir, exercise, exerciseConfig);
                    
                    if (!testResult.success) {
                        return res.json({
                            success: false,
                            status: testResult.status,
                            message: testResult.message,
                            details: testResult.details,
                            deadline: exerciseConfig.deadline,
                            deadlinePassed: new Date() > new Date(exerciseConfig.deadline)
                        });
                    }
                    
                    // If tests passed, return test results
                    return res.json({
                        success: true,
                        status: testResult.status,
                        message: testResult.message,
                        details: testResult.details,
                        deadline: exerciseConfig.deadline,
                        deadlinePassed: new Date() > new Date(exerciseConfig.deadline)
                    });
                }

                // If no tests, just return compilation success
                res.json({
                    success: true,
                    status: '✅',
                    message: 'Compilation successful',
                    details: 'Your code compiled without errors!',
                    deadline: exerciseConfig.deadline,
                    deadlinePassed: new Date() > new Date(exerciseConfig.deadline)
                });

            } catch (processError) {
                console.error('Error during processing:', processError);
                if (!res.headersSent) {
                    return res.status(500).json({ 
                        error: 'Processing error',
                        status: '⚠️',
                        message: 'An error occurred while processing your submission'
                    });
                }
            } finally {
                try {
                    await cleanupTempDir(tempDir);
                } catch (cleanupError) {
                    console.error('Warning: Could not clean up temp directory:', cleanupError.message);
                }
            }

        } catch (error) {
            console.error('Error processing submission:', error);
            if (!res.headersSent) {
                res.status(500).json({ 
                    error: 'Internal server error',
                    status: '⚠️',
                    message: 'An internal error occurred while processing your submission'
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
    return new Promise((resolve) => {
        try {
            console.log(`Compiling student files: ${fileNames.join(', ')}`);
            
            let classpath = '.';
            const testsDir = path.join(__dirname, 'tests');
            
            try {
                if (fs.existsSync(testsDir)) {
                    const jarFiles = fs.readdirSync(testsDir)
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
            
            const command = `javac -cp ${classpath} ${fileNames.map(name => `"${name}"`).join(' ')}`;
            console.log(`Compilation command: ${command}`);
            
            exec(command, { cwd: workingDir }, (error, stdout, stderr) => {
                if (error) {
                    resolve({
                        success: false,
                        error: stderr || error.message
                    });
                } else {
                    resolve({
                        success: true,
                        output: stdout
                    });
                }
            });
        } catch (err) {
            resolve({
                success: false,
                error: `Error setting up compilation: ${err.message}`
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
            'caesarchiffre': 'CaesarChiffrePublicTest',
            'signalplotter': 'SignalPlotterPublicTest',
            'color': 'ColorPublicTest',
            'snakegame': 'SnakePublicTest',
            'sortedset': 'SortedSetPublicTest',
            'contactdb': 'ContactDatabasePublicTest',
            'binarytree': 'BinarySearchTreePublicTest'
        };
        
        const testClassName = testClassMapping[exercise];
        if (!testClassName) {
            return {
                success: true,
                status: '✅',
                message: 'Compilation successful',
                details: 'Your code compiled without errors!',
            };
        }
        
        // Check if test file exists
        const testFilePath = path.join(workingDir, `${testClassName}.java`);
        if (!await fs.pathExists(testFilePath)) {
            return {
                success: true,
                status: '✅',
                message: 'Compilation successful',
                details: 'Your code compiled without errors!',
            };
        }
        
        // Compile test files
        const compileTestCommand = `javac -cp ${classpath} "${testClassName}.java"`;
        console.log(`Test compilation command: ${compileTestCommand}`);
        
        const testCompilationResult = await execPromise(compileTestCommand, { cwd: workingDir });
        
        if (testCompilationResult.error) {
            return {
                success: false,
                status: '💀',
                message: 'Test compilation failed',
                details: `Your code compiled, but the tests failed to compile. This usually means:\n- Missing required methods\n- Wrong method signatures\n- Wrong class/method names\n\nTest compilation error:\n${testCompilationResult.stderr}`,
            };
        }
        
        // Run the public tests
        const runTestCommand = `java -cp ${classpath} org.junit.runner.JUnitCore ${testClassName}`;
        console.log(`Test execution command: ${runTestCommand}`);
        
        const testResult = await execPromise(runTestCommand, { 
            cwd: workingDir,
            timeout: 30000 // 30 second timeout
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
            details: 'Your code compiled and passed all tests successfully!',
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

function execPromise(command, options) {
    return new Promise((resolve) => {
        exec(command, options, (error, stdout, stderr) => {
            resolve({
                error: error,
                stdout: stdout || '',
                stderr: stderr || ''
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

function getExerciseConfig(exerciseId) {
    return exercises.find(ex => ex.id === exerciseId);
}

app.get('/exercises', (req, res) => {
    res.json(exercises);
});

app.listen(PORT, () => {
    console.log(`AuD Tester Website running on http://localhost:${PORT}`);
});

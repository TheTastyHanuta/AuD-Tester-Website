const express = require('express');
const multer = require('multer');
const path = require('path');
const fs = require('fs-extra');
const { exec } = require('child_process');
const cors = require('cors');

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
                points: 0
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
                        points: 0
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
                        points: 0
                    });
                }

                res.json({
                    success: true,
                    status: '✅',
                    message: 'Compilation successful',
                    details: 'Your code compiled without errors!',
                    points: exerciseConfig.points || 10,
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

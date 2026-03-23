const multer = require('multer');
const path = require('path');
const fs = require('fs-extra');

const MAX_FILE_SIZE_BYTES = 2 * 1024 * 1024; // 2 MB per file

const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    const uploadDir = 'uploads';
    fs.ensureDirSync(uploadDir);
    cb(null, uploadDir);
  },
  filename: (req, file, cb) => {
    const base = path.basename(file.originalname);
    const safe = base.replace(/[^A-Za-z0-9._-]/g, '_');
    cb(null, safe);
  },
});

const upload = multer({
  storage: storage,
  limits: {
    fileSize: MAX_FILE_SIZE_BYTES,
    files: 20,
  },
  fileFilter: (req, file, cb) => {
    const ext = path.extname(file.originalname || '').toLowerCase();
    if (ext === '.java') {
      cb(null, true);
    } else {
      cb(new Error('Nur Java-Dateien sind erlaubt!'), false);
    }
  },
});

module.exports = upload;

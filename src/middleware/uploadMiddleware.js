const multer = require('multer');
const path = require('path');
const fs = require('fs-extra');

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

module.exports = upload;

const express = require('express');
const path = require('path');
const router = express.Router();
const logger = require('../../logger');
const fs = require('fs-extra');
const {
  handleClientLogOptions,
  handleClientLog,
} = require('../controllers/clientLogController');

const EXERCISES_FILE = path.join(__dirname, '../../exercises.json');

router.get('/', (req, res) => {
  logger.info('Main page accessed', {
    sessionId: req.session.id,
    userAgent: req.get('User-Agent'),
    ip: req.ip,
  });
  res.sendFile(path.join(__dirname, '../../public', 'index.html'));
});

router.get('/exercises', async (req, res) => {
  try {
    const exercises = await fs.readJson(EXERCISES_FILE);
    res.json(exercises);
  } catch (error) {
    logger.error('Error reading exercises', { error: error.message });
    res.status(500).json({ error: 'Failed to load exercises' });
  }
});

router.options('/client-log', handleClientLogOptions);
router.post('/client-log', handleClientLog);

module.exports = router;

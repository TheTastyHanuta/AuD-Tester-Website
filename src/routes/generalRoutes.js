const express = require('express');
const path = require('path');
const router = express.Router();
const logger = require('../../logger');
const exercises = require('../../exercises.json');
const {
  handleClientLogOptions,
  handleClientLog,
} = require('../controllers/clientLogController');

router.get('/', (req, res) => {
  logger.info('Main page accessed', {
    sessionId: req.session.id,
    userAgent: req.get('User-Agent'),
    ip: req.ip,
  });
  res.sendFile(path.join(__dirname, '../../public', 'index.html'));
});

router.get('/exercises', (req, res) => {
  res.json(exercises);
});

router.options('/client-log', handleClientLogOptions);
router.post('/client-log', handleClientLog);

module.exports = router;

const express = require('express');
const router = express.Router();
const { handleSubmission } = require('../controllers/submissionController');

router.post('/submit', handleSubmission);

module.exports = router;

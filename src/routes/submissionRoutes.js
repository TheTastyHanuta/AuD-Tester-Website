const express = require('express');
const router = express.Router();
const { handleSubmission } = require('../controllers/submissionController');
const { submissionLimiter } = require('../middleware/rateLimitMiddleware');

router.post('/submit', submissionLimiter, handleSubmission);

module.exports = router;

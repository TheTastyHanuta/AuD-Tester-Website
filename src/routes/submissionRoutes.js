const express = require('express');
const router = express.Router();
const {
  getSubmissionStatus,
  handleSubmission,
} = require('../controllers/submissionController');
const { submissionLimiter } = require('../middleware/rateLimitMiddleware');

router.post('/submit', submissionLimiter, handleSubmission);
router.post('/api/submit', submissionLimiter, handleSubmission);
router.get('/api/status/:jobId', getSubmissionStatus);

module.exports = router;

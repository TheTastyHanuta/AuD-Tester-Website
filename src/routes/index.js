const express = require('express');
const router = express.Router();

const generalRoutes = require('./generalRoutes');
const submissionRoutes = require('./submissionRoutes');
const adminRoutes = require('./adminRoutes');

// Mount all routes
router.use('/', generalRoutes);
router.use('/', submissionRoutes);
router.use('/', adminRoutes);

module.exports = router;

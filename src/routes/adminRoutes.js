const express = require('express');
const router = express.Router();
const requireAdmin = require('../middleware/authMiddleware');
const {
  showLoginPage,
  handleLogin,
  handleLogout,
  showLogsPage,
  listLogs,
  getLogFile,
  streamLogSSE,
} = require('../controllers/adminController');

router.get('/admin/login', showLoginPage);
router.post('/admin/login', handleLogin);
router.post('/admin/logout', handleLogout);
router.get('/admin/logs', requireAdmin, showLogsPage);
router.get('/admin/logs/list', requireAdmin, listLogs);
router.get('/admin/logs/file', requireAdmin, getLogFile);
router.get('/admin/logs/sse', requireAdmin, streamLogSSE);

module.exports = router;

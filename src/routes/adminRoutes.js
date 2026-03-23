const express = require('express');
const router = express.Router();
const requireAdmin = require('../middleware/authMiddleware');
const { adminLoginLimiter } = require('../middleware/rateLimitMiddleware');
const {
  showLoginPage,
  handleLogin,
  handleLogout,
  showLogsPage,
  showExercisesPage,
  listLogs,
  getLogFile,
  streamLogSSE,
  getExercises,
  updateDeadline,
} = require('../controllers/adminController');

router.get('/admin/login', showLoginPage);
router.post('/admin/login', adminLoginLimiter, handleLogin);
router.post('/admin/logout', handleLogout);
router.get('/admin/logs', requireAdmin, showLogsPage);
router.get('/admin/exercises-page', requireAdmin, showExercisesPage);
router.get('/admin/logs/list', requireAdmin, listLogs);
router.get('/admin/logs/file', requireAdmin, getLogFile);
router.get('/admin/logs/sse', requireAdmin, streamLogSSE);
router.get('/admin/exercises', requireAdmin, getExercises);
router.put('/admin/exercises/:id/deadline', requireAdmin, updateDeadline);

module.exports = router;

const express = require('express');
const router = express.Router();
const requireAdmin = require('../middleware/authMiddleware');
const requireAdminCsrf = require('../middleware/adminCsrfMiddleware');
const { adminLoginLimiter } = require('../middleware/rateLimitMiddleware');
const {
  showAdminPage,
  showLoginPage,
  handleLogin,
  handleLogout,
  getAdminSession,
  showLogsPage,
  showExercisesPage,
  listLogs,
  getLogFile,
  streamLogSSE,
  getExercises,
  updateDeadline,
  updateDeadlines,
} = require('../controllers/adminController');

router.get('/admin/login', showLoginPage);
router.post('/admin/login', adminLoginLimiter, handleLogin);
router.post('/admin/logout', requireAdmin, requireAdminCsrf, handleLogout);
router.get('/admin', requireAdmin, showAdminPage);
router.get('/admin/session', requireAdmin, getAdminSession);
router.get('/admin/logs', requireAdmin, showLogsPage);
router.get('/admin/exercises-page', requireAdmin, showExercisesPage);
router.get('/admin/logs/list', requireAdmin, listLogs);
router.get('/admin/logs/file', requireAdmin, getLogFile);
router.get('/admin/logs/sse', requireAdmin, streamLogSSE);
router.get('/admin/exercises', requireAdmin, getExercises);
router.put(
  '/admin/exercises/deadlines',
  requireAdmin,
  requireAdminCsrf,
  updateDeadlines
);
router.put(
  '/admin/exercises/:id/deadline',
  requireAdmin,
  requireAdminCsrf,
  updateDeadline
);

module.exports = router;

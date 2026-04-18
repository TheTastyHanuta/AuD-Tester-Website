function requireAdmin(req, res, next) {
  if (req.session && req.session.isLogAdmin === true) {
    return next();
  }
  // If not authenticated, redirect to login with return path
  const ret = encodeURIComponent(req.originalUrl || '/admin');
  return res.redirect(`/admin/login?return=${ret}`);
}

module.exports = requireAdmin;

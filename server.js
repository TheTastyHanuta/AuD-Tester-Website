const app = require('./src/app');
const config = require('./src/config/config');
const logger = require('./logger');
const { checkJavaVersion } = require('./src/services/compilationService');

// Check Java version and start server
(async () => {
  await checkJavaVersion();
  app.listen(config.PORT, config.HOST, () => {
    logger.info('AuD Tester Website started', {
      port: config.PORT,
      host: config.HOST,
      environment: process.env.NODE_ENV || 'development',
      javaVersion: 'Java 8 (enforced with -source 8 -target 8)',
      logLevel: logger.level,
    });
    console.log(
      `AuD Tester Website running on http://${config.HOST}:${config.PORT}`
    );
  });
})();

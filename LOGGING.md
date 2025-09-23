# Logging System Documentation

This project uses Winston for comprehensive logging with daily file rotation and Express sessions for user tracking.

## Log Files

The logging system creates several log files in the `logs/` directory:

- **`error-YYYY-MM-DD.log`** - Contains only error level messages
- **`app-YYYY-MM-DD.log`** - Contains info, warn, and error messages (application logs)
- **`combined-YYYY-MM-DD.log`** - Contains all log levels including debug messages

## Session Tracking

Every user interaction is tracked with a unique session ID that persists across requests:

- Session IDs are automatically generated for each visitor
- Session data is maintained for 24 hours
- All log entries include the session ID for easy correlation
- HTTP requests show session ID in the format: `IP [sessionId] - "METHOD URL"`

## Log Levels

- **error**: System errors, compilation failures, test failures
- **warn**: Warnings like cleanup issues, JAR file problems
- **info**: General application flow, successful operations, user submissions
- **debug**: Detailed information for troubleshooting (commands, file operations)

## Configuration

Configure logging and sessions through environment variables in `.env`:

```env
# Set log level (error, warn, info, debug)
LOG_LEVEL=info

# Set environment (development shows console logs, production minimizes them)
NODE_ENV=development

# Session secret (change in production!)
SESSION_SECRET=secret-change-in-production
```

## Log Rotation

- Files rotate daily with date suffix
- Compressed after rotation
- Error logs kept for 14 days
- Other logs kept for 30 days
- Max file size: 20MB before forced rotation

## Console Output

- **Development**: Colorized console output with debug level
- **Production**: Console output with info level only

## Structured Logging

All logs include structured metadata with session tracking:

```javascript
logger.info('Processing submission', {
  sessionId: sessionId,
  files: uploadedFiles.map(f => f.filename),
  exercise: exercise,
  fileCount: uploadedFiles.length,
});
```

## Viewing Logs

```bash
# View latest logs
tail -f logs/app-$(date +%Y-%m-%d).log

# View errors only
tail -f logs/error-$(date +%Y-%m-%d).log

# Search for specific submissions
grep "Processing submission" logs/app-*.log

# View specific exercise submissions
grep "exercise.*binarytree" logs/app-*.log

# Track specific user session
grep "sessionId.*abc123" logs/app-*.log

# Follow user journey chronologically
grep "sessionId.*abc123" logs/combined-*.log | sort
```

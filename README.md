# AuD Tester Website

A web-based Java code testing platform for Algorithms and Data Structures courses. This application provides automated compilation testing and feedback for student submissions.

## Features

### Website Features

- **Multi-file Upload Support**: Upload multiple Java files for complex exercises
- **Automated Compilation Testing**: Instant feedback on compilation errors with detailed error messages
- **Exercise Management**: Configurable exercises with points and deadlines
- **External JAR Dependencies**: Support for helper libraries and visualization tools
- **Drag and Drop Interface**: User-friendly file upload with drag and drop functionality
- **Clean Results Display**: Clear success/failure indicators with detailed feedback
- **Temporary File Management**: Secure handling and cleanup of uploaded files
- **Cross-platform Compatibility**: Works on Windows, macOS, and Linux

### Admin Log Viewer & Exercise Manager

A password-protected log viewer is available to inspect Winston rotated logs without SSH. Additionally, an exercise manager allows modifying exercises through a web interface.

Setup:

- Set environment variables:
  - `SESSION_SECRET` — strong random string
  - `LOG_VIEWER_PASSWORD` — password for admin login

Usage:

- Visit `/admin/login`, sign in, then go to `/admin/logs`.
- Choose among combined, app, or error logs.
- Live stream uses Server-Sent Events (tail -F). Older rotated logs can be viewed or downloaded.
- Exercise manager at `/admin/exercises` allows editing exercises with changes saved to `exercises.json`.

## Usage and Development

### Requirements

- Node.js (version 24 or higher)
- Docker (for compiling and testing submissions in containers)
- A local Java compiler Docker image, defaulting to `eclipse-temurin:17-jdk`
- npm or yarn package manager

### Installation

1. Clone or download the project to your local machine

   ```bash
   git clone https://github.com/TheTastyHanuta/AuD-Tester-Website
   ```

2. Navigate to the project directory

   ```bash
   cd AuD-Tester-Website
   ```

3. Install dependencies:

   ```bash
   npm install
   ```

4. Copy the example exercises file to create your local exercises.json:

   ```bash
   cp exercises.example.json exercises.json
   ```

   This will create your local exercises.json, which is ignored by git. You can safely modify it without affecting the repository or other users.

5. Ensure Docker is installed and accessible via command line:

   ```bash
   docker --version
   ```

6. Install Perl module JSON::XS

   ```bash
   sudo apt-get install libjson-xs-perl
   ```

### Create Docker Images for exercises

This website uses Docker images to compile and test submissions. For it to work on your machine you will need a Java compiler image for exercises without tests and the test images for exercises with tests.

By default, compilation uses `eclipse-temurin:17-jdk`. You can override this with `JAVA_COMPILER_DOCKER_IMAGE`.

To install the default compiler image:

```bash
docker pull eclipse-temurin:17-jdk
docker run --rm eclipse-temurin:17-jdk javac -version
```

If you prefer a local project-specific tag, create a minimal `Dockerfile.javac`:

```dockerfile
FROM eclipse-temurin:17-jdk
WORKDIR /user
```

Then build it and point the app at it:

```bash
docker build -t aud-javac -f Dockerfile.javac .
JAVA_COMPILER_DOCKER_IMAGE=aud-javac
```

You can find the files to create the exercise test images in this repository: <https://github.com/TheTastyHanuta/AuD-Docker-Files>

Follow the instructions in the README of that repository to create the images.

### Starting the Server

```bash
node server.js
```

The application will start on `http://localhost:3000` by default.

### Starting the Background Worker

Submissions are queued in a local SQLite database at `data/submission-jobs.sqlite`.
Run the web server and worker as separate processes:

```bash
npm run start:prod
npm run worker:prod
```

Run one worker process for this local SQLite queue.

For development, use:

```bash
npm run start:dev
npm run worker
```

The upload endpoint returns `202 Accepted` with a `jobId` and `statusUrl`. Poll
`GET /api/status/:jobId` until the job status is `completed` or `failed`.
Completed and failed job results are kept for 72 hours by default. Override this
with `JOB_RETENTION_HOURS`; the worker checks for expired jobs once per hour by
default, configurable with `JOB_CLEANUP_INTERVAL_MS`.

#### Starting in Production Mode

To start the application in production mode, use the following command:

```bash
npm run start:prod
```

#### Starting with hot reloading (currently broken idk why)

For development, you can use hot reloading to automatically restart the server when file changes are detected. Use the following command:

```bash
npm run dev
```

### Code Formatting

This project uses Prettier for consistent code formatting. Available commands:

```bash
# Format all files
npm run format
```

## Configuration

### Environment Variables

The application uses the following environment variables:

- `PORT`: The port on which the server will run (default: 3000)
- `BIND_HOST`: Address the server binds to (default: `127.0.0.1`)
- `SHOW_SECRET_TESTS`: Boolean flag to show/hide secret tests in the UI
- `FORCE_SHOW_SECRET_TESTS`: Boolean flag to force showing secret tests regardless of deadline or success
- `SESSION_SECRET`: Secret key for session encryption
- `LOG_VIEWER_PASSWORD`: Password for the admin log viewer
- `NODE_ENV`: The environment in which the application is running (default: development)
- `LOG_LEVEL`: The logging level for the application (default: info)
- `SESSION_DB_PATH`: File path for admin session storage (default: `data/admin-sessions.sqlite`)
- `JOB_QUEUE_DB_PATH`: File path for queued submission state (default: `data/submission-jobs.sqlite`)
- `JOB_WORKER_POLL_INTERVAL_MS`: How often the worker checks for pending jobs (default: `1000`)
- `JOB_RETENTION_HOURS`: How long completed and failed job results remain accessible (default: `72`)
- `JOB_CLEANUP_INTERVAL_MS`: How often the worker removes expired jobs (default: `3600000`)
- `JAVA_COMPILER_DOCKER_IMAGE`: Docker image used to compile exercises without tests (default: `eclipse-temurin:17-jdk`)
- `JAVA_COMPILE_TIMEOUT_MS`: Timeout for compilation-only containers (default: `60000`)
- `DOCKER_TEST_TIMEOUT_MS`: Timeout for exercise test containers (default: `120000`)
- `DOCKER_MEMORY_LIMIT`: Memory limit for submission containers (default: `512m`)
- `DOCKER_CPU_LIMIT`: CPU limit for submission containers (default: `0.5`)
- `DOCKER_PIDS_LIMIT`: Process limit for submission containers (default: `100`)
- `DOCKER_TEST_IMAGE_MAPPING`: Exercise-to-Docker-image mapping in `src/config/config.js`

Create a `.env` file in the root directory and copy the contents of `.env.example` into it. Then, customize the values as needed.

### Exercise Setup

Exercises are configured in `exercises.json`. Each exercise has the following properties:

- `id`: Unique identifier for the exercise
- `name`: Display name shown to users
- `points`: Maximum points available
- `hasTests`: Boolean indicating if tests exist
- `required_files`: List of required file names (for validation)
- `deadline`: ISO date string for the submission deadline

Example exercise configuration:

```json
{
  "id": "helloworld",
  "name": "Hello World",
  "points": 3,
  "hasTests": false,
  "required_files": ["HelloWorld.java"],
  "deadline": "2025-09-01T23:59:59Z"
}
```

## How It Works

1. **File Upload**: Students upload Java files through the web interface
2. **Temporary Storage**: Files are stored in a temporary directory for processing
3. **Classpath Management**: JAR dependencies are automatically included in the classpath
4. **Compilation**: The system compiles uploaded files inside a Docker container
5. **JUnit Testing**: If tests are defined for the exercise, the system runs JUnit tests and captures the results via a Docker container
6. **Result Generation**: Compilation and test results are returned with detailed feedback
7. **Cleanup**: Temporary files are automatically cleaned up after processing

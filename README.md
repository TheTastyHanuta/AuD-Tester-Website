# AuD Tester Website

A web-based Java code testing platform for Algorithms and Data Structures courses. This application provides automated compilation testing and feedback for student submissions.

## Features

- **Multi-file Upload Support**: Upload multiple Java files for complex exercises
- **Automated Compilation Testing**: Instant feedback on compilation errors with detailed error messages
- **Exercise Management**: Configurable exercises with points and deadlines
- **External JAR Dependencies**: Support for helper libraries and visualization tools
- **Drag and Drop Interface**: User-friendly file upload with drag and drop functionality
- **Clean Results Display**: Clear success/failure indicators with detailed feedback
- **Temporary File Management**: Secure handling and cleanup of uploaded files
- **Cross-platform Compatibility**: Works on Windows, macOS, and Linux

## Usage and Development

### Requirements

- Node.js (version 14 or higher)
- Java Development Kit (JDK 8 or higher)
- npm or yarn package manager
- Docker (for running JUnit tests in a container)

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

4. Ensure Java is installed and accessible via command line:

   ```bash
   javac -version
   ```

5. Install Perl module JSON::XS

   ```bash
   sudo apt-get install libjson-xs-perl
   ```

### Create Docker Images for exercises

This website uses Docker Images to run tests in submissions. For it to work on your machine you will need those images. You can find all the files to create them in this repository: <https://github.com/TheTastyHanuta/AuD-Docker-Files>

Follow the instructions in the README of that repository to create the images.

### Starting the Server

```bash
node server.js
```

The application will start on `http://localhost:3000` by default.

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
- `SHOW_SECRET_TESTS`: Boolean flag to show/hide secret tests in the UI
- `FORCE_SHOW_SECRET_TESTS`: Boolean flag to force showing secret tests regardless of deadline or success
- `SESSION_SECRET`: Secret key for session encryption
- `NODE_ENV`: The environment in which the application is running (default: development)
- `LOG_LEVEL`: The logging level for the application (default: info)

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

## Project Structure

```bash
AuD Tester Website/
├── server.js              # Main server application
├── package.json            # Node.js dependencies
├── exercises.json          # Exercise configuration
├── public/                 # Frontend files
│   ├── index.html         # Main web interface
│   ├── script.js          # Client-side JavaScript
│   └── styles.css         # Styling
├── tests/                  # Test files and JAR dependencies
├── uploads/               # Temporary upload directory
├── provided/               # Provided files for exercises
└── temp/                  # Temporary compilation directory
```

## How It Works

1. **File Upload**: Students upload Java files through the web interface
2. **Temporary Storage**: Files are stored in a temporary directory for processing
3. **Classpath Management**: JAR dependencies are automatically included in the classpath
4. **Compilation**: The system attempts to compile all uploaded files using javac
5. **JUnit Testing**: If tests are defined for the exercise, the system runs JUnit tests and captures the results via a docker container
6. **Result Generation**: Compilation and test results are returned with detailed feedback
7. **Cleanup**: Temporary files are automatically cleaned up after processing

## Deployment on Oracle Cloud VM with Cloudflare Tunnel

I am hosting this application on an Oracle Cloud VM and using Cloudflare Tunnel for secure access.

### Install Dependencies

1. **Update System**:

   ```bash
   sudo apt update && sudo apt upgrade -y
   ```

2. **Install Node.js**:

   ```bash
   curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
   sudo apt-get install -y nodejs
   ```

3. **Install Java**:

   ```bash
   sudo apt install default-jdk -y
   ```

4. **Install Git**:

   ```bash
   sudo apt install git -y
   ```

5. **Install Docker**:

   ```bash
   sudo apt install docker.io -y
   ```

6. **Install Perl JSON::XS Module**:

   ```bash
   sudo apt-get install libjson-xs-perl
   ```

### Deploy Application

1. **Clone Repository**:

   ```bash
   git clone https://github.com/TheTastyHanuta/AuD-Tester-Website
   cd AuD-Tester-Website
   ```

2. **Install Dependencies**:

   ```bash
   npm install
   ```

3. Create Docker Images for exercises as described in the [Create Docker Images for exercises](#create-docker-images-for-exercises) section.

4. **Test Application**:

   ```bash
   node server.js
   ```

5. **Setup Process Manager** (PM2 for production):

   ```bash
   sudo npm install -g pm2
   pm2 start server.js --name "aud-tester"
   pm2 startup
   pm2 save
   ```

### Monitoring and Maintenance

1. **Check Application Status**:

   ```bash
   pm2 status
   pm2 logs aud-tester
   ```

2. **Update Application**:

   ```bash
   git pull origin main
   npm install
   pm2 restart aud-tester
   ```

## Contributing

### Adding New Features

Contributions are welcome! Please follow these guidelines:

1. **Code Style**: Maintain consistent formatting and naming conventions (see [Code Formatting](#code-formatting))
2. **Testing**: Test your changes with multiple file types and scenarios
3. **Documentation**: Update relevant documentation for new features

#### Submitting Changes

1. Create a descriptive commit message
2. Test your changes thoroughly
3. Update documentation if needed
4. Submit a pull request with a clear description of changes

### Reporting Issues

When reporting issues, please include:

- Operating system and version
- Node.js version
- Java version
- Steps to reproduce the issue
- Expected vs actual behavior
- Any error messages or console output

## License

This project is open source and available under MIT License.

## Admin Log Viewer

A password-protected log viewer is available to inspect Winston rotated logs without SSH.

Setup:

- Set environment variables:
  - `SESSION_SECRET` — strong random string
  - `LOG_VIEWER_PASSWORD` — password for admin login

Usage:

- Visit `/admin/login`, sign in, then go to `/admin/logs`.
- Choose among combined, app, or error logs.
- Live stream uses Server-Sent Events (tail -F). Older rotated logs can be viewed or downloaded.

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

## Requirements

- Node.js (version 14 or higher)
- Java Development Kit (JDK 8 or higher)
- npm or yarn package manager

## Installation

1. Clone or download the project to your local machine
2. Navigate to the project directory
3. Install dependencies:

   ```bash
   npm install
   ```

4. Ensure Java is installed and accessible via command line:

   ```bash
   javac -version
   ```

## Configuration

### Exercise Setup

Exercises are configured in `exercises.json`. Each exercise has the following properties:

- `id`: Unique identifier for the exercise
- `name`: Display name shown to users
- `points`: Maximum points available
- `hasTests`: Boolean indicating if tests exist (for future implementation)
- `deadline`: ISO date string for the submission deadline

Example exercise configuration:

```json
{
    "id": "helloworld",
    "name": "Hello World",
    "points": 3,
    "hasTests": false,
    "deadline": "2025-09-01T23:59:59Z"
}
```

### Test Files and Dependencies

- Place JAR dependencies in the `tests/` directory
- Helper files and test classes can be organized in subdirectories under `tests/`
- The system automatically includes JAR files in the compilation classpath

## Usage

### Starting the Server

```bash
node server.js
```

The application will start on `http://localhost:3000` by default.

### For Students

1. Open the website in your browser
2. Select the appropriate exercise from the dropdown menu
3. Upload your Java files using the file picker or drag and drop
4. Click "Test Code" to submit your solution
5. Review the compilation results and fix any errors if needed

### For Instructors

1. Add new exercises to `exercises.json`
2. Place any required helper files or JAR dependencies in the `tests/` directory
3. Restart the server to load new exercise configurations
4. Students can immediately access the new exercises

## Project Structure

```
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
└── temp/                  # Temporary compilation directory
```

## How It Works

1. **File Upload**: Students upload Java files through the web interface
2. **Temporary Storage**: Files are stored in a temporary directory for processing
3. **Compilation**: The system attempts to compile all uploaded files using javac
4. **Classpath Management**: JAR dependencies are automatically included in the classpath
5. **Result Generation**: Compilation results are returned with detailed feedback
6. **Cleanup**: Temporary files are automatically cleaned up after processing

## Development

### Adding New Features

1. Fork the repository or create a feature branch
2. Make your changes to the appropriate files
3. Test thoroughly with various Java file combinations
4. Update documentation as needed

### Testing

Test the application with various scenarios:

- Single file uploads
- Multiple file uploads
- Files with compilation errors
- Files requiring external JAR dependencies
- Different exercise configurations

## Contributing

Contributions are welcome! Please follow these guidelines:

1. **Code Style**: Maintain consistent formatting and naming conventions
2. **Testing**: Test your changes with multiple file types and scenarios
3. **Documentation**: Update relevant documentation for new features

### Reporting Issues

When reporting issues, please include:

- Operating system and version
- Node.js version
- Java version
- Steps to reproduce the issue
- Expected vs actual behavior
- Any error messages or console output

### Submitting Changes

1. Create a descriptive commit message
2. Test your changes thoroughly
3. Update documentation if needed
4. Submit a pull request with a clear description of changes

## Deployment

### Hosting on Oracle Cloud VM with Cloudflare Tunnel

I am hosting this application on an Oracle Cloud VM and using Cloudflare Tunnel for secure access.

#### Install Dependencies

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

#### Deploy Application

1. **Clone Repository**:

   ```bash
   git clone https://github.com/TheTastyHanuta/AuD-Tester-Website
   cd AuD-Tester-Website
   ```

2. **Install Dependencies**:

   ```bash
   npm install
   ```

3. **Test Application**:

   ```bash
   node server.js
   ```

4. **Setup Process Manager** (PM2 for production):

   ```bash
   sudo npm install -g pm2
   pm2 start server.js --name "aud-tester"
   pm2 startup
   pm2 save
   ```

#### Monitoring and Maintenance

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

## License

This project is open source and available under standard academic use terms.

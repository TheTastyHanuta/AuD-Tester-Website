const path = require('path');
const fs = require('fs-extra');
const logger = require('../../logger');

async function validateUSASCIIEncoding(workingDir, fileNames, log = logger) {
  const nonASCIIFiles = [];

  log.debug('Starting US-ASCII encoding validation', {
    fileNames,
  });

  try {
    for (const fileName of fileNames) {
      const filePath = path.join(workingDir, fileName);
      const content = await fs.readFile(filePath, 'utf8');

      // Check each character in the file
      let lineNumber = 1;
      let columnNumber = 1;
      const fileProblems = [];

      for (let i = 0; i < content.length; i++) {
        const char = content[i];
        const charCode = char.charCodeAt(0);

        // US-ASCII is 0-127, anything above is non-ASCII
        if (charCode > 127) {
          fileProblems.push({
            char: char,
            charCode: charCode,
            line: lineNumber,
            column: columnNumber,
            hexCode: '0x' + charCode.toString(16).toUpperCase(),
          });
        }

        // Track line and column numbers
        if (char === '\n') {
          lineNumber++;
          columnNumber = 1;
        } else {
          columnNumber++;
        }
      }

      if (fileProblems.length > 0) {
        nonASCIIFiles.push({
          fileName: fileName,
          problems: fileProblems,
        });
      }
    }

    if (nonASCIIFiles.length === 0) {
      return { valid: true };
    }

    log.debug('Non-ASCII characters detected in files', {
      filesWithIssues: nonASCIIFiles.map(f => ({
        fileName: f.fileName,
        problemCount: f.problems.length,
        sampleProblems: f.problems.slice(0, 5),
      })),
    });

    // Build detailed error message
    let details =
      'Deine Java-Dateien enthalten nicht-ASCII-Zeichen. Bitte verwende nur US-ASCII-Codierung.\n\n';
    details +=
      'Du solltest die Projekt- und Dateicodierung auf US-ASCII einstellen. Andernfalls kann es passieren, dass der Code nicht kompiliert.\n';
    details += 'Schau dir unsere Anweisungen zu Beginn des Semesters an.\n\n';
    details += 'Häufige nicht-ASCII-Zeichen, die vermieden werden sollten:\n';
    details += '• Umlaute: ä, ö, ü, Ä, Ö, Ü, ß\n';
    details += '• Akzentuierte Zeichen: é, è, à, ç, etc.\n';
    details += '• Besondere Anführungszeichen: " " \' \'\n';
    details += '• Em/en-Dash: — –\n\n';
    details += 'Gefundene Probleme:\n\n';

    for (const file of nonASCIIFiles) {
      details += `File: ${file.fileName}\n`;

      // Group similar characters to avoid spam
      const charCounts = {};
      for (const problem of file.problems) {
        const key = `${problem.char} (${problem.hexCode})`;
        if (!charCounts[key]) {
          charCounts[key] = [];
        }
        charCounts[key].push(`Line ${problem.line}, Column ${problem.column}`);
      }

      for (const [charInfo, locations] of Object.entries(charCounts)) {
        details += `  • Character ${charInfo} found at: ${locations.slice(0, 5).join(', ')}`;
        if (locations.length > 5) {
          details += ` and ${locations.length - 5} more locations`;
        }
        details += '\n';
      }
      details += '\n';
    }

    return {
      valid: false,
      details: details,
    };
  } catch (error) {
    log.error('Error validating encoding:', {
      error: error.message,
      fileNames,
    });
    return {
      valid: false,
      details: `Error checking file encoding: ${error.message}`,
    };
  }
}

function validateRequiredFiles(uploadedFileNames, requiredFiles, log = logger) {
  if (!requiredFiles || requiredFiles.length === 0) {
    return { valid: true };
  }

  log.debug('Starting required files validation', {
    uploadedCount: uploadedFileNames.length,
    requiredCount: requiredFiles.length,
  });

  try {
    const missingFiles = [];
    const extraFiles = [];
    const uploadedNames = uploadedFileNames.map(name => name);
    const requiredNames = requiredFiles.map(name => name);

    // Check for missing files
    for (const requiredFile of requiredFiles) {
      if (!uploadedNames.includes(requiredFile)) {
        missingFiles.push(requiredFile);
      }
    }

    // Check for extra files (files that are not required)
    for (const uploadedFile of uploadedFileNames) {
      if (!requiredNames.includes(uploadedFile)) {
        extraFiles.push(uploadedFile);
      }
    }

    // If there are missing files or extra files, return invalid
    if (missingFiles.length === 0 && extraFiles.length === 0) {
      return { valid: true };
    }

    let details = `Diese Übung erfordert das Hochladen spezifischer Dateien.\n\n`;

    if (missingFiles.length > 0) {
      details += `Dir fehlen:\n`;
      missingFiles.forEach(file => {
        details += `• ${file}\n`;
      });
      details += `\n`;
    }

    if (extraFiles.length > 0) {
      details += `Du hast zu viele Dateien hochgeladen. Folgende Dateien sind nicht erlaubt:\n`;
      extraFiles.forEach(file => {
        details += `• ${file}\n`;
      });
      details += `\n`;
    }

    details += `Benötigte Dateien für diese Übung:\n`;
    requiredFiles.forEach(file => {
      const uploaded = uploadedNames.includes(file);
      details += `${uploaded ? '✅' : '❌'} ${file}\n`;
    });

    details += `\nHochgeladene Dateien:\n`;
    uploadedFileNames.forEach(file => {
      const isRequired = requiredNames.includes(file);
      details += `${isRequired ? '✅' : '❌'} ${file}\n`;
    });

    details += `\nBitte lade nur die erforderlichen Dateien mit den genau angegebenen Dateinamen hoch.`;

    return {
      valid: false,
      details: details,
    };
  } catch (error) {
    log.error('Error validating files:', {
      error: error.message,
      stack: error.stack,
    });
    return {
      valid: false,
      details: `Error checking file validation: ${error.message}`,
    };
  }
}

module.exports = {
  validateUSASCIIEncoding,
  validateRequiredFiles,
};

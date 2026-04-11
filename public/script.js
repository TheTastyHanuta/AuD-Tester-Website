// Google Tag Manager/Analytics
(function () {
  var gtagScript = document.createElement('script');
  gtagScript.src = 'https://www.googletagmanager.com/gtag/js?id=G-ZQP9T6G0DJ';
  gtagScript.async = true;
  document.head.appendChild(gtagScript);

  window.dataLayer = window.dataLayer || [];
  function gtag() {
    dataLayer.push(arguments);
  }
  window.gtag = gtag;
  gtag('js', new Date());
  gtag('config', 'G-ZQP9T6G0DJ');
})();

document.addEventListener('DOMContentLoaded', function () {
  // Client-side logger: sends logs to the server
  window.clientLogger = {
    log: function (level, message, meta = {}) {
      fetch('/client-log', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          level,
          message,
          meta,
          url: window.location.href,
        }),
      }).catch(() => {
        /* ignore errors */
      });
    },
    info: function (message, meta) {
      this.log('info', message, meta);
    },
    warn: function (message, meta) {
      this.log('warn', message, meta);
    },
    error: function (message, meta) {
      this.log('error', message, meta);
    },
    debug: function (message, meta) {
      this.log('debug', message, meta);
    },
  };
  const uploadForm = document.getElementById('uploadForm');
  const exerciseSelect = document.getElementById('exercise');
  const fileInput = document.getElementById('javaFiles');
  const fileDisplay = document.querySelector('.file-input-display');
  const fileText = document.querySelector('.file-text');
  const submitBtn = document.getElementById('submitBtn');
  const btnText = document.querySelector('.btn-text');
  const btnSpinner = document.querySelector('.btn-spinner');
  const resultsSection = document.getElementById('results');
  const resultContent = document.getElementById('resultContent');

  // Load available exercises
  loadExercises();

  // Handle file input change
  fileInput.addEventListener('change', function () {
    if (this.files.length > 0) {
      const fileCount = this.files.length;
      if (fileCount === 1) {
        fileText.textContent = this.files[0].name;
      } else {
        fileText.textContent = `${fileCount} Dateien ausgewählt`;
      }
      fileDisplay.classList.add('has-file');

      // Show file list
      updateFileList();
    } else {
      fileText.textContent = 'Java-Dateien auswählen...';
      fileDisplay.classList.remove('has-file');
    }
  });

  // Handle form submission
  uploadForm.addEventListener('submit', function (e) {
    e.preventDefault();
    submitCode();
  });

  async function loadExercises() {
    try {
      const response = await fetch('/exercises');
      const exercises = await response.json();

      exerciseSelect.innerHTML = '<option value="">Übung auswählen...</option>';
      exercises.forEach(exercise => {
        const option = document.createElement('option');
        option.value = exercise.id;

        // Format deadline
        const deadline = new Date(exercise.deadline);
        const deadlineStr = deadline.toLocaleDateString('de-DE', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit',
        });

        option.textContent = `${exercise.name} (${exercise.points} pts) - Deadline: ${deadlineStr}`;
        option.title = `Übung: ${exercise.name} - ${exercise.points} Punkte`;

        exerciseSelect.appendChild(option);
      });
    } catch (error) {
      console.error('Error loading exercises:', error);
      exerciseSelect.innerHTML =
        '<option value="">Fehler beim Laden der Übungen</option>';
      window.clientLogger.error('Error loading exercises', {
        error: error?.message || error,
      });
    }
  }

  async function submitCode() {
    // Validate inputs
    if (!exerciseSelect.value) {
      alert('Bitte wähle eine Übung aus');
      window.clientLogger.warn('No exercise selected on submit');
      return;
    }

    if (!fileInput.files.length) {
      alert('Bitte wähle mindestens eine Java-Datei aus');
      window.clientLogger.warn('No Java file selected on submit');
      return;
    }

    // Check file extensions
    for (let i = 0; i < fileInput.files.length; i++) {
      const fileName = fileInput.files[i].name;
      if (!fileName.toLowerCase().endsWith('.java')) {
        alert(
          `Bitte wähle nur gültige Java-Dateien (.java-Erweiterung) aus. Ungültige Datei: ${fileName}`
        );
        window.clientLogger.warn('Invalid file extension selected', {
          fileName,
        });
        return;
      }
    }

    // Create FormData manually to avoid duplicates
    const formData = new FormData();

    // Add exercise selection
    formData.append('exercise', exerciseSelect.value);

    // Add all files to formData
    for (let i = 0; i < fileInput.files.length; i++) {
      formData.append('javaFiles', fileInput.files[i]);
    }

    // Show loading state
    setLoadingState(true);
    hideResults();

    try {
      const response = await fetch('/submit', {
        method: 'POST',
        body: formData,
      });

      const result = await response.json();
      if (!response.ok) {
        displayResults(result);
        return;
      }

      if (response.status === 202 && result.jobId) {
        renderQueuedJobStatus(result.status, result.message);
        const completedResult = await pollSubmissionStatus(result.jobId);
        displayResults(completedResult);
        return;
      }

      displayResults(result);
    } catch (error) {
      console.error('Error submitting code:', error);
      window.clientLogger.error('Error submitting code', {
        error: error?.message || error,
      });
      displayResults({
        success: false,
        status: '⚠️',
        message: 'Network Error',
        details: `Fehler bei der Kommunikation mit dem Server. Bitte versuche es erneut. 
          Wenn Du die Dateien aus einer ZIP-Datei hochgeladen hast, versuche bitte, die ZIP-Datei zuerst zu entpacken und dann die Dateien hochzuladen.`,
      });
    } finally {
      setLoadingState(false);
    }
  }

  function setLoadingState(loading) {
    submitBtn.disabled = loading;
    if (loading) {
      btnText.style.display = 'none';
      btnSpinner.style.display = 'inline-block';
    } else {
      btnText.style.display = 'inline';
      btnSpinner.style.display = 'none';
    }
  }

  function hideResults() {
    resultsSection.style.display = 'none';
  }

  function delay(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
  }

  function getQueuedJobMessage(status, fallbackMessage) {
    if (status === 'pending') {
      return 'Deine Abgabe wartet auf die Überprüfung.';
    }

    if (status === 'active') {
      return 'Deine Abgabe wird gerade überprüft.';
    }

    return fallbackMessage || 'Deine Abgabe wurde angenommen.';
  }

  function renderQueuedJobStatus(status, message) {
    resultContent.innerHTML = `
            <div class="result-card result-warning">
                <div class="result-status">⏳</div>
                <div class="result-message">
                    <strong>Test Ergebnisse:</strong> ${escapeHtml(getQueuedJobMessage(status, message))}
                </div>
            </div>
        `;
    resultsSection.style.display = 'block';
  }

  async function pollSubmissionStatus(jobId) {
    while (true) {
      await delay(3000);

      const response = await fetch(`/api/status/${encodeURIComponent(jobId)}`);
      const jobStatus = await response.json();

      if (!response.ok) {
        throw new Error(jobStatus.message || 'Job status request failed');
      }

      if (jobStatus.status === 'completed') {
        return jobStatus.result;
      }

      if (jobStatus.status === 'failed') {
        return jobStatus.result;
      }

      renderQueuedJobStatus(jobStatus.status, jobStatus.message);
    }
  }

  function displayResults(result) {
    // Determine result type for styling
    let resultType = 'error';
    if (result.success) {
      resultType = 'success';
    } else if (result.status === '⚠️') {
      resultType = 'warning';
    }

    // After displaying results, reset file input
    fileInput.value = '';
    fileText.textContent = 'Java-Dateien auswählen...';
    fileDisplay.classList.remove('has-file');
    // Remove file list box if present
    const fileList = document.querySelector('.file-list');
    if (fileList) fileList.remove();

    // Format deadline information
    let deadlineInfo = '';
    if (result.deadline) {
      const deadline = new Date(result.deadline);
      const deadlineStr = deadline.toLocaleDateString('de-DE', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
      });

      deadlineInfo = `
                <div class="deadline-info">
                    <strong>Deadline:</strong> ${deadlineStr}
                </div>
            `;
    }
    // Build points information
    let pointsInfo = '';
    if (result.points !== undefined) {
      pointsInfo = `
                <div class="points-info">
                    <strong>Erreichte Punkte:</strong> ${result.points}
                </div>
            `;
    }

    // Build encoding warning
    let encodingWarningHtml = '';
    if (result.encodingWarning) {
      encodingWarningHtml = `
                <div class="result-card result-warning encoding-warning">
                    <div class="result-status">⚠️</div>
                    <div class="result-message">
                        <strong>Encoding-Warnung:</strong> Non-ASCII-Zeichen gefunden
                    </div>
                    <div class="result-details">${escapeHtml(result.encodingWarning)}</div>
                </div>
            `;
    }

    const resultHtml = `
            ${encodingWarningHtml}
            <div class="result-card result-${resultType}">
                <div class="result-status">${result.status || '❓'}</div>
                <div class="result-message">
                    <strong>Test Ergebnisse:</strong> ${escapeHtml(result.message || 'Unknown result')}
                </div>
                ${deadlineInfo}
                ${pointsInfo}
                ${result.details ? `<div class="result-details">${escapeHtml(result.details)}</div>` : ''}
            </div>
        `;

    resultContent.innerHTML = resultHtml;
    resultsSection.style.display = 'block';

    // Scroll to results
    resultsSection.scrollIntoView({ behavior: 'smooth' });
  }

  function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
  }

  function updateFileList() {
    // Remove existing file list if any
    const existingList = document.querySelector('.file-list');
    if (existingList) {
      existingList.remove();
    }

    if (fileInput.files.length > 1) {
      // Create file list display
      const fileList = document.createElement('div');
      fileList.className = 'file-list';

      const listTitle = document.createElement('div');
      listTitle.className = 'file-list-title';
      listTitle.textContent = 'Selected files:';
      fileList.appendChild(listTitle);

      for (let i = 0; i < fileInput.files.length; i++) {
        const fileItem = document.createElement('div');
        fileItem.className = 'file-item';
        fileItem.textContent = `• ${fileInput.files[i].name}`;
        fileList.appendChild(fileItem);
      }

      // Insert after the file input wrapper
      const fileInputWrapper = document.querySelector('.file-input-wrapper');
      fileInputWrapper.parentNode.insertBefore(
        fileList,
        fileInputWrapper.nextSibling
      );
    }
  }

  // Drag and drop functionality
  uploadForm.addEventListener('drop', function (e) {
    e.preventDefault();
    const files = e.dataTransfer.files;

    if (!files || files.length === 0) {
      alert(
        'Es wurden keine Dateien erkannt. Drag&Drop aus ZIP-Archiven wird von Browsern oft nicht unterstützt.'
      );
      window.clientLogger.warn(
        'No files detected in drag&drop (possibly from ZIP)'
      );
      // Reset file input and display
      fileInput.value = '';
      fileText.textContent = 'Java-Dateien auswählen...';
      fileDisplay.classList.remove('has-file');
      const existingList = document.querySelector('.file-list');
      if (existingList) {
        existingList.remove();
      }
      return;
    }

    // Filter for .java files only
    const javaFiles = Array.from(files).filter(file =>
      file.name.toLowerCase().endsWith('.java')
    );

    window.clientLogger.debug('Files dropped in drag&drop', {
      totalFiles: files.length,
      javaFiles: javaFiles.length,
    });

    if (javaFiles.length > 0) {
      // Create a new FileList-like object
      const dt = new DataTransfer();
      javaFiles.forEach(file => dt.items.add(file));
      fileInput.files = dt.files;
      fileInput.dispatchEvent(new Event('change'));
    } else {
      alert('Bitte nur gültige Java-Dateien (.java-Erweiterung) ablegen');
      window.clientLogger.warn('Non-Java file dropped in drag&drop', {
        totalFiles: files.length,
        javaFiles: javaFiles.length,
      });
      // Reset file input and display
      fileInput.value = '';
      fileText.textContent = 'Java-Dateien auswählen...';
      fileDisplay.classList.remove('has-file');
      // Remove file list if present
      const existingList = document.querySelector('.file-list');
      if (existingList) {
        existingList.remove();
      }
    }
  });
});

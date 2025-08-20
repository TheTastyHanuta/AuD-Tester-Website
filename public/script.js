document.addEventListener('DOMContentLoaded', function() {
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
    fileInput.addEventListener('change', function() {
        if (this.files.length > 0) {
            const fileCount = this.files.length;
            if (fileCount === 1) {
                fileText.textContent = this.files[0].name;
            } else {
                fileText.textContent = `${fileCount} files selected`;
            }
            fileDisplay.classList.add('has-file');
            
            // Show file list
            updateFileList();
        } else {
            fileText.textContent = 'Choose Java files...';
            fileDisplay.classList.remove('has-file');
        }
    });

    // Handle form submission
    uploadForm.addEventListener('submit', function(e) {
        e.preventDefault();
        submitCode();
    });

    async function loadExercises() {
        try {
            const response = await fetch('/exercises');
            const exercises = await response.json();
            
            exerciseSelect.innerHTML = '<option value="">Select an exercise...</option>';
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
                    minute: '2-digit'
                });
                
                option.textContent = `${exercise.name} (${exercise.points} pts) - Deadline: ${deadlineStr}`;
                option.title = `Exercise: ${exercise.name} - ${exercise.points} points`;
                
                exerciseSelect.appendChild(option);
            });
        } catch (error) {
            console.error('Error loading exercises:', error);
            exerciseSelect.innerHTML = '<option value="">Error loading exercises</option>';
        }
    }

    async function submitCode() {
        // Validate inputs
        if (!exerciseSelect.value) {
            alert('Please select an exercise');
            return;
        }
        
        if (!fileInput.files.length) {
            alert('Please select at least one Java file');
            return;
        }

        // Check file extensions
        for (let i = 0; i < fileInput.files.length; i++) {
            const fileName = fileInput.files[i].name;
            if (!fileName.toLowerCase().endsWith('.java')) {
                alert(`Please select only valid Java files (.java extension). Invalid file: ${fileName}`);
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
                body: formData
            });

            const result = await response.json();
            displayResults(result);
            
        } catch (error) {
            console.error('Error submitting code:', error);
            displayResults({
                success: false,
                status: '⚠️',
                message: 'Network Error',
                details: 'Failed to connect to the server. Please try again.',
                points: 0
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

    function displayResults(result) {
        // Determine result type for styling
        let resultType = 'error';
        if (result.success) {
            resultType = 'success';
        } else if (result.status === '⚠️') {
            resultType = 'warning';
        }

        // Format deadline information
        let deadlineInfo = '';
        if (result.deadline) {
            const deadline = new Date(result.deadline);
            const deadlineStr = deadline.toLocaleDateString('de-DE', {
                year: 'numeric',
                month: '2-digit',
                day: '2-digit',
                hour: '2-digit',
                minute: '2-digit'
            });
            
            deadlineInfo = `
                <div class="deadline-info">
                    <strong>Deadline:</strong> ${deadlineStr}
                </div>
            `;
        }

        // Build result HTML
        const resultHtml = `
            <div class="result-card result-${resultType}">
                <div class="result-status">${result.status || '❓'}</div>
                <div class="result-message">${escapeHtml(result.message || 'Unknown result')}</div>
                ${deadlineInfo}
                ${result.details ? `<div class="result-details">${escapeHtml(result.details)}</div>` : ''}
                <div class="result-points">Points: ${result.points || 0}</div>
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
            fileInputWrapper.parentNode.insertBefore(fileList, fileInputWrapper.nextSibling);
        }
    }

    // Drag and drop functionality
    const dropZone = document.querySelector('.file-input-display');
    
    dropZone.addEventListener('dragover', function(e) {
        e.preventDefault();
        this.style.borderColor = '#667eea';
        this.style.background = '#f0f4ff';
    });

    dropZone.addEventListener('dragleave', function(e) {
        e.preventDefault();
        if (!fileInput.files.length) {
            this.style.borderColor = '#d1d5db';
            this.style.background = '#f9fafb';
        }
    });

    dropZone.addEventListener('drop', function(e) {
        e.preventDefault();
        const files = e.dataTransfer.files;
        
        // Filter for .java files only
        const javaFiles = Array.from(files).filter(file => 
            file.name.toLowerCase().endsWith('.java')
        );
        
        if (javaFiles.length > 0) {
            // Create a new FileList-like object
            const dt = new DataTransfer();
            javaFiles.forEach(file => dt.items.add(file));
            fileInput.files = dt.files;
            fileInput.dispatchEvent(new Event('change'));
        } else {
            alert('Please drop only valid Java files (.java extension)');
        }
        
        this.style.borderColor = '#d1d5db';
        this.style.background = '#f9fafb';
    });
});

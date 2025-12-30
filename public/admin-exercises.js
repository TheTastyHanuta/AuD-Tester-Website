let exercises = [];

async function loadExercises() {
  try {
    const response = await fetch('/admin/exercises');
    const data = await response.json();
    exercises = data.exercises;
    renderExercises();
    document.getElementById('loading').style.display = 'none';
    document.getElementById('exercisesContainer').style.display = 'block';
  } catch (error) {
    showMessage('Failed to load exercises: ' + error.message, 'error');
    document.getElementById('loading').style.display = 'none';
  }
}

function getDeadlineStatus(deadline) {
  const now = new Date();
  const deadlineDate = new Date(deadline);
  const daysDiff = Math.ceil((deadlineDate - now) / (1000 * 60 * 60 * 24));

  if (daysDiff < 0) {
    return { label: 'Past', class: 'status-past' };
  } else if (daysDiff <= 7) {
    return { label: 'Active', class: 'status-active' };
  } else {
    return { label: 'Upcoming', class: 'status-upcoming' };
  }
}

function formatDateForInput(dateString) {
  const date = new Date(dateString);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  return `${year}-${month}-${day}T${hours}:${minutes}`;
}

function renderExercises() {
  const tbody = document.getElementById('exercisesBody');
  tbody.innerHTML = '';

  exercises.forEach((exercise, index) => {
    const status = getDeadlineStatus(exercise.deadline);
    const row = document.createElement('tr');
    row.innerHTML = `
      <td>
        <div class="exercise-name">${exercise.name}</div>
        <div style="font-size: 12px; color: #94a3b8; margin-top: 4px">ID: ${exercise.id}</div>
      </td>
      <td>
        <span class="status-badge ${status.class}">${status.label}</span>
      </td>
      <td>
        <input 
          type="datetime-local" 
          class="deadline-input" 
          id="deadline-${exercise.id}"
          value="${formatDateForInput(exercise.deadline)}"
        />
      </td>
      <td>${exercise.hasTests ? '✅ Yes' : '❌ No'}</td>
      <td>
        <button 
          class="save-btn" 
          data-exercise-id="${exercise.id}"
          data-exercise-index="${index}"
        >
          💾 Save
        </button>
      </td>
    `;
    tbody.appendChild(row);
  });
}

async function updateDeadline(exerciseId, index, button) {
  const input = document.getElementById(`deadline-${exerciseId}`);
  const newDeadline = new Date(input.value).toISOString();

  button.disabled = true;
  button.textContent = 'Saving...';

  try {
    const response = await fetch(`/admin/exercises/${exerciseId}/deadline`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ deadline: newDeadline }),
    });

    const data = await response.json();

    if (response.ok) {
      exercises[index] = data.exercise;
      showMessage(
        `✅ Deadline for "${data.exercise.name}" updated successfully!`,
        'success'
      );
      renderExercises();
    } else {
      showMessage(`❌ Error: ${data.error}`, 'error');
    }
  } catch (error) {
    showMessage(`❌ Failed to update deadline: ${error.message}`, 'error');
  } finally {
    button.disabled = false;
    button.textContent = '💾 Save';
  }
}

// Event delegation for save buttons
document.addEventListener('click', function (event) {
  if (event.target.classList.contains('save-btn')) {
    const exerciseId = event.target.dataset.exerciseId;
    const index = parseInt(event.target.dataset.exerciseIndex);
    updateDeadline(exerciseId, index, event.target);
  }
});

function showMessage(text, type) {
  const messageEl = document.getElementById('message');
  messageEl.textContent = text;
  messageEl.className = `message ${type}`;
  messageEl.style.display = 'block';

  setTimeout(() => {
    messageEl.style.display = 'none';
  }, 5000);
}

// Load exercises on page load
loadExercises();

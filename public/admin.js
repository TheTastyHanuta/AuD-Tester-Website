const state = {
  csrfToken: '',
  activeTab: 'logs',
  eventSource: null,
  logMode: 'live',
  logLines: [],
  currentLiveName: '',
  currentArchiveName: '',
  exercises: [],
  draftDeadlines: new Map(),
  messageTimer: null,
};

const MAX_LIVE_LINES = 1500;

const els = {
  tabs: Array.from(document.querySelectorAll('.tab-button')),
  panels: Array.from(document.querySelectorAll('.admin-panel')),
  logoutBtn: document.getElementById('logoutBtn'),
  logType: document.getElementById('logType'),
  refreshLogsBtn: document.getElementById('refreshLogsBtn'),
  backToLiveBtn: document.getElementById('backToLiveBtn'),
  fileCount: document.getElementById('fileCount'),
  files: document.getElementById('files'),
  streamStatus: document.getElementById('streamStatus'),
  currentFile: document.getElementById('currentFile'),
  autoscroll: document.getElementById('autoscroll'),
  logSearch: document.getElementById('logSearch'),
  downloadLogBtn: document.getElementById('downloadLogBtn'),
  logOutput: document.getElementById('logOutput'),
  reloadExercisesBtn: document.getElementById('reloadExercisesBtn'),
  exerciseMessage: document.getElementById('exerciseMessage'),
  exerciseLoading: document.getElementById('exerciseLoading'),
  exerciseEditor: document.getElementById('exerciseEditor'),
  exerciseRows: document.getElementById('exerciseRows'),
  dirtyBar: document.getElementById('dirtyBar'),
  dirtyCount: document.getElementById('dirtyCount'),
  discardChangesBtn: document.getElementById('discardChangesBtn'),
  saveChangesBtn: document.getElementById('saveChangesBtn'),
};

function createEl(tag, options = {}) {
  const el = document.createElement(tag);

  if (options.className) {
    el.className = options.className;
  }

  if (options.text !== undefined) {
    el.textContent = options.text;
  }

  if (options.attrs) {
    for (const [name, value] of Object.entries(options.attrs)) {
      el.setAttribute(name, value);
    }
  }

  return el;
}

function setStreamStatus(label, stateName) {
  els.streamStatus.textContent = label;
  els.streamStatus.dataset.state = stateName;
}

function formatSize(bytes) {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / 1024 / 1024).toFixed(1)} MB`;
}

function formatDateTime(value) {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return 'Invalid date';
  return new Intl.DateTimeFormat('en', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(date);
}

function formatInputDate(value) {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return '';

  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  return `${year}-${month}-${day}T${hours}:${minutes}`;
}

function inputDateToIso(value) {
  if (!value) {
    return null;
  }

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return null;
  }

  return date.toISOString();
}

function setTab(tab) {
  state.activeTab = tab;
  for (const button of els.tabs) {
    button.classList.toggle('is-active', button.dataset.tab === tab);
  }
  for (const panel of els.panels) {
    panel.classList.toggle('is-active', panel.dataset.panel === tab);
  }

  const url = new URL(window.location.href);
  url.searchParams.set('tab', tab);
  window.history.replaceState({}, '', url);

  if (tab === 'exercises' && state.exercises.length === 0) {
    loadExercises();
  }

  updateDirtyBar();
}

async function fetchJson(url, options = {}) {
  const response = await fetch(url, options);
  const data = await response.json().catch(() => ({}));

  if (!response.ok) {
    throw new Error(data.error || `Request failed with ${response.status}`);
  }

  return data;
}

async function loadSession() {
  const data = await fetchJson('/admin/session');
  state.csrfToken = data.csrfToken;
}

async function logout() {
  els.logoutBtn.disabled = true;
  try {
    const response = await fetch('/admin/logout', {
      method: 'POST',
      headers: {
        'X-CSRF-Token': state.csrfToken,
      },
    });

    if (!response.ok) {
      throw new Error('Sign out failed');
    }

    window.location.href = '/';
  } catch (error) {
    els.logoutBtn.disabled = false;
    showExerciseMessage(error.message, 'error');
  }
}

function classifyLogLine(line) {
  const explicit = line.match(/\[(error|warn|info|debug)\]/i);
  const level = explicit ? explicit[1].toLowerCase() : '';

  if (level) return level;
  if (/\berror\b|exception|failed/i.test(line)) return 'error';
  if (/\bwarn\b|warning/i.test(line)) return 'warn';
  if (/\bdebug\b/i.test(line)) return 'debug';
  if (/\binfo\b/i.test(line)) return 'info';
  return 'log';
}

function renderLogOutput() {
  const search = els.logSearch.value.trim().toLowerCase();
  const fragment = document.createDocumentFragment();
  const visibleLines = search
    ? state.logLines.filter(entry => entry.text.toLowerCase().includes(search))
    : state.logLines;

  if (visibleLines.length === 0) {
    fragment.appendChild(
      createEl('div', {
        className: 'log-line',
        text: search ? 'No matching log lines.' : 'No log lines loaded yet.',
      })
    );
  } else {
    for (const entry of visibleLines) {
      const row = createEl('div', {
        className: `log-line is-${entry.level}`,
      });
      row.appendChild(
        createEl('span', {
          className: 'log-level',
          text: entry.level,
        })
      );
      row.appendChild(createEl('span', { text: entry.text }));
      fragment.appendChild(row);
    }
  }

  els.logOutput.replaceChildren(fragment);

  if (els.autoscroll.checked) {
    els.logOutput.scrollTop = els.logOutput.scrollHeight;
  }
}

function pushLogLine(text) {
  state.logLines.push({
    text,
    level: classifyLogLine(text),
  });

  if (state.logLines.length > MAX_LIVE_LINES) {
    state.logLines.splice(0, state.logLines.length - MAX_LIVE_LINES);
  }

  renderLogOutput();
}

function resetLogLines() {
  state.logLines = [];
  renderLogOutput();
}

function closeEventSource() {
  if (state.eventSource) {
    state.eventSource.close();
    state.eventSource = null;
  }
}

function startLiveLogs() {
  const type = els.logType.value;

  closeEventSource();
  state.logMode = 'live';
  state.currentLiveName = '';
  state.currentArchiveName = '';
  els.currentFile.textContent = 'Live tail';
  els.downloadLogBtn.disabled = true;
  resetLogLines();
  setStreamStatus('Connecting', 'reconnecting');

  const source = new EventSource(
    `/admin/logs/sse?type=${encodeURIComponent(type)}`
  );
  state.eventSource = source;

  source.onmessage = event => {
    pushLogLine(event.data);
  };

  source.addEventListener('status', event => {
    const payload = JSON.parse(event.data);
    const label = payload.state === 'connected' ? 'Connected' : payload.state;
    setStreamStatus(label, payload.state || 'connected');
    if (payload.file) {
      state.currentLiveName = payload.file;
      els.currentFile.textContent = `Live tail: ${payload.file}`;
      els.downloadLogBtn.disabled = false;
    }
  });

  source.addEventListener('heartbeat', () => {
    if (state.logMode === 'live') {
      setStreamStatus('Connected', 'connected');
    }
  });

  source.onerror = () => {
    if (state.logMode === 'live') {
      setStreamStatus('Reconnecting', 'reconnecting');
    }
  };
}

function renderFileList(files) {
  els.fileCount.textContent = String(files.length);
  const fragment = document.createDocumentFragment();

  if (files.length === 0) {
    fragment.appendChild(
      createEl('li', {
        className: 'file-item',
        text: 'No log files found.',
      })
    );
    els.files.replaceChildren(fragment);
    return;
  }

  for (const file of files) {
    const item = createEl('li', { className: 'file-item' });
    const details = createEl('div');
    details.appendChild(
      createEl('div', { className: 'file-name', text: file.name })
    );
    details.appendChild(
      createEl('div', {
        className: 'file-meta',
        text: `${formatSize(file.size)} · ${formatDateTime(file.mtime)}`,
      })
    );

    const viewButton = createEl('button', {
      className: 'mini-button',
      text: 'View',
      attrs: { type: 'button' },
    });
    viewButton.addEventListener('click', () => viewLogFile(file.name));

    item.append(details, viewButton);
    fragment.appendChild(item);
  }

  els.files.replaceChildren(fragment);
}

async function loadLogFiles() {
  const type = els.logType.value;
  els.refreshLogsBtn.disabled = true;

  try {
    const data = await fetchJson(
      `/admin/logs/list?type=${encodeURIComponent(type)}`
    );
    renderFileList(data.files || []);
  } catch (error) {
    els.fileCount.textContent = '0';
    els.files.replaceChildren(
      createEl('li', {
        className: 'file-item',
        text: error.message,
      })
    );
  } finally {
    els.refreshLogsBtn.disabled = false;
  }
}

function downloadLogFile(name) {
  if (!name) {
    return;
  }

  const type = els.logType.value;
  window.open(
    `/admin/logs/file?type=${encodeURIComponent(type)}&name=${encodeURIComponent(
      name
    )}&raw=1`,
    '_blank'
  );
}

async function viewLogFile(name) {
  const type = els.logType.value;

  closeEventSource();
  state.logMode = 'archive';
  state.currentArchiveName = name;
  els.currentFile.textContent = name;
  els.downloadLogBtn.disabled = false;
  setStreamStatus('Archive', 'idle');
  resetLogLines();
  pushLogLine('Loading archive...');

  try {
    const response = await fetch(
      `/admin/logs/file?type=${encodeURIComponent(type)}&name=${encodeURIComponent(name)}`
    );
    const text = await response.text();

    if (!response.ok) {
      throw new Error(text || `Failed to load ${name}`);
    }

    state.logLines = text
      .split(/\r?\n/)
      .filter(Boolean)
      .map(line => ({
        text: line,
        level: classifyLogLine(line),
      }));
    renderLogOutput();
  } catch (error) {
    state.logLines = [{ text: error.message, level: 'error' }];
    renderLogOutput();
  }
}

function getDeadlineStatus(deadline) {
  const now = new Date();
  const date = new Date(deadline);
  const days = Math.ceil((date - now) / (1000 * 60 * 60 * 24));

  if (Number.isNaN(date.getTime())) {
    return { label: 'Invalid', className: 'status-error' };
  }
  if (days < 0) {
    return { label: 'Past', className: 'status-past' };
  }
  if (days <= 7) {
    return { label: 'Active', className: 'status-active' };
  }
  return { label: 'Upcoming', className: 'status-upcoming' };
}

function getDraftDeadline(exercise) {
  return state.draftDeadlines.get(exercise.id) || exercise.deadline;
}

function getDirtyUpdates() {
  return state.exercises
    .map(exercise => {
      const deadline = getDraftDeadline(exercise);
      return {
        id: exercise.id,
        original: exercise.deadline,
        deadline,
      };
    })
    .filter(update => update.deadline !== update.original);
}

function validateDrafts() {
  const errors = new Map();
  for (const exercise of state.exercises) {
    const deadline = getDraftDeadline(exercise);
    if (!deadline || Number.isNaN(new Date(deadline).getTime())) {
      errors.set(exercise.id, 'Enter a valid deadline.');
    }
  }
  return errors;
}

function updateDirtyBar() {
  const dirtyCount = getDirtyUpdates().length;
  const errors = validateDrafts();
  els.dirtyBar.hidden = state.activeTab !== 'exercises' || dirtyCount === 0;
  els.dirtyCount.textContent =
    dirtyCount === 1 ? '1 unsaved change' : `${dirtyCount} unsaved changes`;
  els.saveChangesBtn.disabled = dirtyCount === 0 || errors.size > 0;
}

function renderExercises() {
  const errors = validateDrafts();
  const fragment = document.createDocumentFragment();

  for (const exercise of state.exercises) {
    const draftDeadline = getDraftDeadline(exercise);
    const status = getDeadlineStatus(draftDeadline);
    const isDirty = draftDeadline !== exercise.deadline;
    const error = errors.get(exercise.id);
    const row = createEl('div', {
      className: `exercise-row${isDirty ? ' is-dirty' : ''}${error ? ' has-error' : ''}`,
    });

    const summary = createEl('div');
    summary.appendChild(
      createEl('div', { className: 'exercise-name', text: exercise.name })
    );
    summary.appendChild(
      createEl('div', {
        className: 'exercise-meta',
        text: `${exercise.id} · ${exercise.points} points`,
      })
    );
    summary.appendChild(
      createEl('div', {
        className: 'required-files',
        text: `Files: ${(exercise.required_files || []).join(', ') || 'None configured'}`,
      })
    );

    const statusCell = createEl('div', { className: 'status-stack' });
    statusCell.appendChild(
      createEl('span', {
        className: `status-badge ${status.className}`,
        text: status.label,
      })
    );
    statusCell.appendChild(
      createEl('span', {
        className: `status-badge ${exercise.hasTests ? 'status-tests' : 'status-none'}`,
        text: exercise.hasTests ? 'Tests' : 'No tests',
      })
    );

    const deadlineCell = createEl('div', { className: 'deadline-cell' });
    const input = createEl('input', {
      attrs: {
        type: 'datetime-local',
        value: formatInputDate(draftDeadline),
        'aria-label': `Deadline for ${exercise.name}`,
      },
    });
    input.addEventListener('change', () => {
      const iso = inputDateToIso(input.value);
      if (iso) {
        state.draftDeadlines.set(exercise.id, iso);
      } else {
        state.draftDeadlines.set(exercise.id, '');
      }
      renderExercises();
    });
    deadlineCell.appendChild(input);
    deadlineCell.appendChild(
      createEl('div', {
        className: 'deadline-help',
        text: error || formatDateTime(draftDeadline),
      })
    );

    row.append(summary, statusCell, deadlineCell);
    fragment.appendChild(row);
  }

  els.exerciseRows.replaceChildren(fragment);
  updateDirtyBar();
}

function showExerciseMessage(text, type) {
  clearTimeout(state.messageTimer);
  els.exerciseMessage.textContent = text;
  els.exerciseMessage.className = `message ${type}`;
  els.exerciseMessage.hidden = false;
  state.messageTimer = setTimeout(() => {
    els.exerciseMessage.hidden = true;
  }, 5000);
}

async function loadExercises() {
  els.exerciseLoading.hidden = false;
  els.exerciseEditor.hidden = true;

  try {
    const data = await fetchJson('/admin/exercises');
    state.exercises = data.exercises || [];
    state.draftDeadlines = new Map();
    renderExercises();
    els.exerciseEditor.hidden = false;
  } catch (error) {
    showExerciseMessage(error.message, 'error');
  } finally {
    els.exerciseLoading.hidden = true;
  }
}

function discardExerciseChanges() {
  state.draftDeadlines = new Map();
  renderExercises();
}

async function saveExerciseChanges() {
  const updates = getDirtyUpdates().map(update => ({
    id: update.id,
    deadline: update.deadline,
  }));

  if (updates.length === 0) {
    return;
  }

  els.saveChangesBtn.disabled = true;
  els.discardChangesBtn.disabled = true;

  try {
    const data = await fetchJson('/admin/exercises/deadlines', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'X-CSRF-Token': state.csrfToken,
      },
      body: JSON.stringify({ updates }),
    });

    state.exercises = data.exercises || [];
    state.draftDeadlines = new Map();
    renderExercises();
    showExerciseMessage('Deadlines saved.', 'success');
  } catch (error) {
    showExerciseMessage(error.message, 'error');
  } finally {
    els.discardChangesBtn.disabled = false;
    updateDirtyBar();
  }
}

function bindEvents() {
  for (const button of els.tabs) {
    button.addEventListener('click', () => setTab(button.dataset.tab));
  }

  els.logoutBtn.addEventListener('click', logout);
  els.logType.addEventListener('change', () => {
    startLiveLogs();
    loadLogFiles();
  });
  els.refreshLogsBtn.addEventListener('click', loadLogFiles);
  els.backToLiveBtn.addEventListener('click', startLiveLogs);
  els.logSearch.addEventListener('input', renderLogOutput);
  els.downloadLogBtn.addEventListener('click', () => {
    const name =
      state.logMode === 'archive'
        ? state.currentArchiveName
        : state.currentLiveName;
    downloadLogFile(name);
  });
  els.reloadExercisesBtn.addEventListener('click', loadExercises);
  els.discardChangesBtn.addEventListener('click', discardExerciseChanges);
  els.saveChangesBtn.addEventListener('click', saveExerciseChanges);
}

async function init() {
  bindEvents();

  try {
    await loadSession();
  } catch (error) {
    window.location.href = '/admin/login?return=/admin';
    return;
  }

  const initialTab = new URLSearchParams(window.location.search).get('tab');
  setTab(initialTab === 'exercises' ? 'exercises' : 'logs');
  startLiveLogs();
  loadLogFiles();
}

init();

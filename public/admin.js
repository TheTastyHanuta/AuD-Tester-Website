const state = {
  csrfToken: '',
  activeTab: 'logs',
  eventSource: null,
  logMode: 'live',
  logLines: [],
  logHeights: [],
  logHeightTree: null,
  logTotalHeight: 0,
  logVersion: 0,
  logRenderPending: false,
  logShouldStickToBottom: false,
  logStickToBottomUntilStable: false,
  liveInitialTailTimer: null,
  expandedLogMeta: new Set(),
  logSearchCache: {
    query: '',
    version: -1,
    indexes: [],
    tree: null,
    totalHeight: 0,
  },
  currentLiveName: '',
  currentArchiveName: '',
  exercises: [],
  draftDeadlines: new Map(),
  messageTimer: null,
};

const LOG_ROW_ESTIMATE = 48;
const LOG_OVERSCAN = 18;
const LOG_HEIGHT_TOLERANCE = 2;
const LOG_LEVELS = new Set(['error', 'warn', 'info', 'debug', 'log']);

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

function splitLogMetadata(message) {
  if (!message.endsWith('}')) {
    return { message, meta: null, metaText: '' };
  }

  let searchFrom = message.length - 1;

  while (searchFrom >= 0) {
    const jsonStart = message.lastIndexOf(' {', searchFrom);

    if (jsonStart === -1) {
      return { message, meta: null, metaText: '' };
    }

    const jsonText = message.slice(jsonStart + 1);

    try {
      const meta = JSON.parse(jsonText);
      return {
        message: message.slice(0, jsonStart).trimEnd(),
        meta,
        metaText: JSON.stringify(meta, null, 2),
      };
    } catch (_) {
      searchFrom = jsonStart - 1;
    }
  }

  return { message, meta: null, metaText: '' };
}

function normalizeLogLevel(level, fallbackLine) {
  const normalized = level.toLowerCase();
  return LOG_LEVELS.has(normalized)
    ? normalized
    : classifyLogLine(fallbackLine);
}

function parseLogLine(line) {
  const match = line.match(
    /^(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}) \[([^\]]+)\]: (.*)$/
  );

  if (!match) {
    const level = classifyLogLine(line);
    return {
      raw: line,
      timestamp: '',
      level,
      message: line,
      meta: null,
      metaText: '',
      searchText: line.toLowerCase(),
    };
  }

  const [, timestamp, rawLevel, rest] = match;
  const { message, meta, metaText } = splitLogMetadata(rest);
  const level = normalizeLogLevel(rawLevel, line);

  return {
    raw: line,
    timestamp,
    level,
    message,
    meta,
    metaText,
    searchText: `${timestamp} ${level} ${message} ${metaText}`.toLowerCase(),
  };
}

function formatMetadataValue(value) {
  if (value === null) {
    return 'null';
  }

  if (typeof value === 'string') {
    return value;
  }

  if (typeof value === 'number' || typeof value === 'boolean') {
    return String(value);
  }

  return JSON.stringify(value, null, 2);
}

function getMetadataSummary(meta) {
  if (!meta || typeof meta !== 'object' || Array.isArray(meta)) {
    return 'Details';
  }

  const keys = Object.keys(meta);
  if (keys.length === 0) {
    return 'Details';
  }

  const visibleKeys = keys.slice(0, 3).join(', ');
  const remaining = keys.length > 3 ? ` +${keys.length - 3}` : '';
  return `Details: ${visibleKeys}${remaining}`;
}

function renderMetadata(meta, fallbackText, sourceIndex) {
  const details = createEl('details', { className: 'log-meta-details' });
  details.open = state.expandedLogMeta.has(sourceIndex);
  details.addEventListener('toggle', () => {
    const wasOpen = state.expandedLogMeta.has(sourceIndex);
    if (details.open === wasOpen) {
      return;
    }

    if (details.open) {
      state.expandedLogMeta.add(sourceIndex);
    } else {
      state.expandedLogMeta.delete(sourceIndex);
    }
    scheduleLogRender();
  });
  details.appendChild(createEl('summary', { text: getMetadataSummary(meta) }));

  if (meta && typeof meta === 'object' && !Array.isArray(meta)) {
    const list = createEl('dl', { className: 'log-meta-list' });
    for (const [key, value] of Object.entries(meta)) {
      list.appendChild(createEl('dt', { text: key }));
      list.appendChild(createEl('dd', { text: formatMetadataValue(value) }));
    }
    details.appendChild(list);
    return details;
  }

  details.appendChild(createEl('pre', { text: fallbackText }));
  return details;
}

function renderLogEntry(entry, sourceIndex) {
  const row = createEl('div', {
    className: `log-line is-${entry.level}`,
  });

  row.appendChild(
    createEl('span', {
      className: 'log-time',
      text: entry.timestamp || 'raw',
    })
  );

  const body = createEl('div', { className: 'log-body' });
  const main = createEl('div', { className: 'log-entry-main' });
  main.appendChild(
    createEl('span', {
      className: 'log-level',
      text: entry.level,
    })
  );
  main.appendChild(
    createEl('span', {
      className: 'log-message',
      text: entry.message || entry.raw,
    })
  );
  body.appendChild(main);

  if (entry.metaText) {
    body.appendChild(renderMetadata(entry.meta, entry.metaText, sourceIndex));
  }

  row.appendChild(body);
  return row;
}

function buildFenwickFromHeights(indexes = null) {
  const count = indexes ? indexes.length : state.logHeights.length;
  const tree = new Array(count + 1).fill(0);
  let totalHeight = 0;

  for (let i = 0; i < count; i += 1) {
    const sourceIndex = indexes ? indexes[i] : i;
    const height = state.logHeights[sourceIndex] || LOG_ROW_ESTIMATE;
    totalHeight += height;
    const treeIndex = i + 1;
    tree[treeIndex] += height;
    const parent = treeIndex + (treeIndex & -treeIndex);
    if (parent <= count) {
      tree[parent] += tree[treeIndex];
    }
  }

  return { tree, totalHeight };
}

function fenwickAdd(tree, index, delta) {
  for (let i = index + 1; i < tree.length; i += i & -i) {
    tree[i] += delta;
  }
}

function fenwickSum(tree, count) {
  let sum = 0;
  for (let i = count; i > 0; i -= i & -i) {
    sum += tree[i];
  }
  return sum;
}

function findVirtualIndexByOffset(tree, offset) {
  if (!tree || tree.length <= 1) {
    return 0;
  }

  let index = 0;
  let bit = 1;
  while (bit < tree.length) {
    bit <<= 1;
  }

  for (let step = bit >> 1; step > 0; step >>= 1) {
    const next = index + step;
    if (next < tree.length && tree[next] <= offset) {
      index = next;
      offset -= tree[next];
    }
  }

  return Math.min(index, tree.length - 2);
}

function ensureLogHeightTree() {
  if (!state.logHeightTree) {
    const { tree, totalHeight } = buildFenwickFromHeights();
    state.logHeightTree = tree;
    state.logTotalHeight = totalHeight;
  }
}

function appendLogHeight(height) {
  state.logHeights.push(height);

  if (state.logHeightTree) {
    const treeIndex = state.logHeights.length;
    const coveredCount = treeIndex & -treeIndex;
    const previousHeight =
      coveredCount > 1
        ? fenwickSum(state.logHeightTree, treeIndex - 1) -
          fenwickSum(state.logHeightTree, treeIndex - coveredCount)
        : 0;
    state.logHeightTree.push(previousHeight + height);
  }

  state.logTotalHeight += height;
}

function clearLogSearchCache() {
  state.logSearchCache = {
    query: '',
    version: -1,
    indexes: [],
    tree: null,
    totalHeight: 0,
  };
}

function ensureLogSearchCache(query) {
  if (
    state.logSearchCache.query === query &&
    state.logSearchCache.version === state.logVersion
  ) {
    return state.logSearchCache;
  }

  const indexes = [];
  for (let i = 0; i < state.logLines.length; i += 1) {
    if (state.logLines[i].searchText.includes(query)) {
      indexes.push(i);
    }
  }

  const { tree, totalHeight } = buildFenwickFromHeights(indexes);
  state.logSearchCache = {
    query,
    version: state.logVersion,
    indexes,
    tree,
    totalHeight,
  };
  return state.logSearchCache;
}

function getVisibleLogModel(search) {
  if (search) {
    const cache = ensureLogSearchCache(search);
    return {
      count: cache.indexes.length,
      tree: cache.tree,
      totalHeight: cache.totalHeight,
      sourceIndexAt: virtualIndex => cache.indexes[virtualIndex],
      cache,
    };
  }

  ensureLogHeightTree();
  return {
    count: state.logLines.length,
    tree: state.logHeightTree,
    totalHeight: state.logTotalHeight,
    sourceIndexAt: virtualIndex => virtualIndex,
    cache: null,
  };
}

function makeLogSpacer(height) {
  const spacer = createEl('div', { className: 'log-virtual-spacer' });
  spacer.style.height = `${Math.max(0, Math.round(height))}px`;
  return spacer;
}

function updateMeasuredLogHeights(model) {
  const rows = els.logOutput.querySelectorAll('[data-log-index]');
  let changed = false;

  for (const row of rows) {
    const sourceIndex = Number(row.dataset.logIndex);
    const virtualIndex = Number(row.dataset.virtualIndex);
    const measuredHeight = row.offsetHeight;
    const previousHeight = state.logHeights[sourceIndex] || LOG_ROW_ESTIMATE;
    const delta = measuredHeight - previousHeight;

    if (Math.abs(delta) <= LOG_HEIGHT_TOLERANCE) {
      continue;
    }

    state.logHeights[sourceIndex] = measuredHeight;
    ensureLogHeightTree();
    fenwickAdd(state.logHeightTree, sourceIndex, delta);
    state.logTotalHeight += delta;

    if (model.cache && model.cache.tree) {
      fenwickAdd(model.cache.tree, virtualIndex, delta);
      model.cache.totalHeight += delta;
    }

    changed = true;
  }

  return changed;
}

function scheduleLogRender({ stickToBottom = false } = {}) {
  state.logShouldStickToBottom = state.logShouldStickToBottom || stickToBottom;

  if (state.logRenderPending) {
    return;
  }

  state.logRenderPending = true;
  window.requestAnimationFrame(() => {
    state.logRenderPending = false;
    renderLogOutput();
  });
}

function getLogScrollAnchor() {
  const row = els.logOutput.querySelector('[data-log-index]');
  if (!row) {
    return null;
  }

  return {
    sourceIndex: row.dataset.logIndex,
    offset: row.offsetTop - els.logOutput.scrollTop,
  };
}

function restoreLogScrollAnchor(anchor) {
  if (
    !anchor ||
    state.logShouldStickToBottom ||
    state.logStickToBottomUntilStable
  ) {
    return;
  }

  const row = els.logOutput.querySelector(
    `[data-log-index="${anchor.sourceIndex}"]`
  );
  if (row) {
    els.logOutput.scrollTop = Math.max(0, row.offsetTop - anchor.offset);
  }
}

function renderLogOutput() {
  const search = els.logSearch.value.trim().toLowerCase();
  const fragment = document.createDocumentFragment();
  const model = getVisibleLogModel(search);
  const anchor = getLogScrollAnchor();
  const shouldStickToBottom =
    state.logShouldStickToBottom || state.logStickToBottomUntilStable;

  if (model.count === 0) {
    fragment.appendChild(
      createEl('div', {
        className: 'log-line log-line-empty',
        text: search ? 'No matching log lines.' : 'No log lines loaded yet.',
      })
    );
  } else {
    const viewportHeight = els.logOutput.clientHeight || 520;
    const scrollTop = els.logOutput.scrollTop;
    const start = Math.max(
      0,
      findVirtualIndexByOffset(model.tree, scrollTop) - LOG_OVERSCAN
    );
    const end = Math.min(
      model.count,
      findVirtualIndexByOffset(model.tree, scrollTop + viewportHeight) +
        LOG_OVERSCAN +
        1
    );
    const topHeight = fenwickSum(model.tree, start);
    const renderedHeight = fenwickSum(model.tree, end) - topHeight;

    fragment.appendChild(makeLogSpacer(topHeight));
    for (let virtualIndex = start; virtualIndex < end; virtualIndex += 1) {
      const sourceIndex = model.sourceIndexAt(virtualIndex);
      const row = renderLogEntry(state.logLines[sourceIndex], sourceIndex);
      row.dataset.logIndex = String(sourceIndex);
      row.dataset.virtualIndex = String(virtualIndex);
      fragment.appendChild(row);
    }
    fragment.appendChild(
      makeLogSpacer(model.totalHeight - topHeight - renderedHeight)
    );
  }

  els.logOutput.replaceChildren(fragment);

  if (shouldStickToBottom) {
    els.logOutput.scrollTop = els.logOutput.scrollHeight;
    state.logShouldStickToBottom = false;
  } else {
    restoreLogScrollAnchor(anchor);
  }

  const heightsChanged = model.count > 0 && updateMeasuredLogHeights(model);

  if (heightsChanged) {
    if (state.logStickToBottomUntilStable) {
      scheduleLogRender({ stickToBottom: true });
    } else {
      scheduleLogRender();
    }
    return;
  }

  if (state.logStickToBottomUntilStable) {
    state.logStickToBottomUntilStable = false;
    scheduleLogRender();
    return;
  }

  if (shouldStickToBottom) {
    scheduleLogRender();
  }
}

function pushLogLine(text) {
  state.logLines.push(parseLogLine(text));
  appendLogHeight(LOG_ROW_ESTIMATE);
  state.logVersion += 1;
  clearLogSearchCache();
  scheduleLogRender({
    stickToBottom: els.autoscroll.checked || state.logStickToBottomUntilStable,
  });
}

function setLogLines(entries, { stickToBottom = false } = {}) {
  state.logLines = entries;
  state.logHeights = new Array(entries.length).fill(LOG_ROW_ESTIMATE);
  state.logHeightTree = null;
  state.logTotalHeight = entries.length * LOG_ROW_ESTIMATE;
  state.logVersion += 1;
  state.logShouldStickToBottom = stickToBottom;
  state.logStickToBottomUntilStable = stickToBottom;
  state.expandedLogMeta.clear();
  clearLogSearchCache();
  scheduleLogRender({ stickToBottom });
}

function resetLogLines({ render = true } = {}) {
  state.logLines = [];
  state.logHeights = [];
  state.logHeightTree = null;
  state.logTotalHeight = 0;
  state.logVersion += 1;
  state.logShouldStickToBottom = false;
  state.logStickToBottomUntilStable = false;
  clearTimeout(state.liveInitialTailTimer);
  state.liveInitialTailTimer = null;
  state.expandedLogMeta.clear();
  clearLogSearchCache();
  if (render) {
    scheduleLogRender();
  }
}

function closeEventSource() {
  if (state.eventSource) {
    state.eventSource.close();
    state.eventSource = null;
  }
  clearTimeout(state.liveInitialTailTimer);
  state.liveInitialTailTimer = null;
}

function extendLiveInitialTail() {
  if (
    state.logMode !== 'live' ||
    !state.currentLiveName ||
    !els.autoscroll.checked
  ) {
    return;
  }

  state.logStickToBottomUntilStable = true;
  clearTimeout(state.liveInitialTailTimer);
  state.liveInitialTailTimer = setTimeout(() => {
    state.liveInitialTailTimer = null;
    state.logStickToBottomUntilStable = false;
    if (els.autoscroll.checked) {
      scheduleLogRender({ stickToBottom: true });
    }
  }, 350);
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
    extendLiveInitialTail();
    pushLogLine(event.data);
  };

  source.addEventListener('status', event => {
    const payload = JSON.parse(event.data);
    const label =
      payload.state === 'connected'
        ? 'Connected'
        : payload.state === 'waiting'
          ? 'Waiting'
          : payload.state;
    setStreamStatus(label, payload.state || 'connected');
    if (payload.reset) {
      resetLogLines({ render: false });
    }
    if (payload.file) {
      state.currentLiveName = payload.file;
      els.currentFile.textContent = `Live tail: ${payload.file}`;
      els.downloadLogBtn.disabled = false;
      if (els.autoscroll.checked) {
        extendLiveInitialTail();
      }
    } else if (payload.message) {
      els.currentFile.textContent = payload.message;
    }
    scheduleLogRender({ stickToBottom: els.autoscroll.checked });
  });

  source.addEventListener('heartbeat', () => {
    if (state.logMode === 'live') {
      if (state.currentLiveName) {
        setStreamStatus('Connected', 'connected');
      } else {
        setStreamStatus('Waiting', 'waiting');
      }
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

    setLogLines(
      text
        .split(/\r?\n/)
        .filter(Boolean)
        .map(line => parseLogLine(line)),
      { stickToBottom: true }
    );
  } catch (error) {
    setLogLines([parseLogLine(error.message)]);
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
  els.logSearch.addEventListener('input', () => {
    clearLogSearchCache();
    els.logOutput.scrollTop = 0;
    scheduleLogRender();
  });
  els.logOutput.addEventListener('scroll', () => scheduleLogRender());
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

const filesEl = document.getElementById('files');
const fileCountEl = document.getElementById('fileCount');
const logOutput = document.getElementById('logOutput');
const logTypeSel = document.getElementById('logType');
const currentFileEl = document.getElementById('currentFile');
const autoscrollEl = document.getElementById('autoscroll');
const refreshBtn = document.getElementById('refreshList');

let evtSource = null;

function formatSize(bytes) {
  if (bytes < 1024) return bytes + ' B';
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
  return (bytes / 1024 / 1024).toFixed(1) + ' MB';
}

async function loadFiles() {
  const type = logTypeSel.value;
  const res = await fetch(`/admin/logs/list?type=${encodeURIComponent(type)}`);
  if (!res.ok) {
    filesEl.innerHTML = '<li>Failed to load files</li>';
    return;
  }
  const data = await res.json();
  fileCountEl.textContent = String(data.files.length);
  filesEl.innerHTML = '';
  for (const f of data.files) {
    const li = document.createElement('li');
    li.className = 'file-item';
    const name = document.createElement('div');
    name.className = 'file-name';
    name.textContent = `${f.name} (${formatSize(f.size)})`;
    const actions = document.createElement('div');
    actions.className = 'file-actions';
    const viewBtn = document.createElement('button');
    viewBtn.textContent = 'Ansehen';
    viewBtn.onclick = () => viewFile(f.name);
    const dlBtn = document.createElement('button');
    dlBtn.textContent = 'Download';
    dlBtn.onclick = () =>
      window.open(
        `/admin/logs/file?type=${encodeURIComponent(type)}&name=${encodeURIComponent(f.name)}&raw=1`,
        '_blank'
      );
    actions.appendChild(viewBtn);
    actions.appendChild(dlBtn);
    li.appendChild(name);
    li.appendChild(actions);
    filesEl.appendChild(li);
  }
}

function startSSE() {
  const type = logTypeSel.value;
  if (evtSource) evtSource.close();
  logOutput.textContent = '';
  currentFileEl.textContent = 'Live tail';
  evtSource = new EventSource(
    `/admin/logs/sse?type=${encodeURIComponent(type)}`
  );
  evtSource.onmessage = ev => {
    logOutput.textContent += ev.data + '\n';
    if (autoscrollEl.checked) {
      // Append line and scroll
      logOutput.scrollTop = logOutput.scrollHeight;
    }
  };
  evtSource.onerror = () => {
    // Do not clear or restart manually. EventSource auto-reconnects.
    // Keeping the content avoids jumping to top/end on transient drops.
  };
}

async function viewFile(name) {
  const type = logTypeSel.value;
  if (evtSource) {
    evtSource.close();
    evtSource = null;
  }
  currentFileEl.textContent = name;
  logOutput.textContent = 'Lade...';
  const res = await fetch(
    `/admin/logs/file?type=${encodeURIComponent(type)}&name=${encodeURIComponent(name)}`
  );
  if (!res.ok) {
    logOutput.textContent = 'Failed to load file';
    return;
  }
  const text = await res.text();
  logOutput.textContent = text;
  if (autoscrollEl.checked) logOutput.scrollTop = logOutput.scrollHeight;
}

logTypeSel.addEventListener('change', () => {
  startSSE();
  loadFiles();
});
refreshBtn.addEventListener('click', () => loadFiles());

// init
startSSE();
loadFiles();

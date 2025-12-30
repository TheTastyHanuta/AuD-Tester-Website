const params = new URLSearchParams(location.search);
const ret = params.get('return') || '/admin/logs';
document.getElementById('returnInput').value = ret;

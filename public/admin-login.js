const params = new URLSearchParams(location.search);
const ret = params.get('return') || '/admin';
document.getElementById('returnInput').value = ret;

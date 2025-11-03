(function(){
  const bell = document.getElementById('bell');
  if (!bell) return;

  const dot = document.getElementById('dot');
  const pop = document.getElementById('popover');
  const list = document.getElementById('popList');
  const readAllBtn = document.getElementById('readAllBtn');

  async function fetchJson(url, init){
    const res = await fetch(url, {
      headers: { 'X-Requested-With': 'XMLHttpRequest' },
      ...init
    });
    if (res.status === 401 || res.status === 403) throw new Error('AUTH');
    let data = null;
    try { data = await res.json(); } catch(e){}
    if (!res.ok) throw new Error('HTTP ' + res.status);
    return data;
  }

  async function load(){
    list.textContent = '알림을 불러오는 중…';
    try {
      const data = await fetchJson('/api/notifications');
      const items = data.items || [];
      const unread = items.filter(n => !n.readAt);
      if (dot) dot.style.display = unread.length > 0 ? 'block' : 'none';

      if (items.length === 0) {
        list.textContent = '알림이 없습니다.';
        return;
      }

      list.innerHTML = '';
      items.forEach(n => {
        const div = document.createElement('div');
        div.className = 'nitem' + (n.readAt ? ' read' : '');
        const created = (n.createdAt || '').toString().replace('T',' ');
        div.innerHTML = `
          <div class="meta">${n.type} · ${created}</div>
          <div style="font-weight:700; margin:6px 0 4px 0;">${n.title || '-'}</div>
          <div>${n.message || ''}</div>
        `;
        list.appendChild(div);
      });
    } catch(e) {
      if (e.message === 'AUTH') {
        if (dot) dot.style.display = 'none';
        list.textContent = '알림을 보려면 로그인하세요.';
      } else {
        list.textContent = '알림을 불러오지 못했습니다.';
      }
    }
  }

  bell.addEventListener('click', async (e) => {
    e.stopPropagation();
    const willOpen = pop.style.display !== 'block';
    if (willOpen) {
      await load();
      pop.style.display = 'block';
      try {
        await fetch('/api/notifications/read-all', {
          method:'POST',
          headers: { 'X-Requested-With': 'XMLHttpRequest' }
        });
        if (dot) dot.style.display = 'none';
        Array.from(document.querySelectorAll('.nitem')).forEach(n => n.classList.add('read'));
      } catch(e) {}
    } else {
      pop.style.display = 'none';
    }
  });

  document.addEventListener('click', () => { pop.style.display = 'none'; });
  pop.addEventListener('click', (e) => e.stopPropagation());

  (async () => {
    try {
      const data = await fetchJson('/api/notifications');
      const unread = (data.items||[]).filter(n => !n.readAt);
      if (dot) dot.style.display = unread.length > 0 ? 'block' : 'none';
    } catch(e){}
  })();
})();

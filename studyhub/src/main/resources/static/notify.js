(function(){
  const bell = document.getElementById('bell');
  if (!bell) return;

  const dot = document.getElementById('dot');
  const pop = document.getElementById('popover');
  const list = document.getElementById('popList');
  const readAllBtn = document.getElementById('readAllBtn');

  async function fetchJson(url, init){
    const res = await fetch(url, {
      headers: { 'X-Requested-With': 'XMLHttpRequest' }, // ✔ AJAX 표시(안전장치)
      ...init
    });
    // 미로그인/권한없음은 조용히 무시
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
      // 미로그인 등: 배지 끄고, 메시지는 간단히
      if (e.message === 'AUTH') {
        if (dot) dot.style.display = 'none';
        list.textContent = '알림을 보려면 로그인하세요.';
      } else {
        list.textContent = '알림을 불러오지 못했습니다.';
      }
    }
  }

  // 팝오버 토글
  bell.addEventListener('click', async (e) => {
    e.stopPropagation();
    const willOpen = pop.style.display !== 'block';
    if (willOpen) {
      await load();
      pop.style.display = 'block';
      // 열리면 모두 읽음 처리
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

  // 바깥 클릭 시 닫기
  document.addEventListener('click', () => { pop.style.display = 'none'; });
  pop.addEventListener('click', (e) => e.stopPropagation());

  // 최초 배지 상태만 미리 확인(실패 시 무시)
  (async () => {
    try {
      const data = await fetchJson('/api/notifications');
      const unread = (data.items||[]).filter(n => !n.readAt);
      if (dot) dot.style.display = unread.length > 0 ? 'block' : 'none';
    } catch(e){}
  })();
})();

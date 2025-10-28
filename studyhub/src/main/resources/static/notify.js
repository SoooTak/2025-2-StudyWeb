(function(){
  const bell = document.getElementById('bell');
  if (!bell) return;

  const dot = document.getElementById('dot');
  const pop = document.getElementById('popover');
  const list = document.getElementById('popList');
  const readAllBtn = document.getElementById('readAllBtn');

  async function fetchJson(url, init){
    const res = await fetch(url, init);
    let data = null;
    try { data = await res.json(); } catch(e){}
    if (!res.ok) throw new Error('HTTP ' + res.status);
    return data;
  }

  async function load(){
    list.textContent = '불러오는 중…';
    try {
      const data = await fetchJson('/api/notifications');
      const items = data.items || [];
      const unread = items.filter(n => !n.readAt);
      dot.style.display = unread.length > 0 ? 'block' : 'none';

      if (items.length === 0) {
        list.textContent = '알림이 없습니다';
        return;
      }

      list.innerHTML = '';
      items.forEach(n => {
        const div = document.createElement('div');
        div.className = 'nitem' + (n.readAt ? ' read' : '');
        div.innerHTML = `
          <div class="meta">${n.type} · ${(n.createdAt||'').toString().replace('T',' ')}</div>
          <div style="font-weight:700; margin:6px 0 4px 0;">${n.title || '-'}</div>
          <div>${n.message || ''}</div>
        `;
        list.appendChild(div);
      });
    } catch(e) {
      list.textContent = '알림을 불러올 수 없습니다';
    }
  }

  // 팝오버 토글
  bell.addEventListener('click', async (e) => {
    e.stopPropagation();
    const willOpen = pop.style.display !== 'block';
    // 열기 직전 로드
    if (willOpen) {
      await load();
      pop.style.display = 'block';
      // 열면 읽음 처리
      try {
        await fetch('/api/notifications/read-all', { method:'POST' });
        dot.style.display = 'none';
        // 화면도 읽음 스타일로
        Array.from(document.querySelectorAll('.nitem')).forEach(n => n.classList.add('read'));
      } catch(e) {}
    } else {
      pop.style.display = 'none';
    }
  });

  // 외부 클릭 닫기
  document.addEventListener('click', () => {
    pop.style.display = 'none';
  });
  pop.addEventListener('click', (e) => e.stopPropagation());

  // 버튼: 모두 읽음
  if (readAllBtn) {
    readAllBtn.addEventListener('click', async () => {
      try {
        await fetch('/api/notifications/read-all', { method:'POST' });
        dot.style.display = 'none';
        await load();
      } catch(e) {}
    });
  }

  // 페이지 진입 시 한번 미리 카운트만 반영
  (async () => {
    try {
      const data = await fetchJson('/api/notifications');
      const unread = (data.items||[]).filter(n => !n.readAt);
      dot.style.display = unread.length > 0 ? 'block' : 'none';
    } catch(e){}
  })();
})();

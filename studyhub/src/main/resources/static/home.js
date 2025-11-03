(function(){
  // --- 공통 유틸 ---
  async function fetchJson(url, init){
    const res = await fetch(url, {
      headers: { 'X-Requested-With': 'XMLHttpRequest' },
      credentials: 'same-origin',
      ...(init || {})
    });
    if(!res.ok) throw new Error('HTTP ' + res.status);
    try { return await res.json(); } catch { return {}; }
  }
  function el(sel, root=document){ return root.querySelector(sel); }
  function clear(node){ while(node && node.firstChild) node.removeChild(node.firstChild); }

  // ===== 상태 정규화: applyDeadline이 지났으면 CLOSED 취급 =====
  function normalizeStatus(item){
    const raw = (item.status || '').toString().toUpperCase();
    const dl  = item.applyDeadline || item.apply_deadline || null;
    if (dl){
      const now = Date.now();
      const deadlineTs = new Date(dl).getTime();
      if (!isNaN(deadlineTs) && deadlineTs < now) return 'CLOSED';
    }
    return raw || 'OPEN';
  }

  // ==============================
  // 내 스터디: 하나의 리스트로 통합 렌더
  // ==============================
  async function loadMyStudies(){
    const loading = el('#myStudiesLoading');
    const box     = el('#myStudiesContent');
    if(!box) return; // 비회원이면 없음

    try{
      const data = await fetchJson('/api/mystudies'); // {leader, member, pending}
      renderMyStudiesUnified(box, data);
      if(loading) loading.style.display = 'none';
      box.style.display = '';
    }catch(e){
      if(loading) loading.textContent = '내 스터디를 불러오지 못했습니다.';
      if(box) box.style.display = 'none';
    }
  }

  function renderMyStudiesUnified(root, payload){
    clear(root);

    const leader  = (payload.leader  || []).map(s => ({...s, _role: 'LEADER'}));
    const member  = (payload.member  || []).map(s => ({...s, _role: 'MEMBER'}));
    const pending = (payload.pending || []).map(s => ({...s, _role: 'PENDING'}));
    const all = [...leader, ...member, ...pending];

    if(all.length === 0){
      const ghost = document.createElement('div');
      ghost.className = 'sh-card sh-card--ghost';
      ghost.textContent = '참여중인 스터디가 없습니다.';
      root.appendChild(ghost);
      return;
    }

    all.sort((a,b) => (b.id||0) - (a.id||0));
    all.forEach(st => root.appendChild(myStudyRowUnified(st)));
  }

  function myStudyRowUnified(st){
    const row = document.createElement('div');
    row.className = 'sh-mylist__item';

    const left = document.createElement('div');
    const name = document.createElement('a');
    name.className = 'sh-link';
    name.href = '/explore#' + (st.id ?? '');
    name.textContent = st.title ?? '(제목 없음)';

    const meta = document.createElement('span');
    meta.className = 'sh-muted';
    meta.style.marginLeft = '8px';
    const displayStatus = normalizeStatus(st);
    meta.textContent = [
      st.category || '',
      displayStatus || '',
      (st.memberCount != null ? `현재 ${st.memberCount}` : ''),
      (st._role === 'PENDING' ? '신청 대기중' : '')
    ].filter(Boolean).join(' · ');

    left.appendChild(name);
    left.appendChild(meta);

    const right = document.createElement('div');

    if(st._role === 'PENDING'){
      const wait = document.createElement('button');
      wait.className = 'sh-btn';
      wait.disabled = true;
      wait.textContent = '승인 대기';
      right.appendChild(wait);

      const cancel = document.createElement('button');
      cancel.className = 'sh-btn';
      cancel.style.marginLeft = '6px';
      cancel.textContent = '신청 취소';
      cancel.addEventListener('click', (e) => {
        e.preventDefault();
        alert('신청 취소 연동은 다음 단계에서 구현됩니다.');
      });
      right.appendChild(cancel);
    } else {
      const go = document.createElement('a');
      go.className = 'sh-btn sh-btn--primary';
      // 스터디룸 페이지가 생기면 '/studies/'+id 로 교체
      go.href = '/explore#' + (st.id ?? '');
      go.textContent = '스터디룸 이동';
      right.appendChild(go);
    }

    row.appendChild(left);
    row.appendChild(right);
    return row;
  }

  // ==============================
  // 캐러셀 (3개씩 페이지 이동)
  // ==============================
  const carousels = {
    latest:   { key:'latest',   api:'/api/studies?sort=latest',   items:[], page:0, pageSize:3, trackId:'latestTrack',   rootId:'latestCarousel' },
    deadline: { key:'deadline', api:'/api/studies?sort=deadline', items:[], page:0, pageSize:3, trackId:'deadlineTrack', rootId:'deadlineCarousel' }
  };

  function updateArrows(c){
    const root = el('#' + c.rootId);
    const prev = el('.sh-car-prev', root);
    const next = el('.sh-car-next', root);
    const total = c.items.length;
    const maxPage = Math.max(0, Math.ceil(total / c.pageSize) - 1);
    if(prev) prev.disabled = (c.page <= 0);
    if(next) next.disabled = (c.page >= maxPage);
    if(root) root.classList.toggle('sh-car--nooverflow', total <= c.pageSize);
  }

  function renderPage(c){
    const track = el('#' + c.trackId);
    if(!track) return;
    clear(track);
    const start = c.page * c.pageSize;
    const slice = (c.items || []).slice(start, start + c.pageSize);
    if (slice.length === 0 && c.items.length === 0){
      const empty = document.createElement('div');
      empty.className = 'sh-empty';
      empty.textContent = '스터디가 없습니다.';
      track.appendChild(empty);
    } else {
      slice.forEach(it => track.appendChild(buildCard(it)));
      for (let i = slice.length; i < c.pageSize; i++){
        track.appendChild(buildGhost());
      }
    }
    updateArrows(c);
  }

  async function loadCarousel(c){
    try{
      const data = await fetchJson(c.api);
      let items = (data.items ?? data ?? []);

      // ✅ 마감 임박: apply_deadline이 지난 항목(=정규화 상태 CLOSED) 제거
      if (c.key === 'deadline') {
        items = items.filter(it => normalizeStatus(it) !== 'CLOSED');
      }

      c.items = items;
      c.page = 0;
    } catch(e){
      c.items = []; c.page = 0;
    } finally {
      renderPage(c);
    }
  }

  function buildCard(item){
    const id  = item.id ?? '';
    const theTitle = item.title ?? '제목 없음';
    const cat = item.category ?? '';
    const stat = normalizeStatus(item); // ✅ 정규화 상태 사용
    const img = item.imageUrl ?? item.image_url ?? null;

    const card = document.createElement('a');
    card.className = 'study-card';
    card.href = '/explore#' + id;

    const thumb = document.createElement('div');
    thumb.className = 'study-thumb';
    if(img){ thumb.style.backgroundImage = `url('${img}')`; }
    else { thumb.classList.add('is-empty'); thumb.textContent = 'No Image'; }

    const body = document.createElement('div');
    body.className = 'study-body';

    const title = document.createElement('div');
    title.className = 'study-title';
    title.textContent = theTitle;

    const meta = document.createElement('div');
    meta.className = 'study-meta';
    if (cat){
      const b = document.createElement('span');
      b.className = 'badge';
      b.textContent = cat;
      meta.appendChild(b);
    }
    if (stat){
      const s = document.createElement('span');
      s.className = 'badge badge--muted';
      s.textContent = stat;
      meta.appendChild(s);
    }

    body.appendChild(title);
    body.appendChild(meta);
    card.appendChild(thumb);
    card.appendChild(body);
    return card;
  }

  function buildGhost(){
    const g = document.createElement('div');
    g.className = 'sh-card sh-card--ghost';
    g.textContent = ' ';
    return g;
  }

  function bindCarousel(key){
    const c = carousels[key];
    const root = el('#' + c.rootId);
    if(!root) return;
    el('.sh-car-prev', root).addEventListener('click', ()=>{ if(c.page>0){ c.page--; renderPage(c);} });
    el('.sh-car-next', root).addEventListener('click', ()=>{ 
      const maxPage = Math.max(0, Math.ceil(c.items.length / c.pageSize) - 1);
      if(c.page < maxPage){ c.page++; renderPage(c);} 
    });
    loadCarousel(c);
    window.addEventListener('resize', ()=> updateArrows(c));
  }

  // 초기 실행
  document.addEventListener('DOMContentLoaded', () => {
    loadMyStudies();
    bindCarousel('latest');
    bindCarousel('deadline');
  });
})();

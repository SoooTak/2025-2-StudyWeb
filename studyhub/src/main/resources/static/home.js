// home.js
// - 홈 캐러셀: 3장 고정 페이지 이동(기존 유지)
// - "내 스터디" 섹션: /api/mystudies 연동 (로그인 유도/로딩/빈 상태 처리 포함)

(function () {
  const $ = (sel, ctx = document) => ctx.querySelector(sel);
  const $$ = (sel, ctx = document) => Array.from(ctx.querySelectorAll(sel));

  document.addEventListener('DOMContentLoaded', () => {
    initMyStudies();
    initCarousel();
    initTabs();
  });

  /* ===================== 내 스터디 ===================== */

  async function initMyStudies() {
    const loginHint = $('#my-studies-login-hint');
    const loading = $('#my-studies-loading');
    const content = $('#my-studies-content');

    try {
      const res = await fetch('/api/mystudies', {
        headers: { 'X-Requested-With': 'XMLHttpRequest' },
        credentials: 'same-origin'
      });

      if (res.status === 401) {
        // 미로그인: 조용히 로그인 유도
        loading.hidden = true;
        loginHint.hidden = false;
        content.hidden = true;
        return;
      }

      if (!res.ok) {
        throw new Error(`내 스터디 로드 실패 (${res.status})`);
      }

      const data = await res.json();
      renderMyStudies(data);
      loginHint.hidden = true;
      content.hidden = false;
    } catch (e) {
      console.error(e);
      // 실패시에도 화면이 비지 않도록 기본 메시지
      renderMyStudies({ leader: [], member: [], pending: [] });
      content.hidden = false;
    } finally {
      loading.hidden = true;
    }
  }

  function renderMyStudies(payload) {
    const leader = Array.isArray(payload?.leader) ? payload.leader : [];
    const member = Array.isArray(payload?.member) ? payload.member : [];
    const pending = Array.isArray(payload?.pending) ? payload.pending : [];

    fillGrid($('#grid-leader'), leader);
    fillGrid($('#grid-member'), member);
    fillGrid($('#grid-pending'), pending);
  }

  function fillGrid(gridEl, items) {
    gridEl.innerHTML = '';
    if (!items || items.length === 0) {
      const empty = document.createElement('div');
      empty.className = 'empty';
      empty.textContent = gridEl.dataset.emptyText || '데이터가 없습니다.';
      gridEl.appendChild(empty);
      return;
    }

    items.forEach(item => {
      gridEl.appendChild(studyCard(item));
    });
  }

  function studyCard(item) {
    const el = document.createElement('a');
    el.className = 'card';
    el.href = `/explore?id=${encodeURIComponent(item.id)}`; // 상세 경로 확정 전 임시 링크

    const thumb = document.createElement('div');
    thumb.className = 'card__thumb';
    if (item.imageUrl) {
      const img = document.createElement('img');
      img.src = item.imageUrl;
      img.alt = item.title || 'study';
      thumb.appendChild(img);
    } else {
      thumb.classList.add('card__thumb--placeholder');
      thumb.textContent = 'No Image';
    }

    const body = document.createElement('div');
    body.className = 'card__body';

    const title = document.createElement('h3');
    title.className = 'card__title';
    title.textContent = item.title || '(제목 없음)';

    const meta = document.createElement('div');
    meta.className = 'card__meta';
    const category = safeText(item.category);
    const status = safeText(item.status);
    const cap = (item.capacity != null) ? `정원 ${item.capacity}` : '';
    const mem = (item.memberCount != null) ? `현재 ${item.memberCount}` : '';
    meta.textContent = [category, status, cap, mem].filter(Boolean).join(' · ');

    const foot = document.createElement('div');
    foot.className = 'card__foot';
    if (item.applyDeadline) {
      const dd = new Date(item.applyDeadline);
      const span = document.createElement('span');
      span.className = 'badge';
      span.textContent = `마감 ${formatYMD(dd)}`;
      foot.appendChild(span);
    }

    body.appendChild(title);
    body.appendChild(meta);
    el.appendChild(thumb);
    el.appendChild(body);
    el.appendChild(foot);
    return el;
  }

  function safeText(v) {
    return (v === undefined || v === null) ? '' : String(v);
  }
  function pad2(n) { return String(n).padStart(2, '0'); }
  function formatYMD(d) {
    return `${d.getFullYear()}-${pad2(d.getMonth() + 1)}-${pad2(d.getDate())}`;
  }

  /* ===================== 탭 ===================== */

  function initTabs() {
    const navBtns = $$('.tabs__btn');
    const panes = $$('.tabs__pane');
    navBtns.forEach(btn => {
      btn.addEventListener('click', () => {
        const tab = btn.dataset.tab;
        navBtns.forEach(b => b.classList.toggle('is-active', b === btn));
        panes.forEach(p => p.classList.toggle('is-active', p.id === `tab-${tab}`));
      });
    });
  }

  /* ===================== 캐러셀 (기존) ===================== */

  function initCarousel() {
    const viewport = $('#carousel-viewport');
    const prevBtn = $('.carousel .prev');
    const nextBtn = $('.carousel .next');
    const emptyEl = $('#carousel-empty');
    const pageSize = parseInt($('.carousel').dataset.pageSize || '3', 10);

    // 서버 API가 이미 존재한다고 가정: /api/studies?sort=latest
    fetch('/api/studies?sort=latest', {
      headers: { 'X-Requested-With': 'XMLHttpRequest' },
      credentials: 'same-origin'
    })
      .then(r => r.ok ? r.json() : Promise.reject(r.status))
      .then(list => {
        renderCarousel(viewport, list || [], pageSize);
        const totalPages = Math.ceil(Math.max(list.length, 1) / pageSize);
        let page = 0;

        function showPage(p) {
          page = Math.max(0, Math.min(totalPages - 1, p));
          const offset = page * pageSize;
          viewport.style.transform = `translateX(-${offset * 100 / pageSize}%)`;
        }
        prevBtn.addEventListener('click', () => showPage(page - 1));
        nextBtn.addEventListener('click', () => showPage(page + 1));

        emptyEl.hidden = (list && list.length > 0);
      })
      .catch(() => {
        viewport.innerHTML = '';
        emptyEl.hidden = false;
      });
  }

  function renderCarousel(viewport, items, pageSize) {
    // 3개 단위 페이지 구조를 만들기 위해, 뷰포트 안쪽에 "슬라이드-그리드(3칸)"들을 생성
    const slides = [];
    for (let i = 0; i < items.length; i += pageSize) {
      const group = items.slice(i, i + pageSize);
      slides.push(group);
    }
    if (slides.length === 0) slides.push([]); // 비어도 1페이지는 유지

    viewport.innerHTML = '';
    slides.forEach(group => {
      const slide = document.createElement('div');
      slide.className = 'carousel__slide';
      // group 길이가 pageSize보다 작으면 빈칸(placeholder)로 채움
      for (let i = 0; i < pageSize; i++) {
        const item = group[i];
        if (item) {
          slide.appendChild(carouselCard(item));
        } else {
          const ph = document.createElement('div');
          ph.className = 'card card--empty';
          ph.textContent = '';
          slide.appendChild(ph);
        }
      }
      viewport.appendChild(slide);
    });
  }

  function carouselCard(item) {
    const el = document.createElement('a');
    el.className = 'card';
    el.href = `/explore?id=${encodeURIComponent(item.id)}`;

    const body = document.createElement('div');
    body.className = 'card__body';

    const title = document.createElement('h3');
    title.className = 'card__title';
    title.textContent = item.title || '(제목 없음)';

    const meta = document.createElement('div');
    meta.className = 'card__meta';
    meta.textContent = [item.category, item.status].filter(Boolean).join(' · ');

    body.appendChild(title);
    body.appendChild(meta);
    el.appendChild(body);
    return el;
  }
})();

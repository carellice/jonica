// Jonica nel browser: le quattro sezioni dell'app (Oggi, Stagioni, Botteghe, Filiera) con gli stessi
// dati. data.json lo scrive il test WebDataExport dell'app Android: non va modificato a mano.
'use strict';

const MONTHS = ['Gennaio', 'Febbraio', 'Marzo', 'Aprile', 'Maggio', 'Giugno', 'Luglio', 'Agosto', 'Settembre', 'Ottobre', 'Novembre', 'Dicembre'];
const DAYS = ['Domenica', 'Lunedì', 'Martedì', 'Mercoledì', 'Giovedì', 'Venerdì', 'Sabato'];
const CAT_ICON = { Frutta: 'ic_arance', Verdura: 'ic_broccolo_romanesco', Mare: 'ic_pesce', Dispensa: 'ic_olio' };
const SEASONS = {
  Inverno: { icon: 'ic_inverno', hero: 'linear-gradient(140deg, #2F5674, #3E6B8C 55%, #7FA0B8)' },
  Primavera: { icon: 'ic_primavera', hero: 'linear-gradient(140deg, #3E5A2C, #5C8240 55%, #A9CC85)' },
  Estate: { icon: 'ic_estate', hero: 'linear-gradient(140deg, #B0532F, #E3A93B 60%, #F2C46A)' },
  Autunno: { icon: 'ic_autunno', hero: 'linear-gradient(140deg, #3E5A2C, #8A6A2E 50%, #C8643B)' },
};
const PAGE = 40;

const $ = (sel, root = document) => root.querySelector(sel);
const esc = (s) => String(s ?? '').replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
const icon = (name) => `icons/${name}.webp`;
const img = (name, alt = '') => `<img src="${icon(name)}" alt="${esc(alt)}" loading="lazy" decoding="async">`;
const norm = (s) => String(s ?? '').normalize('NFD').replace(/\p{M}+/gu, '').toLowerCase();

const now = new Date();
const month = now.getMonth() + 1;
const prev = (m) => (m === 1 ? 12 : m - 1);
const next = (m) => (m === 12 ? 1 : m + 1);
const seasonOf = (m) => ([12, 1, 2].includes(m) ? 'Inverno' : m <= 5 ? 'Primavera' : m <= 8 ? 'Estate' : 'Autunno');
const inSeason = (p, m) => p.months.includes(m);
const arriving = (p, m) => inSeason(p, m) && !inSeason(p, prev(m));
const leaving = (p, m) => inSeason(p, m) && !inSeason(p, next(m));

let D; // i dati
const state = { month, cat: 'Tutte', shopCat: null, query: '', online: false, shown: PAGE, me: null, flipped: new Set() };
let map = null;
let markers = [];

// ---------- avvio e navigazione ----------

(async function start() {
  D = await (await fetch('data.json')).json();
  D.cats = Object.fromEntries(D.shopCategories.map((c) => [c.id, c]));
  addEventListener('hashchange', route);
  route();
})();

function route() {
  const tab = (location.hash.slice(1) || 'oggi').split('/')[0];
  const pages = { oggi: today, stagioni: seasons, botteghe: shopsPage, filiera: aware };
  const page = pages[tab] ? tab : 'oggi';
  document.querySelectorAll('.nav a').forEach((a) => a.classList.toggle('on', a.dataset.tab === page));
  if (map && page !== 'botteghe') { map.remove(); map = null; }
  closeSheet();
  pages[page]();
  scrollTo({ top: 0, behavior: 'instant' });
}

const view = (html) => { $('#view').innerHTML = html; };
const enter = (i) => `class="enter" style="--i:${i}"`;

function hero({ hello, title, lead, art, gradient, cls = '', stats = '' }) {
  return `<section class="hero ${cls}" style="${gradient ? `--hero:${gradient}` : ''}">
    <div class="glow"></div><div class="dots"></div>
    ${art ? `<img class="art" src="${icon(art)}" alt="">` : ''}
    <div class="wrap">
      ${hello ? `<div class="hello enter">${esc(hello)}</div>` : ''}
      <h1 ${enter(1)}>${esc(title)}</h1>
      ${lead ? `<p class="lead" ${enter(2)}>${esc(lead)}</p>` : ''}
      ${stats}
    </div>
  </section>`;
}

// ---------- Oggi ----------

function today() {
  const h = now.getHours();
  const hello = h >= 5 && h <= 12 ? 'Buongiorno' : h <= 17 && h > 12 ? 'Buon pomeriggio' : 'Buonasera';
  const season = seasonOf(month);
  const fresh = D.products.filter((p) => inSeason(p, month));
  const events = D.events.filter((e) => e.months.includes(month));
  const fact = D.facts[Math.floor(Math.random() * D.facts.length)];
  const rows = ['Frutta', 'Verdura', 'Mare', 'Dispensa'].map((cat) => {
    const list = fresh.filter((p) => p.category === cat);
    if (!list.length) return '';
    return `<section class="section">
      <div class="section-head"><div><h2>${cat === 'Mare' ? 'Dal mare' : cat === 'Dispensa' ? 'In dispensa' : `${cat} di stagione`}</h2>
        <p>${list.length} ${list.length === 1 ? 'prodotto' : 'prodotti'} al meglio adesso</p></div>
        <a class="more" href="#stagioni" data-cat="${cat}" aria-label="Tutto il calendario">→</a></div>
      <div class="row">${list.map(productCard).join('')}</div>
    </section>`;
  }).join('');
  const news = fresh.filter((p) => arriving(p, month));
  const ending = fresh.filter((p) => leaving(p, month));
  view(`${hero({
    hello, title: `${DAYS[now.getDay()]} ${now.getDate()} ${MONTHS[month - 1].toLowerCase()}`, art: SEASONS[season].icon, gradient: SEASONS[season].hero,
    stats: `<div class="stats">
      <div class="stat" ${enter(3)}><b>${season}</b><span>stagione</span></div>
      <div class="stat" ${enter(4)}><b data-count="${fresh.length}">0</b><span>di stagione</span></div>
      <div class="stat" ${enter(5)}><b data-count="${D.shops.length}">0</b><span>botteghe</span></div></div>`,
  })}
  <div class="wrap">
    ${events.length ? `<section class="section">
      <div class="section-head"><div><h2>Cosa succede nei campi</h2><p>Tocca una card per girarla e leggere il consiglio</p></div></div>
      <div class="row">${events.map((e, i) => `<button class="flip enter" style="--i:${i}" data-flip="${i}" aria-label="${esc(e.title)}: gira per il consiglio">
        <div class="flip-in">
          <div class="face"><div class="top"><span class="bubble" style="width:64px;height:64px">${img(e.icon)}</span><h3>${esc(e.title)}</h3></div>
            <p>${esc(e.description)}</p><span class="hint">☝ Gira per il consiglio</span></div>
          <div class="face back"><div class="top"><span class="bubble" style="width:64px;height:64px">${img(e.icon)}</span><h3>Il consiglio</h3></div>
            <p>${esc(e.advice)}</p><span class="hint">↺ Torna indietro</span></div>
        </div></button>`).join('')}</div>
    </section>` : ''}
    ${rows}
    ${news.length || ending.length ? `<section class="section duo">
      ${news.length ? `<div><div class="section-head"><div><h2>Appena arrivati</h2><p>Iniziano la stagione questo mese</p></div></div>
        <div class="row">${news.map(productCard).join('')}</div></div>` : ''}
      ${ending.length ? `<div><div class="section-head"><div><h2>Agli sgoccioli</h2><p>Ultimo mese per gustarli</p></div></div>
        <div class="row">${ending.map(productCard).join('')}</div></div>` : ''}
    </section>` : ''}
    <section class="section duo">
      <div class="card fact enter"><span class="bubble">${img(fact.icon)}</span><p id="fact">${esc(fact.text)}</p>
        <button class="btn round soft" id="another" aria-label="Un'altra curiosità">↻</button></div>
      <div class="card near enter" id="near"><span class="bubble">${img('ic_bancarella')}</span>
        <div><b>La bottega più vicina</b><p class="muted">Usa la tua posizione per trovarla, oppure sfoglia la mappa.</p></div>
        <button class="btn" id="locate-near">Trova</button></div>
    </section>
  </div>${footer()}`);
  countUp();
  bindProducts();
  document.querySelectorAll('[data-flip]').forEach((b) => b.addEventListener('click', () => b.classList.toggle('on')));
  document.querySelectorAll('.more[data-cat]').forEach((a) => a.addEventListener('click', () => { state.cat = a.dataset.cat; state.month = month; }));
  $('#another').addEventListener('click', () => {
    const f = D.facts[Math.floor(Math.random() * D.facts.length)];
    const p = $('#fact');
    p.animate([{ opacity: 1 }, { opacity: 0, transform: 'translateY(6px)' }, { opacity: 1 }], { duration: 420 });
    setTimeout(() => { p.textContent = f.text; $('.fact img').src = icon(f.icon); }, 200);
  });
  $('#locate-near').addEventListener('click', () => locate(() => {
    const s = nearestShops(1)[0];
    if (!s) return;
    const c = D.cats[s.categories[0]];
    $('#near').innerHTML = `<span class="bubble">${img(c.icon)}</span><div><b>${esc(s.name)}</b>
      <p class="muted">${esc(c.label)} · ${distText(s.d)}</p></div><button class="btn" id="open-near">Apri</button>`;
    $('#open-near').addEventListener('click', () => shopSheet(s));
  }));
}


function productCard(p, i = 0) {
  const badge = arriving(p, state.month) ? '<span class="tag new">Novità</span>' : leaving(p, state.month) ? '<span class="tag end">Ultimo mese</span>' : '';
  return `<button class="card product enter" style="--i:${i % 12}" data-product="${esc(p.name)}">
    <span class="bubble">${img(p.icon, p.name)}</span><b>${esc(p.name)}</b>${badge ? `<small>${badge}</small>` : p.origin ? `<small>${esc(p.origin)}</small>` : ''}</button>`;
}

function bindProducts() {
  document.querySelectorAll('[data-product]').forEach((b) => b.addEventListener('click', () => productSheet(D.products.find((p) => p.name === b.dataset.product))));
}

function countUp() {
  document.querySelectorAll('[data-count]').forEach((el) => {
    const target = Number(el.dataset.count);
    const start = performance.now() + 250;
    const step = (t) => {
      const k = Math.min(1, Math.max(0, (t - start) / 1100));
      el.textContent = Math.round(target * (1 - (1 - k) ** 3));
      if (k < 1) requestAnimationFrame(step);
    };
    requestAnimationFrame(step);
  });
}

// ---------- Stagioni ----------

function seasons() {
  view(`${hero({ title: 'Il calendario delle stagioni', lead: 'Frutta, verdura, pesce del Tirreno e dispensa, mese per mese, per il Centro Italia in pieno campo.', art: 'ic_cassetta', gradient: SEASONS[seasonOf(state.month)].hero })}
  <div class="wrap section">
    <div class="months" id="months">${MONTHS.map((m, i) => `<button class="chip ${i + 1 === state.month ? 'on' : ''} ${i + 1 === month ? 'now' : ''}" data-month="${i + 1}">${m}</button>`).join('')}</div>
    <div class="cats" id="cats">${['Tutte', 'Frutta', 'Verdura', 'Mare', 'Dispensa'].map((c) => `<button class="chip ${c === state.cat ? 'on' : ''}" data-cat="${c}">${CAT_ICON[c] ? img(CAT_ICON[c]) : ''}${c}</button>`).join('')}</div>
    <div id="season-grid"></div>
  </div>${footer()}`);
  const draw = () => {
    const list = D.products.filter((p) => inSeason(p, state.month) && (state.cat === 'Tutte' || p.category === state.cat));
    $('#season-grid').innerHTML = `<p class="count">${list.length} ${list.length === 1 ? 'prodotto' : 'prodotti'} a ${MONTHS[state.month - 1].toLowerCase()}</p>
      ${list.length ? `<div class="grid">${list.map(productCard).join('')}</div>` : '<div class="empty">Niente di stagione in questa categoria.</div>'}`;
    bindProducts();
    document.querySelectorAll('#months .chip').forEach((b) => b.classList.toggle('on', Number(b.dataset.month) === state.month));
    document.querySelectorAll('#cats .chip').forEach((b) => b.classList.toggle('on', b.dataset.cat === state.cat));
    document.querySelector('.hero').style.setProperty('--hero', SEASONS[seasonOf(state.month)].hero);
  };
  $('#months').addEventListener('click', (e) => { const b = e.target.closest('[data-month]'); if (b) { state.month = Number(b.dataset.month); draw(); } });
  $('#cats').addEventListener('click', (e) => { const b = e.target.closest('[data-cat]'); if (b) { state.cat = b.dataset.cat; draw(); } });
  draw();
  $('#months .chip.on')?.scrollIntoView({ inline: 'center', block: 'nearest' });
}

function productSheet(p) {
  openSheet(`<div class="head"><span class="bubble">${img(p.icon)}</span><div><h2>${esc(p.name)}</h2>
      <p class="muted">${esc(p.category)}${p.origin ? ` · ${esc(p.origin)}` : ''}</p></div></div>
    <div class="monthsbar">${MONTHS.map((m, i) => `<span class="${p.months.includes(i + 1) ? 'in' : ''} ${i + 1 === month ? 'cur' : ''}" title="${m}">${m[0]}</span>`).join('')}</div>
    <div class="block"><h4>Il consiglio</h4><p>${esc(p.tip)}</p></div>
    ${inSeason(p, month) ? '' : `<div class="block"><h4>Adesso</h4><p>Non è la sua stagione: probabilmente arriva da serre o da lontano.</p></div>`}`);
}

// ---------- Botteghe ----------

function shopsPage() {
  view(`${hero({ cls: 'blue', title: 'Botteghe e produttori', lead: `${D.shops.length} botteghe nel Lazio e ${D.online.length} produttori che spediscono in Italia.`, art: 'ic_bancarella' })}
  <div class="wrap section">
    <div class="toggle" role="tablist">
      <button class="${state.online ? '' : 'on'}" data-online="0">Vicino a te</button>
      <button class="${state.online ? 'on' : ''}" data-online="1">Online</button>
    </div>
    <div id="shops-body"></div>
  </div>${footer()}`);
  document.querySelectorAll('[data-online]').forEach((b) => b.addEventListener('click', () => {
    state.online = b.dataset.online === '1';
    state.shopCat = null; state.shown = PAGE;
    if (map) { map.remove(); map = null; }
    shopsPage();
  }));
  if (state.online) onlineBody(); else nearbyBody();
}

function categoryChips(present) {
  return `<div class="chips" id="shop-cats"><button class="chip ${state.shopCat ? '' : 'on'}" data-sc="">Tutte</button>
    ${D.shopCategories.filter((c) => present.has(c.id)).map((c) => `<button class="chip ${state.shopCat === c.id ? 'on' : ''}" data-sc="${c.id}">${img(c.icon)}${esc(c.label)}</button>`).join('')}</div>`;
}

function searchBox(placeholder) {
  return `<label class="search"><svg viewBox="0 0 24 24"><circle cx="11" cy="11" r="7"/><path d="M20 20l-4-4"/></svg>
    <input id="q" type="search" placeholder="${placeholder}" value="${esc(state.query)}" autocomplete="off"></label>`;
}

function nearbyBody() {
  const present = new Set(D.shops.flatMap((s) => s.categories));
  $('#shops-body').innerHTML = `<div class="shops" style="margin-top:16px">
    <div class="mapbox"><div id="map"></div><button class="btn round locate" id="locate" aria-label="La mia posizione">◎</button></div>
    <div>${searchBox('Cerca per nome, comune o prodotto')}${categoryChips(present)}<p class="count" id="count"></p><div class="list" id="list"></div></div>
  </div>`;
  const draw = () => {
    const q = norm(state.query);
    let list = D.shops.filter((s) => (!state.shopCat || s.categories.includes(state.shopCat))
      && (!q || norm([s.name, s.place, s.address, ...(s.products || []), ...s.categories.map((c) => D.cats[c].label)].join(' ')).includes(q)));
    list = withDistance(list);
    $('#count').textContent = `${list.length} ${list.length === 1 ? 'bottega' : 'botteghe'}${state.me ? ', dalla più vicina' : ''}`;
    $('#list').innerHTML = list.slice(0, state.shown).map((s, i) => {
      const c = D.cats[s.categories[0]];
      return `<button class="card shop enter" style="--i:${i % PAGE};--c:${c.color}" data-shop="${esc(s.id)}">
        <span class="bubble">${img(c.icon)}</span><span class="txt"><b>${esc(s.name)}</b><small>${esc([c.label, s.place || s.address].filter(Boolean).join(' · '))}</small></span>
        ${s.d != null ? `<span class="dist">${distText(s.d)}</span>` : ''}</button>`;
    }).join('') + (list.length > state.shown ? '<button class="btn soft loadmore" id="more">Mostra altre</button>' : '');
    $('#list').querySelectorAll('[data-shop]').forEach((b) => b.addEventListener('click', () => {
      const s = D.shops.find((x) => x.id === b.dataset.shop);
      shopSheet(s);
      map?.flyTo({ center: [s.lon, s.lat], zoom: 15 });
    }));
    $('#more')?.addEventListener('click', () => { state.shown += PAGE; draw(); });
    drawMarkers(list);
  };
  bindFilters(draw);
  initMap(draw);
  $('#locate').addEventListener('click', () => locate(() => { draw(); map?.flyTo({ center: [state.me.lon, state.me.lat], zoom: 13 }); }));
  draw();
}

function bindFilters(draw) {
  let timer;
  $('#q').addEventListener('input', (e) => { clearTimeout(timer); timer = setTimeout(() => { state.query = e.target.value; state.shown = PAGE; draw(); }, 150); });
  $('#shop-cats').addEventListener('click', (e) => {
    const b = e.target.closest('[data-sc]');
    if (!b) return;
    state.shopCat = b.dataset.sc || null; state.shown = PAGE;
    document.querySelectorAll('#shop-cats .chip').forEach((x) => x.classList.toggle('on', x === b));
    draw();
  });
}

function initMap(onReady) {
  if (!window.maplibregl) { $('#map').innerHTML = '<div class="empty">La mappa non si è caricata.</div>'; return; }
  const dark = matchMedia('(prefers-color-scheme: dark)').matches;
  map = new maplibregl.Map({
    container: 'map', style: dark ? 'map_dark.json' : 'map_light.json',
    center: state.me ? [state.me.lon, state.me.lat] : [12.53, 41.95], zoom: state.me ? 13 : 11, attributionControl: false,
  });
  map.addControl(new maplibregl.AttributionControl({ compact: true }), 'bottom-left');
  map.addControl(new maplibregl.NavigationControl({ showCompass: false }), 'bottom-right');
  map.on('load', onReady);
}

function drawMarkers(list) {
  if (!map) return;
  markers.forEach((m) => m.remove());
  // shortcut: un segnaposto DOM per bottega, con tutti i filtri sono ~500; con molte migliaia servirebbe un layer GeoJSON.
  markers = list.map((s) => {
    const c = D.cats[s.categories[0]];
    const el = document.createElement('button');
    el.className = 'pin';
    el.style.setProperty('--c', c.color);
    el.setAttribute('aria-label', s.name);
    el.innerHTML = img(c.icon);
    el.addEventListener('click', (e) => { e.stopPropagation(); shopSheet(s); });
    return new maplibregl.Marker({ element: el }).setLngLat([s.lon, s.lat]).addTo(map);
  });
  if (state.me) {
    const me = document.createElement('div');
    me.className = 'me';
    markers.push(new maplibregl.Marker({ element: me }).setLngLat([state.me.lon, state.me.lat]).addTo(map));
  }
}

function shopSheet(s) {
  const c = D.cats[s.categories[0]];
  const maps = s.mapsUrl || `https://www.google.com/maps/search/?api=1&query=${s.lat},${s.lon}`;
  openSheet(`<div class="head"><span class="bubble" style="background:color-mix(in srgb, ${c.color} 22%, var(--surface))">${img(c.icon)}</span>
      <div><h2>${esc(s.name)}</h2><p class="muted">${esc(s.categories.map((x) => D.cats[x].label).join(' · '))}</p></div></div>
    ${s.description ? `<div class="block"><p>${esc(s.description)}</p></div>` : ''}
    ${s.address || s.place ? `<div class="block"><h4>Dove</h4><p>${esc([s.address, s.place].filter(Boolean).join(', '))}${s.approximate ? ' (posizione indicativa: centro del comune)' : ''}</p>
      ${s.d != null ? `<p class="muted">A ${distText(s.d)} da te</p>` : ''}</div>` : ''}
    ${s.hours ? `<div class="block"><h4>Orari</h4><p>${esc(s.hours)}</p></div>` : ''}
    ${s.products?.length ? `<div class="block"><h4>Prodotti</h4><p>${esc(s.products.join(', '))}</p></div>` : ''}
    ${s.channels?.length ? `<div class="block"><h4>Come comprare</h4><p>${esc(s.channels.join(' · '))}</p></div>` : ''}
    ${s.note ? `<div class="block"><h4>Nota</h4><p>${esc(s.note)}</p></div>` : ''}
    <div class="links">
      <a class="btn" href="${esc(maps)}" target="_blank" rel="noopener">Indicazioni</a>
      ${s.phone ? `<a class="btn soft" href="tel:${esc(s.phone.replace(/\s/g, ''))}">Chiama</a>` : ''}
      ${s.website ? `<a class="btn soft" href="${esc(s.website)}" target="_blank" rel="noopener">Sito</a>` : ''}
      ${s.email ? `<a class="btn soft" href="mailto:${esc(s.email)}">Email</a>` : ''}
    </div>`);
}

function onlineBody() {
  const present = new Set(D.online.flatMap((s) => s.categories));
  $('#shops-body').innerHTML = `<div style="margin-top:16px">${searchBox('Cerca per nome, regione o prodotto')}${categoryChips(present)}
    <p class="count" id="count"></p><div class="list" id="list"></div>
    <section class="section"><div class="section-head"><div><h2>Negozi di più produttori</h2><p>Shop collettivi e multimarca</p></div></div>
      <div class="tips">${D.networks.map((n) => `<a class="card tip" href="${esc(n.url)}" target="_blank" rel="noopener" style="text-decoration:none"><b>${esc(n.name)}</b><span class="muted">${esc(n.description)}</span></a>`).join('')}</div></section>
  </div>`;
  const draw = () => {
    const q = norm(state.query);
    const list = D.online.filter((s) => (!state.shopCat || s.categories.includes(state.shopCat))
      && (!q || norm([s.name, s.region, s.place, ...s.products].join(' ')).includes(q)));
    $('#count').textContent = `${list.length} ${list.length === 1 ? 'produttore' : 'produttori'}`;
    $('#list').innerHTML = list.slice(0, state.shown).map((s, i) => {
      const c = D.cats[s.categories[0]];
      return `<button class="card shop enter" style="--i:${i % PAGE};--c:${c.color}" data-online-shop="${esc(s.id)}">
        <span class="bubble">${img(c.icon)}</span><span class="txt"><b>${esc(s.name)}</b><small>${esc([s.place || s.region, s.products.slice(0, 3).join(', ')].filter(Boolean).join(' · '))}</small></span>
        <span class="dist">${s.nationwide ? 'Italia' : esc(s.region)}</span></button>`;
    }).join('') + (list.length > state.shown ? '<button class="btn soft loadmore" id="more">Mostra altri</button>' : '');
    $('#list').querySelectorAll('[data-online-shop]').forEach((b) => b.addEventListener('click', () => onlineSheet(D.online.find((x) => x.id === b.dataset.onlineShop))));
    $('#more')?.addEventListener('click', () => { state.shown += PAGE; draw(); });
  };
  bindFilters(draw);
  draw();
}

function onlineSheet(s) {
  const c = D.cats[s.categories[0]];
  openSheet(`<div class="head"><span class="bubble" style="background:color-mix(in srgb, ${c.color} 22%, var(--surface))">${img(c.icon)}</span>
      <div><h2>${esc(s.name)}</h2><p class="muted">${esc([s.place, s.region].filter(Boolean).join(' · '))}</p></div></div>
    <div class="block"><h4>Prodotti</h4><p>${esc(s.products.join(', '))}</p></div>
    <div class="block"><h4>Ordini</h4><p>${esc(s.channel)}</p><p class="muted">${esc(s.shipping)}${s.chilled ? ' · Spedizione refrigerata' : ''}</p></div>
    ${s.note ? `<div class="block"><h4>Nota</h4><p>${esc(s.note)}</p></div>` : ''}
    <div class="links"><a class="btn" href="${esc(s.shopUrl)}" target="_blank" rel="noopener">Vai al negozio</a>
      ${s.website !== s.shopUrl ? `<a class="btn soft" href="${esc(s.website)}" target="_blank" rel="noopener">Sito</a>` : ''}</div>`);
}

// ---------- posizione ----------

function locate(done) {
  if (state.me) return done();
  if (!navigator.geolocation) return alert('Il browser non permette di leggere la posizione.');
  navigator.geolocation.getCurrentPosition((p) => {
    state.me = { lat: p.coords.latitude, lon: p.coords.longitude };
    done();
  }, () => alert('Posizione non disponibile: consenti l\'accesso alla posizione nel browser.'), { enableHighAccuracy: false, timeout: 10000 });
}

function km(a, b) {
  const r = Math.PI / 180;
  const x = Math.sin(((b.lat - a.lat) * r) / 2) ** 2 + Math.cos(a.lat * r) * Math.cos(b.lat * r) * Math.sin(((b.lon - a.lon) * r) / 2) ** 2;
  return 12742 * Math.asin(Math.sqrt(x));
}

function withDistance(list) {
  // In ordine alfabetico senza contare simboli iniziali come "*" o le virgolette.
  const key = (s) => norm(s.name).replace(/^[^\p{L}\p{Nd}]+/u, '');
  if (!state.me) return [...list].sort((a, b) => key(a).localeCompare(key(b), 'it'));
  return list.map((s) => ({ ...s, d: km(state.me, s) })).sort((a, b) => a.d - b.d);
}

const nearestShops = (n) => withDistance(D.shops).slice(0, n);
const distText = (d) => (d < 1 ? `${Math.max(10, Math.round(d * 100) * 10)} m` : `${d.toFixed(d < 10 ? 1 : 0).replace('.', ',')} km`);

// ---------- Filiera ----------

function aware() {
  const done = new Set(JSON.parse(localStorage.getItem('jonica.pledges') || '[]'));
  const reasons = [['ic_estate', 'Meno energia', 'Niente serre riscaldate'], ['ic_camion', 'Meno viaggi', 'Meno gasolio, prezzi stabili'],
    ['ic_fragole', 'Più gusto', 'Matura sulla pianta'], ['ic_cesto', 'Prezzo giusto', 'Abbondanza = costa meno']];
  view(`${hero({ cls: 'warm', title: 'Filiera giusta', lead: 'Dove vanno i soldi della spesa, perché la stagionalità conta e piccoli impegni per comprare meglio.', art: 'ic_contadino' })}
  <div class="wrap">
    <section class="section card euro enter">
      <h2>Dove va 1 € della tua spesa</h2><p class="muted">Frutta e verdura fresche al supermercato</p>
      <div class="bar">${D.slices.map((s) => `<span style="--c:${s.color}" data-w="${s.cents}" title="${esc(s.label)}"></span>`).join('')}</div>
      <div class="legend">${D.slices.map((s) => `<div><span class="bubble" style="--c:${s.color}">${img(s.icon)}</span><span>${esc(s.label)}</span><b style="--c:${s.color}">${(s.cents / 100).toFixed(2).replace('.', ',')} €</b></div>`).join('')}</div>
    </section>
    <section class="section"><div class="section-head"><div><h2>Perché la stagionalità conta</h2></div></div>
      <div class="reasons">${reasons.map(([ic, t, s], i) => `<div class="card reason enter" style="--i:${i}"><span class="bubble">${img(ic)}</span><b>${t}</b><span>${s}</span></div>`).join('')}</div>
    </section>
    <section class="section"><div class="section-head"><div><h2>I miei impegni</h2><p id="pledge-count"></p></div></div>
      <div class="progress"><span id="pledge-bar"></span></div>
      <div class="list" id="pledges">${D.pledges.map((p, i) => `<button class="card pledge enter ${done.has(p.text) ? 'on' : ''}" style="--i:${i}" data-pledge="${esc(p.text)}" aria-pressed="${done.has(p.text)}">
        <span class="bubble">${img(p.icon)}</span><span>${esc(p.text)}</span><span class="check"></span></button>`).join('')}</div>
    </section>
    <section class="section"><div class="section-head"><div><h2>Leggi l'etichetta</h2><p>Pochi secondi che fanno la differenza</p></div></div>
      <div class="tips">${D.labelTips.map((t, i) => `<div class="card tip enter" style="--i:${i}"><b>${esc(t.title)}</b><span class="muted">${esc(t.text)}</span></div>`).join('')}</div>
    </section>
    <section class="card cta enter"><img src="logo.png" alt="" width="84" height="84"><h2>Jonica anche sul telefono</h2>
      <p>L'app Android ha le stesse sezioni, funziona offline e si aggiorna da sola.</p>
      <a class="btn" href="https://github.com/carellice/jonica/releases/latest">Scarica l'APK</a></section>
  </div>${footer()}`);
  requestAnimationFrame(() => setTimeout(() => document.querySelectorAll('.bar span').forEach((s) => { s.style.width = `${s.dataset.w}%`; }), 150));
  const progress = () => {
    $('#pledge-count').textContent = `${done.size} di ${D.pledges.length} ${done.size === 1 ? 'impegno preso' : 'impegni presi'}`;
    $('#pledge-bar').style.width = `${(done.size / D.pledges.length) * 100}%`;
  };
  $('#pledges').addEventListener('click', (e) => {
    const b = e.target.closest('[data-pledge]');
    if (!b) return;
    const t = b.dataset.pledge;
    if (done.has(t)) done.delete(t); else done.add(t);
    b.classList.toggle('on', done.has(t));
    b.setAttribute('aria-pressed', done.has(t));
    localStorage.setItem('jonica.pledges', JSON.stringify([...done]));
    progress();
  });
  progress();
}

// ---------- foglio e piè di pagina ----------

function openSheet(html) {
  const sheet = $('#sheet');
  sheet.innerHTML = `<div class="panel" role="dialog" aria-modal="true"><div class="grab"></div>
    <button class="btn round soft close" aria-label="Chiudi">✕</button>${html}</div>`;
  sheet.hidden = false;
  sheet.onclick = (e) => { if (e.target === sheet || e.target.closest('.close')) closeSheet(); };
  sheet.querySelector('.close').focus();
}

function closeSheet() { $('#sheet').hidden = true; }
addEventListener('keydown', (e) => { if (e.key === 'Escape') closeSheet(); });

const footer = () => `<footer>Jonica · Mangia di stagione, compra vicino · Dati delle botteghe © OpenStreetMap contributors e mappature di Jonica ·
  <a href="https://github.com/carellice/jonica">Codice sorgente</a></footer>`;

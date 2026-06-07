// SPDX-License-Identifier: MIT
// radiOOracle listener — render SICURO: niente innerHTML grezzo, link https validati.
"use strict";

const fmt = s => `${Math.floor(s / 60)}:${String(Math.floor(s % 60)).padStart(2, "0")}`;

async function getJSON(path) {
  try { const r = await fetch(path, { cache: "no-store" }); if (r.ok) return await r.json(); }
  catch (e) {}
  return null;
}

// link sicuro: solo https, rel noopener noreferrer, target blank
function safeLink(url, label) {
  const a = document.createElement("a");
  a.className = "cta";
  if (typeof url === "string" && url.startsWith("https://")) {
    a.href = url; a.target = "_blank"; a.rel = "noopener noreferrer nofollow";
  } else { a.href = "#"; }
  a.textContent = label || "Scopri";
  return a;
}

// immagine sponsor: solo https o locale, mai dato:script
function safeLogo(url) {
  if (typeof url !== "string") return null;
  if (!(url.startsWith("https://") || url.startsWith("assets/") || url.startsWith("img/"))) return null;
  const img = document.createElement("img");
  img.className = "logo"; img.src = url; img.alt = "sponsor"; img.loading = "lazy";
  return img;
}

function renderSlot(el, sponsor) {
  if (!el) return;
  el.textContent = "";
  if (!sponsor || !sponsor.name) {
    // BOX VUOTO: invito agli inserzionisti
    const lab = document.createElement("div"); lab.className = "label"; lab.textContent = "Spazio pubblicitario";
    const txt = document.createElement("div"); txt.className = "spn-text";
    txt.textContent = "Disponibile — la tua pubblicità qui.";
    el.append(lab, txt, safeLink(EMPTY_CTA, "Prenota lo spazio"));
    return;
  }
  el.classList.add("filled");
  const lab = document.createElement("div"); lab.className = "label"; lab.textContent = "Sponsor";
  el.appendChild(lab);
  const logo = safeLogo(sponsor.logo); if (logo) el.appendChild(logo);
  const name = document.createElement("div"); name.className = "spn-name"; name.textContent = sponsor.name;
  const text = document.createElement("div"); text.className = "spn-text"; text.textContent = sponsor.text || "";
  el.append(name, text);
  if (sponsor.url) el.appendChild(safeLink(sponsor.url, sponsor.cta || "Scopri"));
}

let EMPTY_CTA = "#";

async function init() {
  // sponsor
  const sp = await getJSON("data/sponsors.json") || {};
  EMPTY_CTA = (typeof sp.advertise_url === "string" && sp.advertise_url.startsWith("https://")) ? sp.advertise_url : "#";
  const slots = sp.slots || {};
  renderSlot(document.getElementById("ad-banner"), slots.banner);
  renderSlot(document.getElementById("ad-box1"), slots.box1);
  renderSlot(document.getElementById("ad-box2"), slots.box2);
  renderSlot(document.getElementById("ad-footer"), slots.footer);

  // scaletta
  const sca = await getJSON("data/scaletta.json");
  const rows = document.getElementById("rows");
  rows.textContent = "";
  if (sca && sca.items) {
    document.getElementById("onair").textContent = "In onda · " + (sca.on_air || "radiOOracle");
    const first = sca.items.find(i => i.type === "music") || sca.items[0];
    if (first) document.getElementById("track").textContent = first.title || "—";
    for (const it of sca.items) {
      const li = document.createElement("li");
      const ix = document.createElement("span"); ix.className = "ix"; ix.textContent = it.n;
      const ty = document.createElement("span"); ty.className = "ty"; ty.textContent = it.type;
      const ti = document.createElement("span"); ti.className = "ti"; ti.textContent = it.title || "";
      const du = document.createElement("span"); du.className = "du"; du.textContent = fmt(it.duration_s || 0);
      li.append(ix, ty, ti, du); rows.appendChild(li);
    }
  } else {
    rows.textContent = "Scaletta non disponibile.";
  }

  // player
  const audio = document.getElementById("audio");
  const btn = document.getElementById("play");
  btn.addEventListener("click", () => {
    if (audio.paused) { audio.play(); btn.textContent = "⏸"; }
    else { audio.pause(); btn.textContent = "▶"; }
  });
  audio.addEventListener("ended", () => { btn.textContent = "▶"; });
}

init();

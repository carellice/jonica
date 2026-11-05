"""Genera ShopsRoma.kt dalla mappatura "Roma: botteghe alimentari e piccoli produttori, priorità Colle Salario 00138".

- Coordinate: quelle della scheda; se mancano, geocoding Nominatim (cache condivisa con geocode_lazio.py).
- Doppioni: le schede nuove sostituiscono quelle già in app (ricerca Lazio e OpenStreetMap del quartiere)
  quando sono la stessa attività: stesso id Lazio di origine, oppure vicine (< 120 m) con nome simile.

Usage: python scripts/gen_roma.py [scripts/data/botteghe_alimentari_roma_00138.json]
"""
import json
import math
import pathlib
import re
import sys
import unicodedata

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))
import geocode_lazio as geo  # noqa: E402
from gen_lazio import CATEGORY as LAZIO_CATEGORY, kstr, klist  # noqa: E402

ROOT = pathlib.Path(__file__).resolve().parent.parent
SRC = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else ROOT / "scripts/data/botteghe_alimentari_roma_00138.json"
OUT = ROOT / "app/src/main/java/it/jonica/app/data/ShopsRoma.kt"
EXISTING = [ROOT / "app/src/main/java/it/jonica/app/data/Shops.kt", ROOT / "app/src/main/java/it/jonica/app/data/ShopsLazio.kt"]
CHUNK = 40
ROMA_CENTER = (41.8933, 12.4829)

CATEGORY = dict(LAZIO_CATEGORY)
CATEGORY.update({
    "bottega_caffe": "TORREFAZIONE",
    "formaggeria": "CASEIFICIO",
    "specialita_vegetali": "FRUTTA_VERDURA",
    "sfuso": "ALIMENTARI",
})

CHANNELS = {
    "acquisto_in_sede_da_confermare": "Acquisto in negozio (da confermare)",
    "vendita_al_dettaglio": "Vendita al dettaglio",
    "vendita_al_dettaglio_documentata": "Vendita al dettaglio",
    "vendita_al_banco": "Vendita al banco",
    "ecommerce": "Vendita online",
    "ecommerce_diretto": "Vendita online",
    "asporto": "Asporto",
    "spaccio_aziendale": "Spaccio aziendale",
    "vendita_diretta_in_azienda": "Vendita diretta in azienda",
    "vendita_diretta": "Vendita diretta",
    "tap_room": "Tap room",
}

FAMILY = {"gestione_personale_o_familiare_documentata", "gestione_personale_familiare_o_cooperativa_documentata", "comunita_monastica"}
VERIFIED = {"approfondita_con_fonti_dirette", "documentata_da_fonti_territoriali", "ripresa_da_ricerca_lazio"}
STOP = {"di", "del", "della", "e", "la", "il", "le", "lo", "da", "dal", "via", "roma", "mercato", "rionale", "pasticceria",
        "macelleria", "forno", "alimentari", "pescheria", "bottega", "frutta", "verdura", "gastronomia", "enoteca", "caffe", "bar"}


def norm(t: str) -> str:
    t = unicodedata.normalize("NFD", t.lower())
    return "".join(c for c in t if unicodedata.category(c) != "Mn")


def tokens(name: str) -> set:
    return {w for w in re.findall(r"[a-z0-9]+", norm(name)) if len(w) > 2 and w not in STOP}


def meters(a, b):
    dlat = math.radians(b[0] - a[0])
    dlon = math.radians(b[1] - a[1]) * math.cos(math.radians(a[0]))
    return 6371000 * math.hypot(dlat, dlon)


def channel(c: str) -> str:
    return CHANNELS.get(c) or (c.replace("_", " ").capitalize())


def existing_shops():
    """(id, nome, lat, lon, categoria) delle botteghe già generate per l'app."""
    pat = re.compile(r'Shop\("(?P<id>[^"]+)", "(?P<name>(?:[^"\\]|\\.)*)", ShopCategory\.(?P<cat>\w+), (?P<lat>[\d.]+), (?P<lon>[\d.]+)')
    named = re.compile(r'id = "(?P<id>[^"]+)",\s+name = "(?P<name>(?:[^"\\]|\\.)*)",\s+category = ShopCategory\.(?P<cat>\w+),\s+lat = (?P<lat>[\d.]+),\s+lon = (?P<lon>[\d.]+)')
    out = []
    for f in EXISTING:
        text = f.read_text(encoding="utf-8")
        for m in list(pat.finditer(text)) + list(named.finditer(text)):
            out.append((m["id"], m["name"], float(m["lat"]), float(m["lon"]), m["cat"]))
    return out


def locate(name, address, zona):
    """Coordinate da geocoding: indirizzo, poi zona, poi centro di Roma. Ritorna (lat, lon, approssimata)."""
    if address:
        street = geo.clean_address(address, "Roma")
        for q in (f"{street}, Roma, Italia", f"{street.split(',')[0]}, Roma, Italia"):
            hit = geo.search(q)
            if hit and meters((hit["lat"], hit["lon"]), ROMA_CENTER) < 30000:
                return hit["lat"], hit["lon"], False
    if zona and zona != "da_confermare":
        hit = geo.search(f"{zona.split(' - ')[0]}, Roma, Italia")
        if hit:
            return hit["lat"], hit["lon"], True
    return ROMA_CENTER[0], ROMA_CENTER[1], True


def place(zona):
    return f"{zona}, Roma" if zona and zona != "da_confermare" else "Roma"


def main():
    data = json.loads(SRC.read_text(encoding="utf-8"))
    old = existing_shops()
    replaces = set()
    rows = []

    def add(row_id, name, cat_list, lat, lon, approx, **kw):
        cats = list(dict.fromkeys(cat_list)) or ["ALIMENTARI"]
        # stessa attività già in app? la scheda nuova la sostituisce
        mine = tokens(name)
        for oid, oname, olat, olon, ocat in old:
            d = meters((lat, lon), (olat, olon))
            same_market = cats[0] == "MERCATO" and ocat == "MERCATO" and d < 350
            if d < 120 and (mine & tokens(oname)) or same_market:
                replaces.add(oid)
        fields = [
            f"        id = {kstr(row_id)},",
            f"        name = {kstr(name)},",
            f"        category = ShopCategory.{cats[0]},",
            f"        lat = {lat:.6f},",
            f"        lon = {lon:.6f},",
            f"        address = {kstr(kw.get('address'))},",
            "        hours = null,",
            f"        phone = {kstr(kw.get('phone'))},",
            f"        categories = listOf({', '.join('ShopCategory.' + c for c in cats)}),",
            f"        comune = {kstr(kw.get('comune'))},",
            "        provincia = \"RM\",",
            f"        description = {kstr(kw.get('description'))},",
            f"        products = {klist(kw.get('products') or [])},",
            f"        channels = {klist(list(dict.fromkeys(kw.get('channels') or [])))},",
            f"        website = {kstr(kw.get('website'))},",
            f"        email = {kstr(kw.get('email'))},",
            f"        note = {kstr(kw.get('note'))},",
            f"        approximate = {str(approx).lower()},",
            f"        source = ShopSource.{kw.get('source', 'SCHEDA')},",
            f"        family = {str(bool(kw.get('family'))).lower()},",
            f"        mapsUrl = {kstr(kw.get('maps'))},",
        ]
        rows.append("    Shop(\n" + "\n".join(fields) + "\n    ),")

    for a in data["attivita"]:
        loc = a["localizzazione"]
        prev = a.get("origine_ricerca_precedente")
        if prev and prev.get("id"):
            replaces.add(prev["id"])
        if loc.get("coordinate"):
            lat, lon, approx = loc["coordinate"]["lat"], loc["coordinate"]["lon"], False
        else:
            lat, lon, approx = locate(a["nome"], loc.get("indirizzo"), loc.get("zona"))
        c = a["contatti"]
        email = next((e for e in c.get("email") or [] if "pec." not in e.lower()), None)
        links = a.get("link_mappe") or {}
        add(
            a["id"], a["nome"], [CATEGORY[x] for x in a["categorie"] if x in CATEGORY], lat, lon, approx,
            address=loc.get("indirizzo"),
            phone=(c.get("telefoni_e164") or c.get("telefoni") or [None])[0],
            comune=place(loc.get("zona")),
            description=a.get("spunto_scoperta"),
            products=a.get("prodotti_documentati"),
            channels=[channel(x) for x in a.get("canali_acquisto") or []],
            website=c.get("sito_web"),
            email=email,
            note=(a.get("verifica", {}).get("note") or [None])[0],
            source="SCHEDA" if a["verifica"]["livello"] in VERIFIED else "REGISTRO",
            family=a.get("profilo_impresa", {}).get("stato") in FAMILY,
            maps=links.get("scheda_da_cid") or links.get("ricerca_google_maps"),
        )

    for i, cnd in enumerate(data["candidati_da_ricontrollare"]):
        lat, lon, approx = locate(cnd["nome"], cnd.get("indirizzo"), cnd.get("zona"))
        cat = "MACELLERIA" if "macell" in norm(cnd["nome"]) else "CASEIFICIO" if "casei" in norm(cnd["nome"]) or "mozza" in norm(cnd["nome"]) \
            else "FRUTTA_VERDURA" if "ortofrutta" in norm(cnd["nome"]) else "ALIMENTARI"
        add(
            f"roma_candidato_{i}", cnd["nome"], [cat], lat, lon, approx,
            address=cnd.get("indirizzo"),
            phone=cnd.get("telefono"),
            comune=place(cnd.get("zona")),
            note=cnd.get("motivo_verifica"),
            source="REGISTRO",
            maps=cnd.get("link_mappe"),
        )

    for m in data["mercati_di_riferimento"]:
        # mercato già mappato su OpenStreetMap con lo stesso nome: la sua posizione è più precisa
        # del geocoding della sola via (che cade a metà strada)
        same = next((o for o in old if o[4] == "MERCATO" and tokens(m["nome"]) & tokens(o[1])), None)
        if m.get("coordinate"):
            lat, lon, approx = m["coordinate"]["lat"], m["coordinate"]["lon"], False
        elif same:
            lat, lon, approx = same[2], same[3], False
        else:
            lat, lon, approx = locate(m["nome"], m.get("indirizzo"), m.get("zona"))
        add(
            m["id"], m["nome"], ["MERCATO"], lat, lon, approx,
            address=m.get("indirizzo"),
            comune=place(m.get("zona")),
            description=m.get("perche_esplorarlo"),
            note=m.get("nota"),
            maps=m.get("link_mappe"),
        )

    chunks = [rows[i:i + CHUNK] for i in range(0, len(rows), CHUNK)]
    body = "\n\n".join(f"private fun part{i}(): List<Shop> = listOf(\n" + "\n".join(c) + "\n)" for i, c in enumerate(chunks))
    OUT.write_text(
        "// Generato da scripts/gen_roma.py dalla mappatura \"Roma: botteghe alimentari e piccoli produttori, priorità Colle Salario 00138\".\n"
        "package it.jonica.app.data\n\n"
        "val romaShops: List<Shop> by lazy { " + " + ".join(f"part{i}()" for i in range(len(chunks))) + " }\n\n"
        "/** Schede già in app (OSM o ricerca Lazio) che la mappatura di Roma sostituisce perché più complete. */\n"
        "val romaReplaces: Set<String> = setOf(\n" + "".join(f"    {kstr(r)},\n" for r in sorted(replaces)) + ")\n\n"
        + body + "\n",
        encoding="utf-8",
    )
    print(len(rows), "schede Roma,", len(replaces), "sostituite ->", OUT)
    names = {o[0]: o[1] for o in old}
    for r in sorted(replaces):
        print("  sostituita:", r, names.get(r))


if __name__ == "__main__":
    main()

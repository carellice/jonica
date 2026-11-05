"""Genera ShopsLazio.kt dalla mappatura "Botteghe alimentari e piccoli produttori del Lazio".

Usa le coordinate calcolate da geocode_lazio.py (scripts/lazio_geo.json).
Usage: python scripts/gen_lazio.py [scripts/data/botteghe_alimentari_lazio.json]
"""
import json
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
SRC = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else ROOT / "scripts/data/botteghe_alimentari_lazio.json"
GEO = ROOT / "scripts/lazio_geo.json"
OUT = ROOT / "app/src/main/java/it/jonica/app/data/ShopsLazio.kt"
CHUNK = 40  # elementi per funzione: evita il limite di 64KB di bytecode per metodo

CATEGORY = {
    "macelleria": "MACELLERIA", "pescheria": "PESCHERIA", "ortofrutta": "FRUTTA_VERDURA",
    "alimentari": "ALIMENTARI", "norcineria": "NORCINERIA", "forno": "FORNO",
    "pasticceria": "PASTICCERIA", "torrefazione": "TORREFAZIONE", "caseificio": "CASEIFICIO",
    "frantoio": "FRANTOIO", "enoteca": "ENOTECA", "cantina": "CANTINA", "pastificio": "PASTA_FRESCA",
    "mulino": "MULINO", "apicoltura": "APICOLTURA", "conserve": "CONSERVE", "gastronomia": "GASTRONOMIA",
    "birrificio": "BIRRIFICIO", "gelateria": "GELATERIA", "spezie": "SPEZIE", "azienda_agricola": "AZIENDA_AGRICOLA",
}

CHANNELS = {
    "banco_mercato": "Banco al mercato",
    "acquisto_su_contatto_da_confermare": "Acquisto su contatto (da confermare)",
    "vendita_al_dettaglio": "Vendita al dettaglio",
    "vendita_diretta_su_contatto": "Vendita diretta su contatto",
    "circuito_vendita_diretta": "Circuito di vendita diretta",
    "esercizio_storico_da_confermare": "Bottega storica (apertura da confermare)",
}

# capoluoghi: ultima riserva se il geocoding non ha trovato nemmeno il comune
CAPOLUOGHI = {"RM": (41.8933, 12.4829), "LT": (41.4676, 12.9037), "FR": (41.6396, 13.3426),
              "VT": (42.4207, 12.1077), "RI": (42.4045, 12.8567)}

LOWER = {"di", "del", "della", "dello", "dei", "degli", "delle", "da", "dal", "dalla", "e", "ed", "in", "a", "al", "alla", "con", "per"}
LEGAL = r"\b(S\.?\s?R\.?\s?L\.?S?|S\.?\s?N\.?\s?C\.?|S\.?\s?A\.?\s?S\.?|S\.?\s?P\.?\s?A\.?|SOC(IETA'?)?\.?\s+COOP(ERATIVA)?\.?|SOCIETA'? AGRICOLA|SEMPLIFICATA|UNIPERSONALE)\b"


def pretty(text: str | None) -> str | None:
    """Converte i nomi in MAIUSCOLO del registro in forma leggibile."""
    if not text:
        return text
    t = text.strip()
    letters = [c for c in t if c.isalpha()]
    if letters and sum(c.isupper() for c in letters) / len(letters) > 0.8:
        t = re.sub(r"\(\s*DITTA INDIVIDUALE\s*\)|\bDITTA INDIVIDUALE\b|\bIMPRESA INDIVIDUALE\b", " ", t, flags=re.I)
        t = re.sub(LEGAL, " ", t, flags=re.I)
        t = re.sub(r"\s*&\s*C\.?\s*$", "", t)
        t = re.sub(r"\bN\.\s*", "", t)
        t = re.sub(r"\s+", " ", t).strip(" ,-.")
        words = t.lower().split(" ")
        out = []
        for i, w in enumerate(words):
            if i > 0 and w in LOWER:
                out.append(w)
            else:
                out.append("-".join(p[:1].upper() + p[1:] for p in w.split("-")))
        t = " ".join(out)
        t = re.sub(r"(?<=\w)'(\w)", lambda m: "'" + m.group(1).upper(), t)  # Dell'Uomo
        t = re.sub(r"([aeiou])'(?=\s|$|,)", lambda m: {"a": "à", "e": "è", "i": "ì", "o": "ò", "u": "ù"}[m.group(1)], t)
    return re.sub(r"\s+", " ", t).strip()


def sentence(text: str | None) -> str | None:
    if not text:
        return None
    t = text.strip().lower().replace(" - ", ", ")
    return t[:1].upper() + t[1:]


def kstr(s):
    if s is None:
        return "null"
    return '"' + s.replace("\\", "\\\\").replace('"', '\\"').replace("$", "\\$").replace("\n", " ") + '"'


def klist(items):
    return "listOf(" + ", ".join(kstr(i) for i in items) + ")" if items else "emptyList()"


def main():
    data = json.loads(SRC.read_text(encoding="utf-8"))
    geo = json.loads(GEO.read_text(encoding="utf-8"))
    rows = []
    records = [(r, True) for r in data["attivita_approfondite"]] + [(r, False) for r in data["candidati_da_verificare"]]
    for rec, detailed in records:
        loc = rec["localizzazione"]
        cats = [CATEGORY[c] for c in rec["categorie"] if c in CATEGORY] or ["ALIMENTARI"]
        g = geo.get(rec["id"])
        if g:
            lat, lon, approx = g["lat"], g["lon"], g["precisione"] != "indirizzo"
        else:
            (lat, lon), approx = CAPOLUOGHI[loc["provincia"]], True
        contacts = rec["contatti"]
        phone = (contacts.get("telefoni_e164") or contacts.get("telefoni") or [None])[0]
        email = next((e for e in contacts.get("email") or [] if "pec." not in e.lower()), None)
        description = rec.get("perche_scoprirla")
        if not detailed:
            description = sentence(rec.get("dati_registro", {}).get("tipologia_originale"))
        market = [p["nome"] for p in rec.get("altri_punti_acquisto") or []]
        note = (rec.get("verifica", {}).get("note") or [None])[0]
        family = rec.get("indipendenza", {}).get("stato") == "gestione_personale_o_familiare_documentata"
        rows.append(
            "    Shop(\n"
            f"        id = {kstr(rec['id'])},\n"
            f"        name = {kstr(pretty(rec['nome']))},\n"
            f"        category = ShopCategory.{cats[0]},\n"
            f"        lat = {lat:.6f},\n"
            f"        lon = {lon:.6f},\n"
            f"        address = {kstr(pretty(loc.get('indirizzo')) or loc.get('localita'))},\n"
            f"        hours = null,\n"
            f"        phone = {kstr(phone)},\n"
            f"        categories = listOf({', '.join('ShopCategory.' + c for c in dict.fromkeys(cats))}),\n"
            f"        comune = {kstr(loc['comune'])},\n"
            f"        provincia = {kstr(loc['provincia'])},\n"
            f"        description = {kstr(description)},\n"
            f"        products = {klist(rec.get('prodotti_segnalati') or [])},\n"
            f"        channels = {klist([CHANNELS.get(c, c.replace('_', ' ')) for c in rec.get('canali_acquisto') or []])},\n"
            f"        alsoAt = {klist(market)},\n"
            f"        website = {kstr(contacts.get('sito_web'))},\n"
            f"        email = {kstr(email)},\n"
            f"        note = {kstr(note)},\n"
            f"        approximate = {str(approx).lower()},\n"
            f"        source = ShopSource.{'SCHEDA' if detailed else 'REGISTRO'},\n"
            f"        family = {str(family).lower()},\n"
            "    ),"
        )
    for m in data["mercati_di_riferimento"]:
        g = geo.get("mercato:" + m["nome"])
        if not g:
            continue
        rows.append(
            "    Shop(\n"
            f"        id = {kstr('mercato_' + re.sub(r'[^a-z]+', '_', m['nome'].lower()))},\n"
            f"        name = {kstr(m['nome'])},\n"
            "        category = ShopCategory.MERCATO,\n"
            f"        lat = {g['lat']:.6f},\n"
            f"        lon = {g['lon']:.6f},\n"
            f"        address = {kstr(m['indirizzo'])},\n"
            "        hours = null,\n"
            "        phone = null,\n"
            f"        comune = {kstr(m['comune'])},\n"
            f"        provincia = {kstr(m['provincia'])},\n"
            "        description = \"Mercato dove vendono direttamente diversi produttori della mappatura.\",\n"
            "        source = ShopSource.SCHEDA,\n"
            "    ),"
        )
    chunks = [rows[i:i + CHUNK] for i in range(0, len(rows), CHUNK)]
    body = "\n\n".join(
        f"private fun part{i}(): List<Shop> = listOf(\n" + "\n".join(c) + "\n)" for i, c in enumerate(chunks)
    )
    OUT.write_text(
        "// Generato da scripts/gen_lazio.py dalla mappatura \"Botteghe alimentari e piccoli produttori del Lazio\".\n"
        "// Coordinate: geocoding OpenStreetMap/Nominatim (© OpenStreetMap contributors, ODbL).\n"
        "package it.jonica.app.data\n\n"
        "val lazioShops: List<Shop> by lazy { " + " + ".join(f"part{i}()" for i in range(len(chunks))) + " }\n\n"
        + body + "\n",
        encoding="utf-8",
    )
    print(len(rows), "botteghe ->", OUT)


if __name__ == "__main__":
    main()

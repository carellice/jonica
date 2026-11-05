"""Geocodifica le botteghe della mappatura Lazio con Nominatim (OpenStreetMap).

Rispetta la policy d'uso: una richiesta al secondo, User-Agent identificativo, cache su disco.
Prima prova l'indirizzo, poi ripiega sul centro del comune (posizione approssimativa).

Usage: python scripts/geocode_lazio.py <botteghe_alimentari_lazio.json>
Output: scripts/lazio_geo.json  {id: {"lat", "lon", "precisione": "indirizzo" | "comune"}}
"""
import json
import math
import pathlib
import re
import sys
import time
import urllib.parse
import urllib.request

ROOT = pathlib.Path(__file__).resolve().parent
CACHE = ROOT / "geocode_cache.json"
OUT = ROOT / "lazio_geo.json"
UA = "JonicaApp/1.3 (+https://github.com/carellice/jonica)"
PROVINCE = {"RM": "Roma", "LT": "Latina", "FR": "Frosinone", "VT": "Viterbo", "RI": "Rieti"}
# riquadro del Lazio con un po' di margine
LAT_MIN, LAT_MAX, LON_MIN, LON_MAX = 40.75, 42.85, 11.40, 14.10

cache = json.loads(CACHE.read_text(encoding="utf-8")) if CACHE.exists() else {}
last_call = 0.0


def search(q: str):
    global last_call
    if q in cache:
        return cache[q]
    wait = 1.1 - (time.time() - last_call)
    if wait > 0:
        time.sleep(wait)
    url = "https://nominatim.openstreetmap.org/search?" + urllib.parse.urlencode(
        {"q": q, "format": "jsonv2", "limit": 1, "countrycodes": "it",
         "viewbox": f"{LON_MIN},{LAT_MAX},{LON_MAX},{LAT_MIN}", "bounded": 1}
    )
    req = urllib.request.Request(url, headers={"User-Agent": UA, "Accept-Language": "it"})
    for attempt in range(3):
        try:
            with urllib.request.urlopen(req, timeout=30) as r:
                res = json.loads(r.read().decode("utf-8"))
            break
        except Exception as e:  # rete instabile: riprova con pausa
            print("  retry", attempt, e, flush=True)
            time.sleep(5)
    else:
        res = []
    last_call = time.time()
    hit = None
    if res:
        hit = {"lat": float(res[0]["lat"]), "lon": float(res[0]["lon"]), "type": res[0].get("addresstype")}
    cache[q] = hit
    CACHE.write_text(json.dumps(cache, ensure_ascii=False, indent=0), encoding="utf-8")
    return hit


def km(a, b):
    dlat = math.radians(b["lat"] - a["lat"])
    dlon = math.radians(b["lon"] - a["lon"]) * math.cos(math.radians(a["lat"]))
    return 6371 * math.hypot(dlat, dlon)


def clean_address(addr: str, comune: str) -> str:
    a = addr
    a = re.sub(r"\(\s*[A-Z]{2}\s*\)", " ", a)            # (FR)
    a = re.sub(r"\bN\.\s*", " ", a, flags=re.I)          # "N. 7"
    a = re.sub(r"\bkm\s*[\d.,]+", " ", a, flags=re.I)    # "km 1,500"
    a = re.sub(r"\bs\.?n\.?c?\b", " ", a, flags=re.I)    # "snc"
    # tolgo il comune se ripetuto nell'indirizzo, verrà aggiunto dopo
    a = re.sub(re.escape(comune), " ", a, flags=re.I)
    a = re.sub(r"\b(Latina|Roma|Frosinone|Viterbo|Rieti|Lazio|Italia)\b\s*$", " ", a, flags=re.I)
    a = re.sub(r"\s+", " ", a).strip(" ,;-")
    return a


def main(src: pathlib.Path):
    data = json.loads(src.read_text(encoding="utf-8"))
    records = data["attivita_approfondite"] + data["candidati_da_verificare"]
    out = json.loads(OUT.read_text(encoding="utf-8")) if OUT.exists() else {}
    comuni = {}
    for i, rec in enumerate(records, 1):
        loc = rec["localizzazione"]
        comune, prov = loc["comune"], loc["provincia"]
        key = (comune, prov)
        if key not in comuni:
            comuni[key] = search(f"{comune}, {PROVINCE.get(prov, prov)}, Lazio, Italia")
        center = comuni[key]
        point, precision = None, None
        if loc.get("indirizzo"):
            street = clean_address(loc["indirizzo"], comune)
            if street:
                hit = search(f"{street}, {comune}, {PROVINCE.get(prov, prov)}, Italia")
                if not hit and "," in street:
                    hit = search(f"{street.split(',')[0]}, {comune}, Italia")
                if hit and (center is None or km(hit, center) < 25):
                    point, precision = hit, "indirizzo"
        if point is None and center is not None:
            point, precision = center, "comune"
        if point:
            out[rec["id"]] = {"lat": round(point["lat"], 6), "lon": round(point["lon"], 6), "precisione": precision}
        print(f"{i}/{len(records)} {precision or 'NON TROVATA'} {rec['nome'][:40]}", flush=True)
        OUT.write_text(json.dumps(out, ensure_ascii=False, indent=0), encoding="utf-8")
    for m in data["mercati_di_riferimento"]:
        if m.get("coordinate"):
            out["mercato:" + m["nome"]] = {"lat": m["coordinate"]["latitudine"], "lon": m["coordinate"]["longitudine"], "precisione": "indirizzo"}
        else:
            hit = search(f"{m['indirizzo']}, {m['comune']}, Italia")
            if hit:
                out["mercato:" + m["nome"]] = {"lat": hit["lat"], "lon": hit["lon"], "precisione": "indirizzo"}
    OUT.write_text(json.dumps(out, ensure_ascii=False, indent=0), encoding="utf-8")
    from collections import Counter
    print("FINE", len(out), Counter(v["precisione"] for v in out.values()))


if __name__ == "__main__":
    main(pathlib.Path(sys.argv[1]))

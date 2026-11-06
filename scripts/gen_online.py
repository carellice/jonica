"""Genera ShopsOnline.kt dalla mappatura "Botteghe e produttori alimentari italiani con acquisti a distanza".

Usage: python scripts/gen_online.py [scripts/data/botteghe_alimentari_italiane_online.json]
"""
import json
import pathlib
import sys

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))
from gen_lazio import kstr, klist  # noqa: E402

ROOT = pathlib.Path(__file__).resolve().parent.parent
SRC = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else ROOT / "scripts/data/botteghe_alimentari_italiane_online.json"
OUT = ROOT / "app/src/main/java/it/jonica/app/data/ShopsOnline.kt"
CHUNK = 40  # elementi per funzione: evita il limite di 64KB di bytecode per metodo

CATEGORY = {
    "olio_e_frantoi": "FRANTOIO", "caseifici": "CASEIFICIO", "forni_e_panifici": "FORNO",
    "norcinerie_e_salumifici": "NORCINERIA", "macellerie": "MACELLERIA", "pescherie_e_prodotti_ittici": "PESCHERIA",
    "ortofrutta": "FRUTTA_VERDURA", "alimentari": "ALIMENTARI", "pastifici": "PASTA_FRESCA",
    "torrefazioni": "TORREFAZIONE", "miele": "APICOLTURA", "conserve": "CONSERVE", "farine_e_cereali": "MULINO",
    "gastronomia": "GASTRONOMIA", "birrifici": "BIRRIFICIO", "vini": "CANTINA", "specialita": "ALIMENTARI",
}

CHANNEL = {
    "ecommerce_con_riscontro_di_acquisto": "Negozio online con carrello",
    "negozio_online_dichiarato": "Negozio online dichiarato",
    "ordine_a_distanza_su_richiesta": "Ordine a distanza su richiesta",
    "acquisto_su_shop_del_consorzio": "Acquisto sullo shop del Consorzio",
}


def shipping(s):
    area = s.get("area_dichiarata")
    return {
        "italia_dichiarata": "Spedisce in tutta Italia",
        "locale": f"Consegna locale: {area}" if area else "Consegna locale",
        "regionale": f"Consegna in {area}" if area else "Consegna regionale",
        "multiregionale": f"Consegna in {area}" if area else "Consegna in più regioni",
    }.get(s["copertura"], "Zone di consegna da verificare sul sito")


def cap(t):
    if not t:
        return t
    return t.title() if t == t.lower() else t[0].upper() + t[1:]


def main():
    data = json.loads(SRC.read_text(encoding="utf-8"))
    rows = []
    for a in data["attivita"]:
        sede = a["sede"]
        comune = cap(sede.get("comune"))
        place = f"{comune} ({sede['provincia']})" if comune and sede.get("provincia") else comune
        cats = list(dict.fromkeys(CATEGORY[c] for c in a["categorie"])) or ["ALIMENTARI"]
        refr = a["spedizione"].get("refrigerata")
        fields = [
            f"        id = {kstr(a['id'])},",
            f"        name = {kstr(cap(a['nome']))},",
            f"        categories = listOf({', '.join('ShopCategory.' + c for c in cats)}),",
            f"        region = {kstr(sede['regione'])},",
            f"        place = {kstr(place)},",
            f"        products = {klist(a.get('prodotti_indicativi') or [])},",
            f"        shopUrl = {kstr(a['url_ordini'])},",
            f"        website = {kstr(a['sito_di_riferimento'])},",
            f"        channel = {kstr(CHANNEL.get(a['canale'], a['canale'].replace('_', ' ').capitalize()))},",
            f"        shipping = {kstr(shipping(a['spedizione']))},",
            f"        nationwide = {str(a['spedizione']['copertura'] == 'italia_dichiarata').lower()},",
            f"        chilled = {str(bool(refr)).lower()},",
            f"        family = {str('familiare' in a['profilo'] or 'cooperativa' in a['profilo']).lower()},",
            f"        note = {kstr(a.get('nota_acquisto'))},",
        ]
        rows.append("    OnlineShop(\n" + "\n".join(fields) + "\n    ),")

    nets = "".join(
        f"    OnlineNetwork({kstr(n['nome'])}, {kstr(n['url'])}, {kstr(n['descrizione'])}),\n"
        for n in data["reti_e_negozi_multimarca"]
    )
    chunks = [rows[i:i + CHUNK] for i in range(0, len(rows), CHUNK)]
    body = "\n\n".join(f"private fun part{i}(): List<OnlineShop> = listOf(\n" + "\n".join(c) + "\n)" for i, c in enumerate(chunks))
    OUT.write_text(
        "// Generato da scripts/gen_online.py dalla mappatura \"Botteghe e produttori alimentari italiani con acquisti a distanza\".\n"
        "package it.jonica.app.data\n\n"
        "val onlineShops: List<OnlineShop> by lazy { " + " + ".join(f"part{i}()" for i in range(len(chunks))) + " }\n\n"
        "/** Shop collettivi e negozi multimarca che raccolgono più produttori. */\n"
        "val onlineNetworks: List<OnlineNetwork> = listOf(\n" + nets + ")\n\n"
        + body + "\n",
        encoding="utf-8",
    )
    print(len(rows), "botteghe online,", len(data["reti_e_negozi_multimarca"]), "reti ->", OUT)


if __name__ == "__main__":
    main()

"""Generate Shops.kt from an OpenStreetMap Overpass export (osm_shops.json).

Data © OpenStreetMap contributors, ODbL.
Usage: python scripts/gen_shops.py
"""
import json
import pathlib

ROOT = pathlib.Path(__file__).resolve().parent.parent
SRC = ROOT / "scripts" / "osm_shops.json"
OUT = ROOT / "app/src/main/java/it/jonica/app/data/Shops.kt"

CATEGORY = {
    "butcher": "MACELLERIA",
    "greengrocer": "FRUTTA_VERDURA",
    "bakery": "FORNO",
    "pastry": "PASTICCERIA",
    "deli": "GASTRONOMIA",
    "cheese": "GASTRONOMIA",
    "dairy": "GASTRONOMIA",
    "seafood": "PESCHERIA",
    "wine": "ENOTECA",
    "pasta": "PASTA_FRESCA",
    "convenience": "ALIMENTARI",
    "farm": "FRUTTA_VERDURA",
    "marketplace": "MERCATO",
}

DEFAULT_NAME = {
    "MACELLERIA": "Macelleria di quartiere",
    "FRUTTA_VERDURA": "Fruttivendolo di quartiere",
    "FORNO": "Forno di quartiere",
    "PASTICCERIA": "Pasticceria di quartiere",
    "GASTRONOMIA": "Gastronomia",
    "PESCHERIA": "Pescheria",
    "ENOTECA": "Enoteca",
    "PASTA_FRESCA": "Pasta fresca",
    "ALIMENTARI": "Alimentari di quartiere",
    "MERCATO": "Mercato rionale",
}

# Not small food shops: bars/restaurants that OSM tags as shops.
EXCLUDE = {"Vintro bar & bites"}


def kstr(s):
    if s is None:
        return "null"
    return '"' + s.replace("\\", "\\\\").replace('"', '\\"').replace("$", "\\$") + '"'


def main():
    elements = json.loads(SRC.read_text(encoding="utf-8"))["elements"]
    rows = []
    for e in elements:
        t = e["tags"]
        kind = t.get("shop") or t.get("amenity")
        cat = CATEGORY.get(kind)
        if not cat or t.get("name") in EXCLUDE:
            continue
        lat = e.get("lat", e.get("center", {}).get("lat"))
        lon = e.get("lon", e.get("center", {}).get("lon"))
        street = t.get("addr:street")
        address = None
        if street:
            address = street + (" " + t["addr:housenumber"] if "addr:housenumber" in t else "")
            if "addr:postcode" in t:
                address += ", " + t["addr:postcode"]
        rows.append(
            "    Shop(\"osm_{id}\", {name}, ShopCategory.{cat}, {lat:.6f}, {lon:.6f}, {addr}, {hours}, {phone}),".format(
                id=e["id"],
                name=kstr(t.get("name") or DEFAULT_NAME[cat]),
                cat=cat,
                lat=lat,
                lon=lon,
                addr=kstr(address),
                hours=kstr(t.get("opening_hours")),
                phone=kstr(t.get("phone") or t.get("contact:phone")),
            )
        )
    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text(
        "// Generato da scripts/gen_shops.py — dati © OpenStreetMap contributors (ODbL).\n"
        "package it.jonica.app.data\n\n"
        "val shops: List<Shop> = listOf(\n" + "\n".join(rows) + "\n)\n",
        encoding="utf-8",
    )
    print(f"{len(rows)} shops -> {OUT}")


if __name__ == "__main__":
    main()

"""Taglia i fogli di icone generati (griglie trasparenti) in singoli drawable WebP.

Usage: python scripts/slice_icons.py <cartella_fogli>
"""
import pathlib
import sys

import numpy as np
from PIL import Image
from scipy import ndimage

ROOT = pathlib.Path(__file__).resolve().parent.parent
OUT = ROOT / "app/src/main/res/drawable-nodpi"
SIZE = 192

SHEETS = {
    "sheet1.png": (4, 4, [
        "arance", "mandarini", "clementine", "limoni",
        "pompelmi", "kiwi", "mele", "pere",
        "cachi", "melograni", "castagne", "uva",
        "fichi", "fichi_india", "mele_cotogne", "fragole",
    ]),
    "sheet2.png": (4, 4, [
        "nespole", "ciliegie", "albicocche", "pesche",
        "susine", "meloni", "anguria", "mirtilli",
        "lamponi", "noci", "nocciole", "carciofo",
        "puntarelle", "broccoletti", "broccolo_romanesco", "cavolfiore",
    ]),
    "sheet3.png": (4, 4, [
        "cavolo_nero", "verza", "cavoletti", "cicoria",
        "finocchi", "radicchio", "spinaci", "bietole",
        "porri", "zucca", "topinambur", "sedano",
        "rape", "porcini", "carote", "asparagi",
    ]),
    "sheet4.png": (4, 4, [
        "agretti", "fave", "piselli", "ravanelli",
        "lattuga", "rucola", "zucchine", "fiori_zucca",
        "pomodori", "melanzane", "peperoni", "cetrioli",
        "fagiolini", "patate", "cipolle", "aglio",
    ]),
    "sheet5.png": (4, 4, [
        "alici", "sardine", "sgombro", "pesce_spada",
        "triglie", "cefalo", "seppie", "calamari",
        "polpo", "spigola", "nasello", "vongole",
        "olio", "ricotta", "pecorino", "abbacchio",
    ]),
    "sheet6.png": (4, 4, [
        "miele", "vino_novello", "tartufo_bianco", "tartufo_nero",
        "legumi", "uova", "grano", "ancora",
        "cassetta", "carne", "pane", "cornetto",
        "formaggio", "pesce", "vino", "pasta",
    ]),
    "sheet7.png": (2, 4, [
        "cesto", "bancarella", "inverno", "primavera",
        "estate", "autunno", "camion", "contadino",
    ]),
    "sheet8.png": (2, 4, [
        "salumi", "caffe", "conserve", "birra",
        "gelato", "zafferano", "cascina", "mulino",
    ]),
}


def clean(cell: np.ndarray) -> np.ndarray:
    alpha = cell[:, :, 3].astype(np.float32)
    mask = alpha >= 128
    labels, n = ndimage.label(mask)
    if n == 0:
        return cell
    areas = ndimage.sum(mask, labels, range(1, n + 1))
    objs = ndimage.find_objects(labels)
    hh, ww = mask.shape

    def touches_border(sl):
        return sl[0].start == 0 or sl[1].start == 0 or sl[0].stop == hh or sl[1].stop == ww

    # scarta puntini e pezzi delle icone vicine che sconfinano nella cella
    keep_ids = [
        i + 1 for i, a in enumerate(areas)
        if a >= max(areas) * 0.04 and not (touches_border(objs[i]) and a < max(areas) * 0.5)
    ]
    keep = np.isin(labels, keep_ids)
    keep = ndimage.binary_fill_holes(keep)
    keep = ndimage.binary_dilation(keep, iterations=2)
    alpha = np.where(keep & (alpha >= 40), alpha, 0)
    out = cell.copy()
    out[:, :, 3] = alpha.astype(np.uint8)
    return out


def main(src: pathlib.Path):
    OUT.mkdir(parents=True, exist_ok=True)
    for sheet, (rows, cols, names) in SHEETS.items():
        img = np.array(Image.open(src / sheet).convert("RGBA"))
        h, w = img.shape[:2]
        for i, name in enumerate(names):
            r, c = divmod(i, cols)
            cell = img[r * h // rows:(r + 1) * h // rows, c * w // cols:(c + 1) * w // cols]
            cell = clean(cell)
            ys, xs = np.nonzero(cell[:, :, 3])
            cell = cell[ys.min():ys.max() + 1, xs.min():xs.max() + 1]
            side = int(max(cell.shape[:2]) * 1.08)
            canvas = Image.new("RGBA", (side, side), (0, 0, 0, 0))
            piece = Image.fromarray(cell)
            canvas.paste(piece, ((side - piece.width) // 2, (side - piece.height) // 2))
            canvas.resize((SIZE, SIZE), Image.LANCZOS).save(OUT / f"ic_{name}.webp", "WEBP", quality=90, method=6)
    print("ok", len(list(OUT.glob("ic_*.webp"))))


if __name__ == "__main__":
    main(pathlib.Path(sys.argv[1]))

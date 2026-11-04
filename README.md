# 🫒 Jonica

**Mangia di stagione. Compra vicino.**

Jonica è un'app Android etica che aiuta a fare una spesa più consapevole:
scoprire cosa è davvero di stagione, capire dove vanno i soldi della spesa
e trovare le botteghe di quartiere (fruttivendoli, macellerie, forni,
pescherie, mercati rionali) vicino a te.

Nasce in un periodo di proteste di agricoltori e trasportatori, schiacciati
tra prezzi bassi all'origine e costi del carburante alti: comprare di stagione
e dalle botteghe locali accorcia la filiera e lascia più valore a chi produce
e a chi vende nel quartiere.

## Cosa trovi

- **Oggi**: la data, la stagione, cosa succede nei campi questo mese (olio
  nuovo al frantoio, vendemmia, castagne, fermo pesca…), frutta e verdura al
  meglio adesso, novità e prodotti agli sgoccioli, curiosità e la bottega più
  vicina.
- **Stagioni**: calendario mese per mese di frutta, verdura, pesce del Tirreno
  e dispensa (olio, ricotta, miele, legumi…), con consigli d'acquisto per ogni
  prodotto.
- **Botteghe**: mappa e lista delle botteghe di quartiere ordinate per distanza
  dalla tua posizione (per ora zona Roma 00138 e dintorni).
- **Filiera**: dove va 1 € della spesa, perché la stagionalità conta, come
  leggere le etichette e una lista di piccoli impegni da spuntare.

## Installazione

Scarica l'APK dall'ultima [release](../../releases/latest) e installalo sul
telefono (consenti l'installazione da origini sconosciute).

## Sviluppo

Kotlin + Jetpack Compose, mappe con [osmdroid](https://github.com/osmdroid/osmdroid).
Nessun backend, nessuna chiave API: tutti i dati sono nell'app.

```bash
./gradlew assembleRelease
```

Le botteghe sono estratte da OpenStreetMap: per aggiornarle, riesegui la query
Overpass salvando il risultato in `scripts/osm_shops.json` e lancia
`python scripts/gen_shops.py`.

## Crediti

- Dati delle botteghe e mappe © [OpenStreetMap](https://www.openstreetmap.org/copyright) contributors (ODbL).
- Le stime sulla ripartizione del prezzo sono indicative e basate su studi ISMEA.
- Il calendario di stagionalità è indicativo per il Centro-Sud Italia.

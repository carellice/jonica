package it.jonica.app.data

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import it.jonica.app.R

enum class Category(val label: String, @DrawableRes val icon: Int) {
    FRUTTA("Frutta", R.drawable.ic_arance),
    VERDURA("Verdura", R.drawable.ic_broccolo_romanesco),
    MARE("Mare", R.drawable.ic_pesce),
    DISPENSA("Dispensa", R.drawable.ic_olio),
}

/** Un prodotto con i mesi (1..12) in cui è di stagione nel Centro Italia. */
data class Product(
    val name: String,
    @DrawableRes val icon: Int,
    val category: Category,
    val months: Set<Int>,
    val tip: String,
    val origin: String? = null,
) {
    fun inSeason(month: Int) = month in months
    fun isArriving(month: Int) = inSeason(month) && !inSeason(prevMonth(month))
    fun isLeaving(month: Int) = inSeason(month) && !inSeason(nextMonth(month))
}

/** Un momento della filiera: olio nuovo, vendemmia, fermo pesca... */
data class FarmEvent(
    val title: String,
    @DrawableRes val icon: Int,
    val months: Set<Int>,
    val description: String,
    val advice: String,
)

enum class ShopCategory(val label: String, @DrawableRes val icon: Int, val color: Color) {
    FRUTTA_VERDURA("Frutta e verdura", R.drawable.ic_cassetta, Color(0xFF5C8A3A)),
    MACELLERIA("Macelleria", R.drawable.ic_carne, Color(0xFFB5452F)),
    FORNO("Forno", R.drawable.ic_pane, Color(0xFFC98A2E)),
    PASTICCERIA("Pasticceria", R.drawable.ic_cornetto, Color(0xFFD9708A)),
    GASTRONOMIA("Gastronomia", R.drawable.ic_formaggio, Color(0xFFE0A526)),
    PESCHERIA("Pescheria", R.drawable.ic_pesce, Color(0xFF2E7DA6)),
    ENOTECA("Enoteca", R.drawable.ic_vino, Color(0xFF7D2C4A)),
    PASTA_FRESCA("Pasta fresca", R.drawable.ic_pasta, Color(0xFFD99A3C)),
    ALIMENTARI("Alimentari", R.drawable.ic_cesto, Color(0xFF6B7A3A)),
    MERCATO("Mercato rionale", R.drawable.ic_bancarella, Color(0xFF3E6B8C)),
    CASEIFICIO("Caseificio", R.drawable.ic_ricotta, Color(0xFFB89A4E)),
    NORCINERIA("Norcineria", R.drawable.ic_salumi, Color(0xFFA8473A)),
    FRANTOIO("Frantoio", R.drawable.ic_olio, Color(0xFF7A8C2E)),
    CANTINA("Cantina", R.drawable.ic_uva, Color(0xFF6E3A6B)),
    TORREFAZIONE("Torrefazione", R.drawable.ic_caffe, Color(0xFF7A5236)),
    MULINO("Mulino", R.drawable.ic_mulino, Color(0xFFB0843A)),
    APICOLTURA("Miele", R.drawable.ic_miele, Color(0xFFD69A1E)),
    CONSERVE("Conserve", R.drawable.ic_conserve, Color(0xFFC44A32)),
    BIRRIFICIO("Birrificio", R.drawable.ic_birra, Color(0xFFC7891F)),
    GELATERIA("Gelateria", R.drawable.ic_gelato, Color(0xFF6FA08A)),
    SPEZIE("Spezie e zafferano", R.drawable.ic_zafferano, Color(0xFF8A62B0)),
    AZIENDA_AGRICOLA("Azienda agricola", R.drawable.ic_cascina, Color(0xFF5E8A3C)),
}

/** Da dove arriva la scheda: OpenStreetMap, mappatura approfondita o elenco regionale da verificare. */
enum class ShopSource { OSM, SCHEDA, REGISTRO }

data class Shop(
    val id: String,
    val name: String,
    val category: ShopCategory,
    val lat: Double,
    val lon: Double,
    val address: String?,
    val hours: String?,
    val phone: String?,
    val categories: List<ShopCategory> = listOf(category),
    val comune: String? = null,
    val provincia: String? = null,
    val description: String? = null,
    val products: List<String> = emptyList(),
    val channels: List<String> = emptyList(),
    val alsoAt: List<String> = emptyList(),
    val website: String? = null,
    val email: String? = null,
    val note: String? = null,
    /** true se la posizione è il centro del comune e non l'indirizzo esatto. */
    val approximate: Boolean = false,
    val source: ShopSource = ShopSource.OSM,
    /** Gestione personale, familiare o cooperativa documentata dalle fonti. */
    val family: Boolean = false,
    /** Scheda Google Maps del punto vendita, quando la mappatura la riporta. */
    val mapsUrl: String? = null,
) {
    val place: String? get() = comune?.let { c -> provincia?.let { "$c ($it)" } ?: c }
}

/** Bottega o produttore italiano che vende online o accetta ordini a distanza. */
data class OnlineShop(
    val id: String,
    val name: String,
    val categories: List<ShopCategory>,
    val region: String,
    val place: String?,
    val products: List<String>,
    /** Pagina del negozio o degli ordini. */
    val shopUrl: String,
    val website: String,
    val channel: String,
    val shipping: String,
    val nationwide: Boolean,
    /** Spedizione refrigerata dichiarata. */
    val chilled: Boolean,
    val family: Boolean,
    val note: String?,
) {
    val category: ShopCategory get() = categories.first()
}

/** Shop collettivo o multimarca di più produttori. */
data class OnlineNetwork(val name: String, val url: String, val description: String)

/**
 * Tutte le botteghe: quartiere 00138 (OpenStreetMap), mappatura del Lazio e mappatura di Roma.
 * Le schede di Roma sono le più recenti e complete: sostituiscono i doppioni delle altre due.
 */
val allShops: List<Shop> by lazy { (shops + lazioShops).filterNot { it.id in romaReplaces } + romaShops }

fun prevMonth(m: Int) = if (m == 1) 12 else m - 1
fun nextMonth(m: Int) = if (m == 12) 1 else m + 1

val monthNames = listOf(
    "Gennaio", "Febbraio", "Marzo", "Aprile", "Maggio", "Giugno",
    "Luglio", "Agosto", "Settembre", "Ottobre", "Novembre", "Dicembre",
)
val monthShort = listOf("G", "F", "M", "A", "M", "G", "L", "A", "S", "O", "N", "D")

fun seasonOf(month: Int): Pair<String, Int> = when (month) {
    12, 1, 2 -> "Inverno" to R.drawable.ic_inverno
    3, 4, 5 -> "Primavera" to R.drawable.ic_primavera
    6, 7, 8 -> "Estate" to R.drawable.ic_estate
    else -> "Autunno" to R.drawable.ic_autunno
}

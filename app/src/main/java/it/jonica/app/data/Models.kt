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
}

data class Shop(
    val id: Long,
    val name: String,
    val category: ShopCategory,
    val lat: Double,
    val lon: Double,
    val address: String?,
    val hours: String?,
    val phone: String?,
)

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

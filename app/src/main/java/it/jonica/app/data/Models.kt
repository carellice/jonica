package it.jonica.app.data

import androidx.compose.ui.graphics.Color

enum class Category(val label: String, val emoji: String) {
    FRUTTA("Frutta", "🍊"),
    VERDURA("Verdura", "🥦"),
    MARE("Mare", "🐟"),
    DISPENSA("Dispensa", "🫒"),
}

/** Un prodotto con i mesi (1..12) in cui è di stagione nel Centro Italia. */
data class Product(
    val name: String,
    val emoji: String,
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
    val emoji: String,
    val months: Set<Int>,
    val description: String,
    val advice: String,
)

enum class ShopCategory(val label: String, val emoji: String, val color: Color) {
    FRUTTA_VERDURA("Frutta e verdura", "🥬", Color(0xFF5C8A3A)),
    MACELLERIA("Macelleria", "🥩", Color(0xFFB5452F)),
    FORNO("Forno", "🥖", Color(0xFFC98A2E)),
    PASTICCERIA("Pasticceria", "🥐", Color(0xFFD9708A)),
    GASTRONOMIA("Gastronomia", "🧀", Color(0xFFE0A526)),
    PESCHERIA("Pescheria", "🐟", Color(0xFF2E7DA6)),
    ENOTECA("Enoteca", "🍷", Color(0xFF7D2C4A)),
    PASTA_FRESCA("Pasta fresca", "🍝", Color(0xFFD99A3C)),
    ALIMENTARI("Alimentari", "🧺", Color(0xFF6B7A3A)),
    MERCATO("Mercato rionale", "⛺", Color(0xFF3E6B8C)),
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

fun seasonOf(month: Int): Pair<String, String> = when (month) {
    12, 1, 2 -> "Inverno" to "❄️"
    3, 4, 5 -> "Primavera" to "🌸"
    6, 7, 8 -> "Estate" to "☀️"
    else -> "Autunno" to "🍂"
}

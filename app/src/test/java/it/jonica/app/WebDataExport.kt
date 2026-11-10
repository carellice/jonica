package it.jonica.app

import it.jonica.app.data.allShops
import it.jonica.app.data.facts
import it.jonica.app.data.farmEvents
import it.jonica.app.data.onlineNetworks
import it.jonica.app.data.onlineShops
import it.jonica.app.data.products
import it.jonica.app.data.ShopCategory
import it.jonica.app.ui.aware.labelTips
import it.jonica.app.ui.aware.pledges
import it.jonica.app.ui.aware.slices
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Scrive web/data.json per la versione web dagli stessi elenchi dell'app: prodotti, momenti della
 * filiera, curiosità, botteghe e contenuti di Filiera. Le icone sono indicate col nome del drawable,
 * che il sito trova in web/icons/.
 */
class WebDataExport {
    private val iconNames: Map<Int, String> =
        R.drawable::class.java.fields.associate { it.getInt(null) to it.name }

    private fun icon(id: Int) = iconNames[id] ?: error("drawable $id")

    @Test
    fun exportWebData() {
        val out = System.getProperty("jonica.webData") ?: return
        val json = obj(
            "products" to products.map {
                obj("name" to it.name, "icon" to icon(it.icon), "category" to it.category.label, "months" to it.months.sorted(), "tip" to it.tip, "origin" to it.origin)
            },
            "events" to farmEvents.map {
                obj("title" to it.title, "icon" to icon(it.icon), "months" to it.months.sorted(), "description" to it.description, "advice" to it.advice)
            },
            "facts" to facts.map { (i, text) -> obj("icon" to icon(i), "text" to text) },
            "shopCategories" to ShopCategory.entries.map {
                obj("id" to it.name, "label" to it.label, "icon" to icon(it.icon), "color" to hex(it.color.value))
            },
            "shops" to allShops.map {
                obj(
                    "id" to it.id, "name" to it.name, "categories" to it.categories.map { c -> c.name }, "lat" to it.lat, "lon" to it.lon,
                    "address" to it.address, "hours" to it.hours, "phone" to it.phone, "place" to it.place,
                    "description" to it.description, "products" to it.products, "channels" to it.channels,
                    "website" to it.website, "email" to it.email, "note" to it.note, "approximate" to it.approximate,
                    "family" to it.family, "mapsUrl" to it.mapsUrl,
                )
            },
            "online" to onlineShops.map {
                obj(
                    "id" to it.id, "name" to it.name, "categories" to it.categories.map { c -> c.name }, "region" to it.region,
                    "place" to it.place, "products" to it.products, "shopUrl" to it.shopUrl, "website" to it.website,
                    "channel" to it.channel, "shipping" to it.shipping, "nationwide" to it.nationwide, "chilled" to it.chilled,
                    "family" to it.family, "note" to it.note,
                )
            },
            "networks" to onlineNetworks.map { obj("name" to it.name, "url" to it.url, "description" to it.description) },
            "slices" to slices.map { obj("label" to it.label, "cents" to it.cents, "color" to hex(it.color.value), "icon" to icon(it.icon)) },
            "pledges" to pledges.map { (i, text) -> obj("icon" to icon(i), "text" to text) },
            "labelTips" to labelTips.map { (title, text) -> obj("title" to title, "text" to text) },
        )
        File(out).apply { parentFile?.mkdirs() }.writeText(json.text + "\n")
        assertTrue(products.isNotEmpty() && allShops.isNotEmpty())
    }

    // Il colore di Compose tiene ARGB nei 32 bit alti del valore (spazio sRGB).
    private fun hex(value: ULong) = "#%06X".format((value shr 32).toLong() and 0xFFFFFF)

    /** JSON già scritto, da non racchiudere tra virgolette. */
    private class Json(val text: String)

    private fun obj(vararg pairs: Pair<String, Any?>) =
        Json(pairs.filter { it.second != null }.joinToString(",", "{", "}") { (k, v) -> "${str(k)}:${value(v)}" })

    private fun value(v: Any?): String = when (v) {
        null -> "null"
        is Json -> v.text
        is String -> str(v)
        is Boolean, is Int, is Long, is Double -> v.toString()
        is Iterable<*> -> v.joinToString(",", "[", "]") { value(it) }
        else -> str(v.toString())
    }

    private fun str(s: String) = buildString {
        append('"')
        for (c in s) when (c) {
            '"' -> append("\\\"")
            '\\' -> append("\\\\")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            else -> if (c < ' ') append("\\u%04x".format(c.code)) else append(c)
        }
        append('"')
    }
}

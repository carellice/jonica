package it.jonica.app.ui.shops

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Directions
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import it.jonica.app.data.Shop
import it.jonica.app.data.ShopCategory
import it.jonica.app.data.shops
import it.jonica.app.location.Default00138
import it.jonica.app.location.GeoPoint
import it.jonica.app.location.distanceMeters
import it.jonica.app.location.formatDistance
import it.jonica.app.ui.components.EmojiBubble
import it.jonica.app.ui.components.Pill
import it.jonica.app.ui.components.WavyHeader
import it.jonica.app.ui.theme.Sky
import it.jonica.app.ui.components.bouncyClick
import it.jonica.app.ui.components.staggeredEnter
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.util.GeoPoint as OsmPoint

/** Tinta calda "carta da pacchi" sulle tile OSM; in tema scuro inverte e scalda i colori. */
private fun tileFilter(dark: Boolean): ColorMatrixColorFilter {
    val m = if (dark) {
        ColorMatrix(floatArrayOf(
            -0.85f, 0f, 0f, 0f, 235f,
            0f, -0.85f, 0f, 0f, 230f,
            0f, 0f, -0.85f, 0f, 205f,
            0f, 0f, 0f, 1f, 0f,
        )).apply { postConcat(ColorMatrix().apply { setSaturation(0.35f) }) }
    } else {
        ColorMatrix().apply { setSaturation(0.55f) }.apply {
            postConcat(ColorMatrix(floatArrayOf(
                1.02f, 0f, 0f, 0f, 8f,
                0f, 1f, 0f, 0f, 2f,
                0f, 0f, 0.9f, 0f, -6f,
                0f, 0f, 0f, 1f, 0f,
            )))
        }
    }
    return ColorMatrixColorFilter(m)
}

@Composable
fun ShopsScreen(location: GeoPoint?, permissionGranted: Boolean, onRequestPermission: () -> Unit) {
    val context = LocalContext.current
    val dark = isSystemInDarkTheme()
    var filter by remember { mutableStateOf<ShopCategory?>(null) }
    var selected by remember { mutableStateOf<Shop?>(null) }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    val here = location ?: Default00138

    val visible = remember(filter, here) {
        shops.filter { filter == null || it.category == filter }
            .map { it to distanceMeters(here, it.lat, it.lon) }
            .sortedBy { it.second }
    }
    val categories = remember { ShopCategory.entries.filter { c -> shops.any { it.category == c } } }

    Column(Modifier.fillMaxSize()) {
        WavyHeader(color = Sky, secondColor = Color(0xFF5E93A6), floating = listOf("🥖", "🧀", "🥬", "🐟", "🍷", "🥩")) {
            Column(Modifier.statusBarsPadding().padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 44.dp)) {
                Text("Botteghe di quartiere", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                Text(
                    if (location != null) "Le più vicine a te, tra Fidene, Serpentara, Val Melaina e dintorni."
                    else "Zona 00138 · attiva la posizione per ordinarle in base a dove sei.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.88f),
                )
            }
        }
        Spacer(Modifier.height(12.dp))

        Box(
            Modifier
                .padding(horizontal = 16.dp)
                .offset(y = (-20).dp)
                .fillMaxWidth()
                .height(270.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            OsmMap(
                dark = dark,
                center = here,
                userLocation = location,
                shops = visible.map { it.first },
                onShopClick = { selected = it },
                onReady = { mapView = it },
            )
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .bouncyClick {
                        if (!permissionGranted) onRequestPermission()
                        mapView?.controller?.animateTo(OsmPoint(here.lat, here.lon), 14.6, 700L)
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.MyLocation, "La mia posizione", tint = MaterialTheme.colorScheme.primary)
            }
            Text(
                "© OpenStreetMap contributors",
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            )
        }

        Spacer(Modifier.height(12.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item { FilterChip("✨ Tutte", filter == null) { filter = null } }
            items(categories) { c -> FilterChip("${c.emoji} ${c.label}", filter == c) { filter = if (filter == c) null else c } }
        }

        if (!permissionGranted) {
            Row(
                Modifier
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .bouncyClick(onRequestPermission)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("📍", fontSize = 22.sp)
                Spacer(Modifier.width(10.dp))
                Text(
                    "Attiva la posizione per vedere le botteghe più vicine a te",
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            itemsIndexed(visible, key = { _, it -> it.first.id }) { i, (shop, dist) ->
                ShopRow(shop, dist, Modifier.animateItem().staggeredEnter(i, filter)) {
                    selected = shop
                    mapView?.controller?.animateTo(OsmPoint(shop.lat, shop.lon), 17.0, 700L)
                }
            }
            item {
                Text(
                    "Dati © OpenStreetMap contributors. Manca la tua bottega preferita? Aggiungila su openstreetmap.org: è gratis e aiuta tutti.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(8.dp),
                )
            }
        }
    }

    selected?.let { s ->
        ShopSheet(s, distanceMeters(here, s.lat, s.lon), location != null, context) { selected = null }
    }
}

@Composable
private fun FilterChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val bg by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer,
        tween(250), label = "chip",
    )
    Text(
        text,
        color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .bouncyClick(onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
    )
}

@Composable
private fun ShopRow(shop: Shop, dist: Float, modifier: Modifier, onClick: () -> Unit) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .bouncyClick(onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EmojiBubble(shop.category.emoji, 50.dp, shop.category.color.copy(alpha = 0.18f))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(shop.name, style = MaterialTheme.typography.titleMedium, maxLines = 1)
            Text(
                shop.address ?: shop.category.label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
        Spacer(Modifier.width(8.dp))
        Pill(formatDistance(dist), shop.category.color.copy(alpha = 0.16f), shop.category.color)
    }
}

@Composable
private fun OsmMap(
    dark: Boolean,
    center: GeoPoint,
    userLocation: GeoPoint?,
    shops: List<Shop>,
    onShopClick: (Shop) -> Unit,
    onReady: (MapView) -> Unit,
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val map = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            overlayManager.tilesOverlay.setColorFilter(tileFilter(dark))
            overlayManager.tilesOverlay.loadingBackgroundColor = if (dark) 0xFF1C1E15.toInt() else 0xFFF1EBDD.toInt()
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            isTilesScaledToDpi = true
            controller.setZoom(14.6)
            controller.setCenter(OsmPoint(center.lat, center.lon))
            minZoomLevel = 11.0
        }
    }
    DisposableEffect(lifecycle) {
        val obs = LifecycleEventObserver { _, e ->
            when (e) {
                Lifecycle.Event.ON_RESUME -> map.onResume()
                Lifecycle.Event.ON_PAUSE -> map.onPause()
                else -> Unit
            }
        }
        lifecycle.addObserver(obs)
        map.onResume()
        onReady(map)
        onDispose {
            lifecycle.removeObserver(obs)
            map.onPause()
            map.onDetach()
        }
    }
    var centeredOnUser by remember { mutableStateOf(false) }
    LaunchedEffect(userLocation) {
        if (userLocation != null && !centeredOnUser) {
            centeredOnUser = true
            map.controller.animateTo(OsmPoint(userLocation.lat, userLocation.lon), 14.6, 900L)
        }
    }
    AndroidView(
        factory = { map },
        modifier = Modifier.fillMaxSize(),
        update = { m ->
            m.overlays.clear()
            shops.forEach { s ->
                m.overlays.add(
                    Marker(m).apply {
                        position = OsmPoint(s.lat, s.lon)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        icon = markerIcon(context, s.category.emoji, s.category.color.toArgb())
                        title = s.name
                        setOnMarkerClickListener { _, _ -> onShopClick(s); true }
                    },
                )
            }
            userLocation?.let { u ->
                m.overlays.add(
                    Marker(m).apply {
                        position = OsmPoint(u.lat, u.lon)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        icon = userIcon(context)
                        setInfoWindow(null)
                        setOnMarkerClickListener { _, _ -> true }
                    },
                )
            }
            m.invalidate()
        },
    )
}

private val iconCache = HashMap<String, BitmapDrawable>()

private fun markerIcon(context: Context, emoji: String, color: Int): BitmapDrawable = iconCache.getOrPut(emoji) {
    val d = context.resources.displayMetrics.density
    val size = (40 * d).toInt()
    val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val c = Canvas(bmp)
    val p = Paint(Paint.ANTI_ALIAS_FLAG)
    p.color = 0x33000000
    c.drawCircle(size / 2f, size / 2f + 2 * d, size / 2f - 2 * d, p)
    p.color = android.graphics.Color.WHITE
    c.drawCircle(size / 2f, size / 2f, size / 2f - 2 * d, p)
    p.color = color
    c.drawCircle(size / 2f, size / 2f, size / 2f - 5 * d, p)
    p.textSize = 18 * d
    p.textAlign = Paint.Align.CENTER
    p.color = android.graphics.Color.WHITE
    val y = size / 2f - (p.descent() + p.ascent()) / 2f
    c.drawText(emoji, size / 2f, y, p)
    BitmapDrawable(context.resources, bmp)
}

private fun userIcon(context: Context): BitmapDrawable = iconCache.getOrPut("__user") {
    val d = context.resources.displayMetrics.density
    val size = (30 * d).toInt()
    val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val c = Canvas(bmp)
    val p = Paint(Paint.ANTI_ALIAS_FLAG)
    p.color = 0x552E7DD6
    c.drawCircle(size / 2f, size / 2f, size / 2f, p)
    p.color = android.graphics.Color.WHITE
    c.drawCircle(size / 2f, size / 2f, 8 * d, p)
    p.color = 0xFF2E7DD6.toInt()
    c.drawCircle(size / 2f, size / 2f, 6 * d, p)
    BitmapDrawable(context.resources, bmp)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShopSheet(shop: Shop, dist: Float, fromUser: Boolean, context: Context, onDismiss: () -> Unit) {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val inf = rememberInfiniteTransition(label = "pulse")
    val pulse by inf.animateFloat(1f, 1.08f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "p")
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = state, containerColor = MaterialTheme.colorScheme.surface) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.graphicsLayer { scaleX = pulse; scaleY = pulse }) {
                    EmojiBubble(shop.category.emoji, 72.dp, shop.category.color.copy(alpha = 0.2f))
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(shop.name, style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Pill(shop.category.label, shop.category.color.copy(alpha = 0.16f), shop.category.color)
                        Pill(
                            formatDistance(dist) + if (fromUser) " da te" else " da 00138",
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            InfoLine("📍", shop.address ?: "Indirizzo non indicato · Roma")
            shop.hours?.let { InfoLine("🕒", it) }
            shop.phone?.let { InfoLine("📞", it) }
            Spacer(Modifier.height(8.dp))
            AnimatedVisibility(true, enter = fadeIn() + slideInVertically { it / 2 }, exit = fadeOut()) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.tertiaryContainer)
                        .padding(16.dp),
                ) {
                    Text(
                        "💚 " + adviceFor(shop.category),
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        val uri = Uri.parse("geo:${shop.lat},${shop.lon}?q=${shop.lat},${shop.lon}(${Uri.encode(shop.name)})")
                        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }.onFailure {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse("https://www.openstreetmap.org/?mlat=${shop.lat}&mlon=${shop.lon}#map=18/${shop.lat}/${shop.lon}")),
                            )
                        }
                    },
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.weight(1f).height(52.dp),
                ) {
                    Icon(Icons.Rounded.Directions, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Portami qui")
                }
                shop.phone?.let { phone ->
                    OutlinedButton(
                        onClick = { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone.replace(" ", "")))) },
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.height(52.dp),
                    ) {
                        Icon(Icons.Rounded.Call, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Chiama")
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoLine(icon: String, text: String) {
    Row(Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(icon, fontSize = 18.sp)
        Spacer(Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}

private fun adviceFor(c: ShopCategory) = when (c) {
    ShopCategory.FRUTTA_VERDURA -> "Chiedi da dove arriva la merce: un buon fruttivendolo conosce i suoi produttori e sa consigliarti cosa è al meglio oggi."
    ShopCategory.MACELLERIA -> "Il macellaio di fiducia sa dirti l'allevamento di provenienza. Prova i tagli meno noti: costano meno e non si sprecano."
    ShopCategory.FORNO -> "Il pane del forno di quartiere dura di più di quello industriale. Quello del giorno prima? Perfetto per bruschette e panzanella."
    ShopCategory.PASTICCERIA -> "Le pasticcerie artigianali seguono le stagioni: castagne d'autunno, agrumi d'inverno, frutti rossi d'estate."
    ShopCategory.GASTRONOMIA -> "Compra formaggi e salumi al taglio, nella quantità che ti serve: meno plastica, meno sprechi."
    ShopCategory.PESCHERIA -> "Chiedi il pescato del giorno e la zona FAO: il pesce del Mediterraneo (zona 37) ha viaggiato meno."
    ShopCategory.ENOTECA -> "Lasciati consigliare un vino del Lazio: Castelli Romani, Cesanese, Tuscia. Spesso il rapporto qualità-prezzo sorprende."
    ShopCategory.PASTA_FRESCA -> "La pasta fresca fatta in bottega dà lavoro nel quartiere ed è senza conservanti. Chiedi la farina che usano."
    ShopCategory.ALIMENTARI -> "Gli alimentari di quartiere sono un presidio sociale: conoscono i clienti, fanno credito, consegnano a chi non può uscire."
    ShopCategory.MERCATO -> "Al mercato rionale cerca i banchi dei produttori diretti: vendono quello che coltivano, a prezzi spesso più giusti."
}

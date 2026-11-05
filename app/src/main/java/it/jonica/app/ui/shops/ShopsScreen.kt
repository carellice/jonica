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
import it.jonica.app.data.allShops
import it.jonica.app.data.ShopSource
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ShoppingBasket
import androidx.compose.material.icons.rounded.Storefront
import it.jonica.app.location.Default00138
import it.jonica.app.location.GeoPoint
import it.jonica.app.location.distanceMeters
import it.jonica.app.location.formatDistance
import it.jonica.app.ui.components.IconBubble
import it.jonica.app.ui.components.ScreenPadding
import androidx.annotation.DrawableRes
import android.graphics.BitmapFactory
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Schedule
import it.jonica.app.ui.components.Pill
import it.jonica.app.ui.components.WavyHeader
import it.jonica.app.ui.theme.Sky
import it.jonica.app.ui.theme.readable
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
    var onlyVerified by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<Shop?>(null) }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    val here = location ?: Default00138

    val visible = remember(filter, onlyVerified, query, here) {
        val q = query.trim().normalized()
        allShops.asSequence()
            .filter { filter == null || filter in it.categories }
            .filter { !onlyVerified || it.source != ShopSource.REGISTRO }
            .filter { q.isEmpty() || it.searchText.contains(q) }
            .map { it to distanceMeters(here, it.lat, it.lon) }
            .sortedBy { it.second }
            .toList()
    }
    // categorie ordinate per numero di botteghe
    val categories = remember {
        ShopCategory.entries
            .map { c -> c to allShops.count { c in it.categories } }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .map { it.first }
    }

    Column(Modifier.fillMaxSize()) {
        WavyHeader(color = Sky, secondColor = Color(0xFF5E93A6), floating = ShopCategory.entries.take(6).map { it.icon }) {
            Column(Modifier.statusBarsPadding().padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 44.dp)) {
                Text("Botteghe e produttori", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                Text(
                    if (location != null) "${allShops.size} botteghe nel Lazio, dalla più vicina a te."
                    else "${allShops.size} botteghe nel Lazio · attiva la posizione per ordinarle in base a dove sei.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.88f),
                )
            }
        }
        Spacer(Modifier.height(12.dp))

        Box(
            Modifier
                .padding(horizontal = ScreenPadding)
                .offset(y = (-20).dp)
                .fillMaxWidth()
                .height(230.dp)
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

        SearchField(query, { query = it }, Modifier.offset(y = (-8).dp))
        Spacer(Modifier.height(4.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = ScreenPadding),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item { FilterChip(null, "Tutte", filter == null) { filter = null } }
            item { FilterChip(null, "Solo verificate", onlyVerified) { onlyVerified = !onlyVerified } }
            items(categories) { c -> FilterChip(c.icon, c.label, filter == c) { filter = if (filter == c) null else c } }
        }

        if (!permissionGranted) {
            Row(
                Modifier
                    .padding(horizontal = ScreenPadding, vertical = 10.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .bouncyClick(onRequestPermission)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Rounded.LocationOn, null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
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
            contentPadding = PaddingValues(start = ScreenPadding, end = ScreenPadding, top = 8.dp, bottom = 130.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Text(
                    if (visible.size == 1) "1 bottega" else "${visible.size} botteghe",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
            if (visible.isEmpty()) {
                item {
                    Text(
                        "Nessuna bottega trovata. Prova a cambiare filtro o ricerca.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
            itemsIndexed(visible, key = { _, it -> it.first.id }) { i, (shop, dist) ->
                ShopRow(shop, dist, Modifier.animateItem().staggeredEnter(i, filter to onlyVerified)) {
                    selected = shop
                    mapView?.controller?.animateTo(OsmPoint(shop.lat, shop.lon), if (shop.approximate) 14.0 else 17.0, 700L)
                }
            }
            item {
                Text(
                    "Botteghe del Lazio: ricerca su fonti pubbliche (siti dei produttori, mercati contadini, reti locali ed elenco regionale delle botteghe storiche). Le schede \"da verificare\" vengono dall'elenco regionale e non sono state confermate: chiama prima di andare. Quartiere 00138 e mappe © OpenStreetMap contributors.",
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
private fun FilterChip(@DrawableRes icon: Int?, text: String, selected: Boolean, onClick: () -> Unit) {
    val bg by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer,
        tween(250), label = "chip",
    )
    Row(
        Modifier
            .height(40.dp)
            .clip(RoundedCornerShape(50))
            .background(bg)
            .bouncyClick(onClick)
            .padding(start = if (icon != null) 6.dp else 16.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            androidx.compose.foundation.Image(androidx.compose.ui.res.painterResource(icon), null, Modifier.size(28.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text,
            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun ShopRow(shop: Shop, dist: Float, modifier: Modifier, onClick: () -> Unit) {
    Row(
        modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .bouncyClick(onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconBubble(shop.category.icon, 52.dp, shop.category.color.copy(alpha = 0.16f))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    shop.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (shop.source == ShopSource.REGISTRO) {
                    Spacer(Modifier.width(6.dp))
                    Box(Modifier.size(7.dp).clip(CircleShape).background(MaterialTheme.colorScheme.tertiary))
                }
            }
            Text(
                listOfNotNull(shop.category.label, shop.place ?: shop.address).joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(8.dp))
        Pill(formatDistance(dist), shop.category.color.copy(alpha = 0.16f), shop.category.color.readable(), Modifier.width(84.dp))
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
            minZoomLevel = 7.0
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
                        icon = markerIcon(context, s.category.icon, s.category.color.toArgb())
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

private fun markerIcon(context: Context, @DrawableRes res: Int, color: Int): BitmapDrawable = iconCache.getOrPut("m$res") {
    val d = context.resources.displayMetrics.density
    val size = (44 * d).toInt()
    val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val c = Canvas(bmp)
    val p = Paint(Paint.ANTI_ALIAS_FLAG)
    p.color = 0x33000000
    c.drawCircle(size / 2f, size / 2f + 2 * d, size / 2f - 2 * d, p)
    p.color = color
    c.drawCircle(size / 2f, size / 2f, size / 2f - 2 * d, p)
    p.color = android.graphics.Color.WHITE
    c.drawCircle(size / 2f, size / 2f, size / 2f - 5 * d, p)
    val art = BitmapFactory.decodeResource(context.resources, res)
    val inset = (10 * d).toInt()
    c.drawBitmap(art, null, android.graphics.Rect(inset, inset, size - inset, size - inset), Paint(Paint.FILTER_BITMAP_FLAG))
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ShopSheet(shop: Shop, dist: Float, fromUser: Boolean, context: Context, onDismiss: () -> Unit) {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val inf = rememberInfiniteTransition(label = "pulse")
    val pulse by inf.animateFloat(1f, 1.08f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "p")
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = state, containerColor = MaterialTheme.colorScheme.surface) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.graphicsLayer { scaleX = pulse; scaleY = pulse }) {
                    IconBubble(shop.category.icon, 72.dp, shop.category.color.copy(alpha = 0.18f))
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(shop.name, style = MaterialTheme.typography.headlineSmall)
                    shop.place?.let {
                        Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(6.dp))
                    Pill(
                        formatDistance(dist) + if (fromUser) " da te" else " da 00138",
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                shop.categories.forEach { c -> Pill(c.label, c.color.copy(alpha = 0.16f), c.color.readable()) }
                if (shop.family) Pill("Gestione familiare", MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
                if (shop.source == ShopSource.REGISTRO) Pill("Da verificare", MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
            }
            shop.description?.let {
                Spacer(Modifier.height(14.dp))
                Text(it, style = MaterialTheme.typography.bodyLarge)
            }
            if (shop.products.isNotEmpty()) {
                Spacer(Modifier.height(14.dp))
                Text("Prodotti segnalati", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    shop.products.forEach { Pill(it, MaterialTheme.colorScheme.surfaceContainer, MaterialTheme.colorScheme.onSurface) }
                }
            }
            Spacer(Modifier.height(14.dp))
            InfoLine(Icons.Rounded.Place, shop.address ?: "Indirizzo non indicato")
            if (shop.approximate) {
                InfoLine(Icons.Rounded.Info, "Sulla mappa è al centro del comune: l'indirizzo esatto non è stato trovato.")
            }
            shop.hours?.let { InfoLine(Icons.Rounded.Schedule, it) }
            shop.phone?.let { InfoLine(Icons.Rounded.Phone, it) }
            if (shop.channels.isNotEmpty()) InfoLine(Icons.Rounded.ShoppingBasket, shop.channels.joinToString(" · "))
            shop.alsoAt.forEach { InfoLine(Icons.Rounded.Storefront, "Anche al $it (verifica giorni e presenza)") }
            Spacer(Modifier.height(8.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.tertiaryContainer)
                    .padding(16.dp),
            ) {
                Row {
                    Icon(Icons.Rounded.Lightbulb, null, tint = MaterialTheme.colorScheme.onTertiaryContainer, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        if (shop.source == ShopSource.OSM) adviceFor(shop.category) else shop.note ?: adviceFor(shop.category),
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    // posizione approssimativa: meglio far cercare nome e indirizzo all'app di mappe
                    val uri = if (shop.approximate) {
                        Uri.parse("geo:0,0?q=" + Uri.encode(listOfNotNull(shop.name, shop.address, shop.comune).joinToString(" ")))
                    } else {
                        Uri.parse("geo:${shop.lat},${shop.lon}?q=${shop.lat},${shop.lon}(${Uri.encode(shop.name)})")
                    }
                    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }.onFailure {
                        context.startActivity(
                            Intent(Intent.ACTION_VIEW, Uri.parse("https://www.openstreetmap.org/?mlat=${shop.lat}&mlon=${shop.lon}#map=16/${shop.lat}/${shop.lon}")),
                        )
                    }
                },
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                Icon(Icons.Rounded.Directions, null)
                Spacer(Modifier.width(8.dp))
                Text("Portami qui")
            }
            val phone = shop.phone
            val site = shop.website
            if (phone != null || site != null) {
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (phone != null) {
                        OutlinedButton(
                            onClick = { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone.replace(" ", "")))) },
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.weight(1f).height(52.dp),
                        ) {
                            Icon(Icons.Rounded.Call, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Chiama")
                        }
                    }
                    if (site != null) {
                        OutlinedButton(
                            onClick = {
                                val url = if (site.startsWith("http")) site else "https://$site"
                                runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
                            },
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.weight(1f).height(52.dp),
                        ) {
                            Icon(Icons.Rounded.Language, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Sito web")
                        }
                    }
                }
            }
        }
    }
}

/** Campo di ricerca a pillola: nome, comune, prodotti o categoria. */
@Composable
private fun SearchField(value: String, onChange: (String) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .padding(horizontal = ScreenPadding)
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(10.dp))
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text("Cerca per nome, comune o prodotto", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            }
            BasicTextField(
                value = value,
                onValueChange = onChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (value.isNotEmpty()) {
            Icon(
                Icons.Rounded.Close, "Cancella",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp).bouncyClick { onChange("") },
            )
        }
    }
}

private val accents = Regex("\\p{M}+")

private fun String.normalized(): String =
    java.text.Normalizer.normalize(lowercase(), java.text.Normalizer.Form.NFD).replace(accents, "")

private val searchCache = HashMap<String, String>()

private val Shop.searchText: String
    get() = searchCache.getOrPut(id) {
        (listOf(name, comune.orEmpty(), address.orEmpty(), description.orEmpty()) + products + categories.map { it.label })
            .joinToString(" ").normalized()
    }

@Composable
private fun InfoLine(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
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
    ShopCategory.CASEIFICIO -> "Chiedi i formaggi freschi del giorno e da quale latte nascono: il caseificio ti racconta il suo territorio."
    ShopCategory.NORCINERIA -> "I salumi artigiani seguono i tempi della stagionatura: chiedi il taglio e la provenienza della carne."
    ShopCategory.FRANTOIO -> "Tra novembre e gennaio c'è l'olio nuovo: comprarlo al frantoio è il modo migliore per pagarlo il giusto."
    ShopCategory.CANTINA -> "Le piccole cantine spesso vendono anche sfuso: porta le tue bottiglie e chiedi di assaggiare."
    ShopCategory.TORREFAZIONE -> "Il caffè tostato da poco è più profumato: compralo in piccole quantità e chiedi di macinarlo al momento."
    ShopCategory.MULINO -> "Le farine macinate a pietra conservano più gusto: chiedi da quali grani e campi provengono."
    ShopCategory.APICOLTURA -> "Il miele dell'apicoltore locale ha in etichetta l'origine Italia: ogni fioritura dà un sapore diverso."
    ShopCategory.CONSERVE -> "Le conserve artigiane fermano la stagione in un barattolo: sono il modo giusto di mangiare pomodoro d'inverno."
    ShopCategory.BIRRIFICIO -> "I birrifici artigiani usano spesso cereali locali: chiedi le birre stagionali."
    ShopCategory.GELATERIA -> "Una gelateria artigiana cambia i gusti alla frutta con le stagioni: è un buon segnale."
    ShopCategory.SPEZIE -> "Lo zafferano e le spezie coltivate nel Lazio sono rari e preziosi: comprali direttamente da chi li produce."
    ShopCategory.AZIENDA_AGRICOLA -> "Comprare in azienda agricola accorcia la filiera al massimo: telefona prima per sapere giorni e orari di vendita."
}

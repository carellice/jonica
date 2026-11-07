package it.jonica.app.ui.shops

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
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
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.rounded.CloseFullscreen
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.OpenInFull
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.lerp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
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
import android.annotation.SuppressLint
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberUpdatedState
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point

private val CardMapHeight = 230.dp

/** Molla della mappa a schermo intero: morbida, con un filo di rimbalzo finale. */
private val ExpandSpring = spring<Float>(dampingRatio = 0.82f, stiffness = 240f)

/** Molla con cui la mappa si aggancia aperta o chiusa quando finisce lo scroll. */
private val SnapSpring = spring<Float>(dampingRatio = 0.9f, stiffness = 420f)

@Composable
fun ShopsScreen(
    location: GeoPoint?,
    permissionGranted: Boolean,
    onRequestPermission: () -> Unit,
    onFullscreenChange: (Boolean) -> Unit = {},
    onOpenOnline: () -> Unit = {},
) {
    val context = LocalContext.current
    val dark = isSystemInDarkTheme()
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    var filter by remember { mutableStateOf<ShopCategory?>(null) }
    var onlyVerified by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<Shop?>(null) }
    var mapView by remember { mutableStateOf<MapLibreMap?>(null) }
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

    // ── stato della mappa ─────────────────────────────────────────────
    // expand: 0 = card nella pagina, 1 = schermo intero
    val expand = remember { Animatable(0f) }
    var expanded by remember { mutableStateOf(false) }
    // collapse: quanti px di mappa ha "mangiato" lo scroll della lista (0..cardPx)
    val cardPx = with(density) { CardMapHeight.toPx() }
    val collapse = remember { mutableFloatStateOf(0f) }
    val collapsedFraction by remember { derivedStateOf { collapse.floatValue / cardPx } }

    fun setExpanded(value: Boolean) {
        if (expanded == value) return
        expanded = value
        onFullscreenChange(value)
        scope.launch {
            if (value && collapse.floatValue > 0f) {
                // prima riapre la card, poi la porta a schermo intero
                launch { animate(collapse.floatValue, 0f, animationSpec = SnapSpring) { v, _ -> collapse.floatValue = v } }
            }
            expand.animateTo(if (value) 1f else 0f, ExpandSpring)
        }
    }
    BackHandler(enabled = expanded) { setExpanded(false) }
    DisposableEffect(Unit) { onDispose { onFullscreenChange(false) } }

    // La lista "spinge" via la mappa mentre scorre verso l'alto e la riporta giù solo quando
    // torna in cima; al rilascio la mappa si aggancia aperta o chiusa con una molla.
    val nested = remember(cardPx) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val c = collapse.floatValue
                if (available.y < 0f && c < cardPx) {
                    val next = (c - available.y).coerceAtMost(cardPx)
                    collapse.floatValue = next
                    return Offset(0f, -(next - c))
                }
                return Offset.Zero
            }

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                val c = collapse.floatValue
                if (available.y > 0f && c > 0f) {
                    val next = (c - available.y).coerceAtLeast(0f)
                    collapse.floatValue = next
                    return Offset(0f, c - next)
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                val c = collapse.floatValue
                if (c > 0f && c < cardPx) {
                    val velocity = consumed.y + available.y
                    val target = when {
                        velocity < -800f -> cardPx
                        velocity > 800f -> 0f
                        c > cardPx / 2 -> cardPx
                        else -> 0f
                    }
                    animate(c, target, animationSpec = SnapSpring) { v, _ -> collapse.floatValue = v }
                }
                return Velocity.Zero
            }
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val fullW = constraints.maxWidth
        val fullH = constraints.maxHeight
        val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

        Column(
            Modifier
                .fillMaxSize()
                .clipToBounds()
                .nestedScroll(nested),
        ) {
            // l'intestazione scivola verso l'alto quando la mappa va a schermo intero
            Box(
                Modifier
                    .layout { m, c ->
                        val p = m.measure(c)
                        val e = expand.value.coerceIn(0f, 1f)
                        val h = (p.height * (1f - e)).roundToInt()
                        layout(p.width, h) { p.placeRelativeWithLayer(0, h - p.height) }
                    },
            ) {
                WavyHeader(color = Sky, secondColor = Color(0xFF5E93A6), floating = ShopCategory.entries.take(6).map { it.icon }) {
                    Column(Modifier.statusBarsPadding().padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 44.dp)) {
                        Text("Botteghe e produttori", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                        // il sottotitolo si richiude insieme alla mappa durante lo scroll
                        Box(
                            Modifier
                                .layout { m, c ->
                                    val p = m.measure(c)
                                    val f = collapsedFraction.coerceIn(0f, 1f)
                                    layout(p.width, (p.height * (1f - f)).roundToInt()) { p.placeRelative(0, 0) }
                                }
                                .graphicsLayer { alpha = 1f - collapsedFraction },
                        ) {
                            Text(
                                if (location != null) "${allShops.size} botteghe nel Lazio, dalla più vicina a te."
                                else "${allShops.size} botteghe nel Lazio · attiva la posizione per ordinarle in base a dove sei.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.88f),
                                minLines = 2,
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        ShopsModeSwitch(online = false) { if (it) onOpenOnline() }
                    }
                }
            }

            // ── finestra della mappa ──────────────────────────────────────
            // Il MapView ha sempre la dimensione dello schermo e resta centrato: la "finestra"
            // si allarga e si stringe sopra di lui, così la mappa non viene mai ridimensionata
            // e l'animazione resta fluida anche a 120 Hz.
            val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
            Box(
                Modifier
                    .fillMaxWidth()
                    .layout { m, c ->
                        val e = expand.value
                        val pad = lerpF(ScreenPadding.toPx(), 0f, e.coerceIn(0f, 1f))
                        val card = (cardPx - collapse.floatValue).coerceAtLeast(0f)
                        val h = lerpF(card, fullH.toFloat(), e).roundToInt().coerceAtLeast(0)
                        val w = (c.maxWidth - 2 * pad).roundToInt().coerceAtLeast(0)
                        val lift = lerpF(-20.dp.toPx(), 0f, e) * (1f - collapsedFraction)
                        val p = m.measure(Constraints.fixed(w, h))
                        layout(c.maxWidth, (h + lift).roundToInt().coerceAtLeast(0)) {
                            p.placeRelativeWithLayer(pad.roundToInt(), lift.roundToInt())
                        }
                    }
                    .graphicsLayer {
                        val e = expand.value
                        // la molla può superare 1 nel rimbalzo: raggio e margini restano non negativi
                        shape = RoundedCornerShape(lerp(28.dp, 0.dp, e.coerceIn(0f, 1f)))
                        clip = true
                        // la card si "allontana" un po' mentre si richiude
                        val f = collapsedFraction * (1f - e)
                        alpha = 1f - 0.5f * f
                        val s = 1f - 0.04f * f
                        scaleX = s; scaleY = s
                        transformOrigin = TransformOrigin(0.5f, 0f)
                    }
                    .background(surfaceVariant),
            ) {
                Box(
                    Modifier.layout { m, c ->
                        val p = m.measure(Constraints.fixed(fullW, fullH))
                        layout(c.maxWidth, c.maxHeight) {
                            p.placeRelative((c.maxWidth - fullW) / 2, (c.maxHeight - fullH) / 2)
                        }
                    },
                ) {
                    OsmMap(
                        dark = dark,
                        center = here,
                        userLocation = location,
                        shops = visible.map { it.first },
                        onShopClick = { selected = it },
                        onReady = { mapView = it },
                    )
                }

                MapControls(
                    expand = { expand.value },
                    expanded = expanded,
                    statusTop = statusTop,
                    navBottom = navBottom,
                    count = visible.size,
                    filterLabel = filter?.label,
                    onToggle = { setExpanded(!expanded) },
                    onLocate = {
                        if (!permissionGranted) onRequestPermission()
                        mapView?.flyTo(here.lat, here.lon, 13.6)
                    },
                )
            }

            Row(
                Modifier
                    .padding(horizontal = ScreenPadding)
                    .offset(y = (-8).dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SearchField(query, { query = it }, Modifier.weight(1f))
                // quando la mappa è richiusa compare un pulsante per riaprirla
                AnimatedVisibility(
                    visible = collapsedFraction > 0.85f,
                    enter = fadeIn(tween(220)) + scaleIn(spring(dampingRatio = 0.5f, stiffness = 500f), initialScale = 0.4f) +
                        expandHorizontally(spring(stiffness = 500f)),
                    exit = fadeOut(tween(150)) + scaleOut(tween(150), targetScale = 0.4f) + shrinkHorizontally(tween(200)),
                ) {
                    Row {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .bouncyClick {
                                    scope.launch {
                                        animate(collapse.floatValue, 0f, animationSpec = ExpandSpring) { v, _ -> collapse.floatValue = v }
                                    }
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Rounded.Map, "Mostra la mappa", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }
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
                        mapView?.flyTo(shop.lat, shop.lon, if (shop.approximate) 13.0 else 16.0)
                    }
                }
                item {
                    Text(
                        "Botteghe di Roma e del Lazio: ricerche su fonti pubbliche (siti dei produttori, mercati contadini, reti locali, schede Maps ed elenco regionale delle botteghe storiche). Le schede \"da verificare\" non sono state confermate: chiama prima di andare. Quartiere 00138 e mappe © OpenStreetMap contributors.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(8.dp),
                    )
                }
            }
        }
    }

    selected?.let { s ->
        ShopSheet(s, distanceMeters(here, s.lat, s.lon), location != null, context) { selected = null }
    }
}

/** Pulsanti sopra la mappa: si spostano con gli inset di sistema quando la mappa è a schermo intero. */
@Composable
private fun BoxScope.MapControls(
    expand: () -> Float,
    expanded: Boolean,
    statusTop: Dp,
    navBottom: Dp,
    count: Int,
    filterLabel: String?,
    onToggle: () -> Unit,
    onLocate: () -> Unit,
) {
    val surface = MaterialTheme.colorScheme.surface
    // a schermo intero: velo scuro sotto la status bar, così l'ora e le icone restano leggibili
    Box(
        Modifier
            .align(Alignment.TopCenter)
            .fillMaxWidth()
            .height(statusTop + 28.dp)
            .graphicsLayer { alpha = expand().coerceIn(0f, 1f) }
            .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.35f), Color.Transparent))),
    )
    // l'icona ruota di mezzo giro mentre si passa da "espandi" a "chiudi"
    val turn by animateFloatAsState(if (expanded) 180f else 0f, spring(dampingRatio = 0.6f, stiffness = 300f), label = "turn")

    Box(
        Modifier
            .align(Alignment.TopEnd)
            .layout { m, c ->
                val p = m.measure(c)
                val top = (12.dp.toPx() + statusTop.toPx() * expand()).roundToInt()
                layout(p.width + 12.dp.roundToPx(), p.height + top) { p.placeRelative(0, top) }
            }
            .size(48.dp)
            .shadow(6.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(surface)
            .bouncyClick(onToggle),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            if (turn > 90f) Icons.Rounded.CloseFullscreen else Icons.Rounded.OpenInFull,
            contentDescription = if (expanded) "Riduci la mappa" else "Espandi la mappa",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.graphicsLayer { rotationZ = turn },
        )
    }

    // a schermo intero: quante botteghe sono sulla mappa
    Box(
        Modifier
            .align(Alignment.TopStart)
            .layout { m, c ->
                val p = m.measure(c)
                val top = (18.dp.toPx() + statusTop.toPx() * expand()).roundToInt()
                layout(p.width + 20.dp.roundToPx(), p.height + top) { p.placeRelative(20.dp.roundToPx(), top) }
            }
            .graphicsLayer {
                val e = expand()
                alpha = ((e - 0.5f) * 2f).coerceIn(0f, 1f)
                translationY = (1f - e) * -24.dp.toPx()
            }
            .shadow(6.dp, RoundedCornerShape(50))
            .clip(RoundedCornerShape(50))
            .background(surface)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(
            (if (count == 1) "1 bottega" else "$count botteghe") + (filterLabel?.let { " · $it" } ?: ""),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }

    Box(
        Modifier
            .align(Alignment.BottomEnd)
            .layout { m, c ->
                val p = m.measure(c)
                val bottom = (12.dp.toPx() + (navBottom + 12.dp).toPx() * expand()).roundToInt()
                layout(p.width + 12.dp.roundToPx(), p.height + bottom) { p.placeRelative(0, 0) }
            }
            .size(48.dp)
            .shadow(6.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(surface)
            .bouncyClick(onLocate),
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Rounded.MyLocation, "La mia posizione", tint = MaterialTheme.colorScheme.primary)
    }

    Text(
        "© OpenFreeMap · © OpenStreetMap contributors",
        fontSize = 9.sp,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        modifier = Modifier
            .align(Alignment.BottomStart)
            .layout { m, c ->
                val p = m.measure(c)
                val bottom = (8.dp.toPx() + navBottom.toPx() * expand()).roundToInt()
                layout(p.width + 8.dp.roundToPx(), p.height + bottom) { p.placeRelative(8.dp.roundToPx(), 0) }
            }
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}

@Composable
internal fun FilterChip(@DrawableRes icon: Int?, text: String, selected: Boolean, onClick: () -> Unit) {
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

/**
 * Mappa vettoriale in stile Google Maps (MapLibre + tile OpenFreeMap, nessuna API key).
 * Stili chiaro e scuro in assets/map_light.json e map_dark.json.
 */
@SuppressLint("ClickableViewAccessibility")
@Composable
private fun OsmMap(
    dark: Boolean,
    center: GeoPoint,
    userLocation: GeoPoint?,
    shops: List<Shop>,
    onShopClick: (Shop) -> Unit,
    onReady: (MapLibreMap) -> Unit,
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val shopClick by rememberUpdatedState(onShopClick)
    val currentShops by rememberUpdatedState(shops)
    var ready by remember { mutableStateOf<Pair<MapLibreMap, Style>?>(null) }
    val view = remember {
        MapView(context).apply {
            onCreate(null)
            getMapAsync { map ->
                map.uiSettings.apply {
                    isLogoEnabled = false
                    isAttributionEnabled = false
                    isCompassEnabled = false
                    isRotateGesturesEnabled = false
                    isTiltGesturesEnabled = false
                }
                map.setMinZoomPreference(6.0)
                map.cameraPosition = CameraPosition.Builder().target(LatLng(center.lat, center.lon)).zoom(13.6).build()
                map.addOnMapClickListener { at ->
                    val hit = map.queryRenderedFeatures(map.projection.toScreenLocation(at), SHOPS_LAYER).firstOrNull()
                    val i = hit?.getNumberProperty("i")?.toInt() ?: return@addOnMapClickListener false
                    currentShops.getOrNull(i)?.let(shopClick)
                    true
                }
                map.setStyle(Style.Builder().fromUri("asset://map_${if (dark) "dark" else "light"}.json")) { style ->
                    style.addImage("__user", userIcon(context))
                    style.addSource(GeoJsonSource(SHOPS_SOURCE))
                    style.addSource(GeoJsonSource(USER_SOURCE))
                    style.addLayer(
                        SymbolLayer(SHOPS_LAYER, SHOPS_SOURCE).withProperties(
                            PropertyFactory.iconImage(Expression.get("icon")),
                            PropertyFactory.iconAllowOverlap(true),
                            PropertyFactory.iconIgnorePlacement(true),
                        )
                    )
                    style.addLayer(
                        SymbolLayer("user-layer", USER_SOURCE).withProperties(
                            PropertyFactory.iconImage("__user"),
                            PropertyFactory.iconAllowOverlap(true),
                            PropertyFactory.iconIgnorePlacement(true),
                        )
                    )
                    ready = map to style
                    onReady(map)
                }
            }
        }
    }
    DisposableEffect(lifecycle) {
        val obs = LifecycleEventObserver { _, e ->
            when (e) {
                Lifecycle.Event.ON_START -> view.onStart()
                Lifecycle.Event.ON_RESUME -> view.onResume()
                Lifecycle.Event.ON_PAUSE -> view.onPause()
                Lifecycle.Event.ON_STOP -> view.onStop()
                else -> Unit
            }
        }
        lifecycle.addObserver(obs)
        view.onStart()
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) view.onResume()
        onDispose {
            lifecycle.removeObserver(obs)
            view.onPause()
            view.onStop()
            view.onDestroy()
        }
    }
    var centeredOnUser by remember { mutableStateOf(false) }
    LaunchedEffect(userLocation, ready) {
        val map = ready?.first ?: return@LaunchedEffect
        if (userLocation != null && !centeredOnUser) {
            centeredOnUser = true
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(userLocation.lat, userLocation.lon), 13.6), 900)
        }
    }
    ready?.let { (_, style) ->
        SideEffect {
            val features = shops.mapIndexed { i, s ->
                val key = "m${s.category.icon}"
                if (style.getImage(key) == null) style.addImage(key, markerIcon(context, s.category.icon, s.category.color.toArgb()))
                Feature.fromGeometry(Point.fromLngLat(s.lon, s.lat)).apply {
                    addNumberProperty("i", i)
                    addStringProperty("icon", key)
                }
            }
            style.getSourceAs<GeoJsonSource>(SHOPS_SOURCE)?.setGeoJson(FeatureCollection.fromFeatures(features))
            val user = userLocation?.let { listOf(Feature.fromGeometry(Point.fromLngLat(it.lon, it.lat))) }.orEmpty()
            style.getSourceAs<GeoJsonSource>(USER_SOURCE)?.setGeoJson(FeatureCollection.fromFeatures(user))
        }
    }
    AndroidView(factory = { view }, modifier = Modifier.fillMaxSize())
}

private const val SHOPS_SOURCE = "shops"
private const val SHOPS_LAYER = "shops-layer"
private const val USER_SOURCE = "user"

/** Zoom MapLibre (tile da 512 px): uno in meno rispetto alla vecchia mappa a tile da 256 px. */
private fun MapLibreMap.flyTo(lat: Double, lon: Double, zoom: Double) =
    animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lon), zoom), 700)

private val iconCache = HashMap<String, Bitmap>()

private fun markerIcon(context: Context, @DrawableRes res: Int, color: Int): Bitmap = iconCache.getOrPut("m$res") {
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
    bmp
}

private fun userIcon(context: Context): Bitmap = iconCache.getOrPut("__user") {
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
    bmp
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
                if (shop.family) Pill("Gestione familiare o cooperativa", MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
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
                    val cid = shop.mapsUrl?.takeIf { "cid=" in it }
                    val uri = if (cid != null) {
                        // scheda Maps del negozio: apre direttamente il punto giusto
                        Uri.parse(cid)
                    } else if (shop.approximate) {
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
internal fun SearchField(
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Cerca per nome, comune o prodotto",
) {
    Row(
        modifier
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
                Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
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

internal fun String.normalized(): String =
    java.text.Normalizer.normalize(lowercase(), java.text.Normalizer.Form.NFD).replace(accents, "")

private val searchCache = HashMap<String, String>()

private val Shop.searchText: String
    get() = searchCache.getOrPut(id) {
        (listOf(name, comune.orEmpty(), address.orEmpty(), description.orEmpty()) + products + categories.map { it.label })
            .joinToString(" ").normalized()
    }

@Composable
internal fun InfoLine(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
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

private fun lerpF(a: Float, b: Float, t: Float) = a + (b - a) * t

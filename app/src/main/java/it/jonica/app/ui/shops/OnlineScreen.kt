package it.jonica.app.ui.shops

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.ShoppingBasket
import androidx.compose.material.icons.rounded.ShoppingCart
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.sp
import it.jonica.app.data.OnlineShop
import it.jonica.app.data.ShopCategory
import it.jonica.app.data.onlineNetworks
import it.jonica.app.data.onlineShops
import it.jonica.app.ui.components.IconBubble
import it.jonica.app.ui.components.Pill
import it.jonica.app.ui.components.ScreenPadding
import it.jonica.app.ui.components.WavyHeader
import it.jonica.app.ui.components.bouncyClick
import it.jonica.app.ui.components.staggeredEnter
import it.jonica.app.ui.theme.readable

/** Botteghe e produttori italiani da cui comprare online o con ordine a distanza. */
@Composable
fun OnlineScreen(onOpenNearby: () -> Unit) {
    val context = LocalContext.current
    var filter by remember { mutableStateOf<ShopCategory?>(null) }
    var onlyNationwide by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<OnlineShop?>(null) }

    val visible = remember(filter, onlyNationwide, query) {
        val q = query.trim().normalized()
        onlineShops
            .filter { filter == null || filter in it.categories }
            .filter { !onlyNationwide || it.nationwide }
            .filter { q.isEmpty() || it.searchText.contains(q) }
            // prima il Lazio, poi in ordine alfabetico
            .sortedWith(compareBy({ it.region != "Lazio" }, { it.name.lowercase() }))
    }
    val categories = remember {
        ShopCategory.entries
            .map { c -> c to onlineShops.count { c in it.categories } }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .map { it.first }
    }

    // come in "Vicino a te": scorrendo la lista l'intestazione si ritira e la lista va a tutto schermo
    val header = remember { HeaderCollapse() }
    val statusPx = WindowInsets.statusBars.getTop(LocalDensity.current)
    val nested = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset =
                if (available.y < 0f) Offset(0f, -header.consume(-available.y)) else Offset.Zero

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset =
                if (available.y > 0f) Offset(0f, -header.consume(-available.y)) else Offset.Zero

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                header.settle(consumed.y + available.y)
                return Velocity.Zero
            }
        }
    }

    Column(Modifier.fillMaxSize().nestedScroll(nested)) {
        WavyHeader(
            color = Color(0xFF5E8A3C), secondColor = Color(0xFF3E6B8C), floating = categories.take(6).map { it.icon },
            modifier = Modifier.collapsingHeader(header, statusPx),
        ) {
            Column(
                Modifier
                    .graphicsLayer { alpha = 1f - header.fraction }
                    .statusBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 44.dp),
            ) {
                Text("Botteghe online", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                Text(
                    "${onlineShops.size} produttori e botteghe italiane: compra da chi produce, anche da lontano.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.88f),
                    // stesse righe del sottotitolo "Vicino a te": l'interruttore non salta
                    minLines = 2,
                )
                Spacer(Modifier.height(12.dp))
                ShopsModeSwitch(online = true) { if (!it) onOpenNearby() }
            }
        }
        SearchField(
            query, { query = it },
            Modifier.padding(horizontal = ScreenPadding).offset(y = (-8).dp),
            placeholder = "Cerca per nome, regione o prodotto",
        )
        Spacer(Modifier.height(4.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = ScreenPadding),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item { FilterChip(null, "Tutte", filter == null) { filter = null } }
            item { FilterChip(null, "Spedisce in Italia", onlyNationwide) { onlyNationwide = !onlyNationwide } }
            items(categories) { c -> FilterChip(c.icon, c.label, filter == c) { filter = if (filter == c) null else c } }
        }
        LazyColumn(
            contentPadding = PaddingValues(start = ScreenPadding, end = ScreenPadding, top = 12.dp, bottom = 130.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Text(
                    if (visible.size == 1) "1 negozio online" else "${visible.size} negozi online",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
            if (visible.isEmpty()) {
                item {
                    Text(
                        "Nessun negozio trovato. Prova a cambiare filtro o ricerca.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
            itemsIndexed(visible, key = { _, it -> it.id }) { i, shop ->
                OnlineRow(shop, Modifier.animateItem().staggeredEnter(i, filter to onlyNationwide)) { selected = shop }
            }
            if (query.isBlank() && filter == null && !onlyNationwide) {
                item {
                    Text(
                        "Shop collettivi",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 4.dp, top = 12.dp),
                    )
                }
                items(onlineNetworks, key = { it.url }) { n ->
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .bouncyClick { openUrl(context, n.url) }
                            .padding(16.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(n.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                            Icon(Icons.AutoMirrored.Rounded.OpenInNew, "Apri", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        }
                        Text(n.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            item {
                Text(
                    "Botteghe, laboratori, aziende agricole e cooperative italiane con un negozio online o ordini a distanza, da ricerche su fonti pubbliche (novembre 2026). Spedizioni, freschi e zone servite: controlla sempre le condizioni sul sito del venditore.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(8.dp),
                )
            }
        }
    }

    selected?.let { s -> OnlineSheet(s, context) { selected = null } }
}

/** Ultima modalità mostrata dall'interruttore, per animarlo solo quando cambia davvero. */
private var lastShopsMode: Boolean? = null

private fun Boolean.toFloat() = if (this) 1f else 0f

/** Interruttore "Vicino a te / Online" nell'intestazione delle botteghe. */
@Composable
internal fun ShopsModeSwitch(online: Boolean, onChange: (Boolean) -> Unit) {
    // ogni schermata ha il suo interruttore: dopo un cambio di modalità la pillola parte dal lato opposto e scivola al suo posto
    val pos = remember { Animatable(if (online != (lastShopsMode ?: online)) 1f - online.toFloat() else online.toFloat()) }
    LaunchedEffect(online) {
        lastShopsMode = online
        pos.animateTo(online.toFloat(), spring(dampingRatio = 0.7f, stiffness = 500f))
    }
    BoxWithConstraints(
        Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.18f))
            .padding(4.dp),
    ) {
        val half = maxWidth / 2
        Box(
            Modifier
                .offset(x = half * pos.value)
                .width(half)
                .fillMaxSize()
                .clip(RoundedCornerShape(50))
                .background(Color.White),
        )
        Row(Modifier.fillMaxSize()) {
            listOf("Vicino a te", "Online").forEachIndexed { i, label ->
                val sel = (i == 1) == online
                Box(Modifier.weight(1f).fillMaxSize().bouncyClick { onChange(i == 1) }, contentAlignment = Alignment.Center) {
                    Text(
                        label,
                        color = if (sel) Color(0xFF2F4A5E) else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun OnlineRow(shop: OnlineShop, modifier: Modifier, onClick: () -> Unit) {
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
            Text(shop.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                listOfNotNull(shop.category.label, shop.region).joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(8.dp))
        Icon(
            if (shop.nationwide) Icons.Rounded.LocalShipping else Icons.Rounded.Language,
            contentDescription = if (shop.nationwide) "Spedisce in Italia" else "Negozio online",
            tint = shop.category.color.readable(),
            modifier = Modifier.size(22.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun OnlineSheet(shop: OnlineShop, context: Context, onDismiss: () -> Unit) {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
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
                IconBubble(shop.category.icon, 72.dp, shop.category.color.copy(alpha = 0.18f))
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(shop.name, style = MaterialTheme.typography.headlineSmall)
                    Text(
                        listOfNotNull(shop.place, shop.region).joinToString(" · "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                shop.categories.forEach { c -> Pill(c.label, c.color.copy(alpha = 0.16f), c.color.readable()) }
                if (shop.family) Pill("Gestione familiare o cooperativa", MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
                if (shop.chilled) Pill("Spedizione refrigerata", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
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
            InfoLine(Icons.Rounded.ShoppingBasket, shop.channel)
            InfoLine(Icons.Rounded.LocalShipping, shop.shipping)
            shop.place?.let { InfoLine(Icons.Rounded.Place, "Sede a $it") }
            shop.note?.let { InfoLine(Icons.Rounded.Language, it) }
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { openUrl(context, shop.shopUrl) },
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                Icon(Icons.Rounded.ShoppingCart, null)
                Spacer(Modifier.width(8.dp))
                Text("Vai al negozio")
            }
            if (shop.website != shop.shopUrl) {
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { openUrl(context, shop.website) },
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                ) {
                    Icon(Icons.Rounded.Language, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Sito web")
                }
            }
        }
    }
}

private fun openUrl(context: Context, url: String) {
    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
}

private val onlineSearchCache = HashMap<String, String>()

private val OnlineShop.searchText: String
    get() = onlineSearchCache.getOrPut(id) {
        (listOf(name, region, place.orEmpty()) + products + categories.map { it.label }).joinToString(" ").normalized()
    }

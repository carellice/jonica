package it.jonica.app.ui.today

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.jonica.app.data.Category
import it.jonica.app.data.FarmEvent
import it.jonica.app.data.Product
import it.jonica.app.data.facts
import it.jonica.app.data.farmEvents
import it.jonica.app.data.products
import it.jonica.app.data.seasonOf
import it.jonica.app.data.shops
import it.jonica.app.location.Default00138
import it.jonica.app.location.GeoPoint
import it.jonica.app.location.distanceMeters
import it.jonica.app.location.formatDistance
import it.jonica.app.ui.components.EmojiBubble
import it.jonica.app.ui.components.Pill
import it.jonica.app.ui.components.SectionTitle
import it.jonica.app.ui.components.WavyHeader
import it.jonica.app.ui.components.bouncyClick
import it.jonica.app.ui.components.staggeredEnter
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TodayScreen(
    location: GeoPoint?,
    onProduct: (Product) -> Unit,
    onOpenSeasons: () -> Unit,
    onOpenShops: () -> Unit,
) {
    val today = remember { LocalDate.now() }
    val month = today.monthValue
    val fruit = remember(month) { products.filter { it.category == Category.FRUTTA && it.inSeason(month) } }
    val veg = remember(month) { products.filter { it.category == Category.VERDURA && it.inSeason(month) } }
    val arriving = remember(month) { products.filter { it.isArriving(month) } }
    val leaving = remember(month) { products.filter { it.isLeaving(month) } }
    val events = remember(month) { farmEvents.filter { month in it.months } }
    val inSeasonCount = remember(month) { products.count { it.inSeason(month) } }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item { Hero(today, inSeasonCount) }

        if (events.isNotEmpty()) {
            item {
                SectionTitle("Cosa succede nei campi", "Il calendario della filiera, questo mese")
                Spacer(Modifier.height(12.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    itemsIndexed(events) { i, e -> EventCard(e, Modifier.staggeredEnter(i)) }
                }
            }
        }

        item { ProductRow("Frutta di stagione", "${fruit.size} prodotti al meglio adesso", fruit, onProduct, onOpenSeasons) }
        item { ProductRow("Verdura di stagione", "${veg.size} prodotti al meglio adesso", veg, onProduct, onOpenSeasons) }

        if (arriving.isNotEmpty() || leaving.isNotEmpty()) {
            item { ArrivingLeaving(arriving, leaving, onProduct) }
        }

        item { FactCarousel() }

        item { NearestShop(location, onOpenShops) }
    }
}

@Composable
private fun Hero(today: LocalDate, count: Int) {
    val (season, seasonEmoji) = seasonOf(today.monthValue)
    val dateText = remember(today) {
        today.format(DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.ITALIAN)).replaceFirstChar { it.uppercase() }
    }
    val greeting = remember {
        when (LocalTime.now().hour) {
            in 5..12 -> "Buongiorno"
            in 13..17 -> "Buon pomeriggio"
            else -> "Buonasera"
        }
    }
    val inf = rememberInfiniteTransition(label = "hero")
    val shift by inf.animateFloat(0f, 1f, infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Reverse), label = "shift")
    val bob by inf.animateFloat(-6f, 6f, infiniteRepeatable(tween(2200), RepeatMode.Reverse), label = "bob")
    val sway by inf.animateFloat(-8f, 8f, infiniteRepeatable(tween(3200), RepeatMode.Reverse), label = "sway")

    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary = MaterialTheme.colorScheme.tertiary

    val floating = remember(today) { products.filter { it.inSeason(today.monthValue) }.shuffled(java.util.Random(today.toEpochDay())).take(7).map { it.emoji } }

    WavyHeader(
        color = primary,
        secondColor = lerpColor(secondary, tertiary, shift * 0.6f),
        floating = floating,
    ) {
        // sole che pulsa dietro al testo
        Canvas(Modifier.matchParentSize()) {
            drawCircle(tertiary.copy(alpha = 0.18f), radius = size.width * (0.32f + shift * 0.04f), center = Offset(size.width * 0.92f, size.height * 0.18f))
            drawCircle(Color.White.copy(alpha = 0.07f), radius = size.width * 0.22f, center = Offset(size.width * 0.92f, size.height * 0.18f))
        }
        Column(
            Modifier
                .statusBarsPadding()
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 60.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(greeting, color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.titleMedium)
                    Text(dateText, color = Color.White, style = MaterialTheme.typography.headlineLarge)
                }
                Text(
                    seasonEmoji,
                    fontSize = 56.sp,
                    modifier = Modifier.graphicsLayer { translationY = bob; rotationZ = sway },
                )
            }
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HeroStat(season, "stagione")
                HeroStat("$count", "di stagione")
                HeroStat("${shops.size}", "botteghe")
            }
        }
    }
}

@Composable
private fun HeroStat(value: String, label: String) {
    Column(
        Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.16f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
    }
}

@Composable
private fun EventCard(e: FarmEvent, modifier: Modifier = Modifier) {
    var expanded by remember { androidx.compose.runtime.mutableStateOf(false) }
    Column(
        modifier
            .width(290.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.tertiaryContainer)
            .bouncyClick { expanded = !expanded }
            .padding(20.dp)
            .animateContentSize(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(e.emoji, fontSize = 34.sp)
            Spacer(Modifier.width(12.dp))
            Text(e.title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onTertiaryContainer)
        }
        Spacer(Modifier.height(10.dp))
        Text(
            e.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            maxLines = if (expanded) Int.MAX_VALUE else 3,
        )
        if (expanded) {
            Spacer(Modifier.height(12.dp))
            Box(
                Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                    .padding(12.dp),
            ) {
                Text("💡 " + e.advice, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onTertiaryContainer)
            }
        } else {
            Spacer(Modifier.height(8.dp))
            Text("Tocca per il consiglio →", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
private fun ProductRow(
    title: String,
    subtitle: String,
    items: List<Product>,
    onProduct: (Product) -> Unit,
    onMore: () -> Unit,
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionTitle(title, subtitle, Modifier.weight(1f))
            Icon(
                Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = "Vedi tutto",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 20.dp).size(28.dp).bouncyClick(onMore),
            )
        }
        Spacer(Modifier.height(12.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            itemsIndexed(items) { i, p ->
                Column(
                    Modifier
                        .staggeredEnter(i)
                        .width(108.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .bouncyClick { onProduct(p) }
                        .padding(vertical = 16.dp, horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    EmojiBubble(p.emoji, 60.dp, MaterialTheme.colorScheme.surface)
                    Spacer(Modifier.height(10.dp))
                    Text(p.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 2, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun ArrivingLeaving(arriving: List<Product>, leaving: List<Product>, onProduct: (Product) -> Unit) {
    Column(Modifier.padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (arriving.isNotEmpty()) {
            ChipGroup("🌱 Appena arrivati", "Novità del mese", arriving, MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer, onProduct)
        }
        if (leaving.isNotEmpty()) {
            ChipGroup("⏳ Ultime settimane", "Approfittane prima che finiscano", leaving, MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer, onProduct)
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun ChipGroup(title: String, subtitle: String, items: List<Product>, bg: Color, fg: Color, onProduct: (Product) -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(bg)
            .padding(20.dp),
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = fg)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = fg.copy(alpha = 0.8f))
        Spacer(Modifier.height(12.dp))
        androidx.compose.foundation.layout.FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items.forEachIndexed { i, p ->
                Row(
                    Modifier
                        .staggeredEnter(i)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                        .bouncyClick { onProduct(p) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(p.emoji, fontSize = 16.sp)
                    Spacer(Modifier.width(6.dp))
                    Text(p.name, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
private fun FactCarousel() {
    var index by remember { mutableIntStateOf((System.currentTimeMillis() / 86_400_000L % facts.size).toInt()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(6500)
            index = (index + 1) % facts.size
        }
    }
    Column(Modifier.padding(horizontal = 20.dp)) {
        Text("Lo sapevi?", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.primary)
                .bouncyClick { index = (index + 1) % facts.size }
                .padding(22.dp),
        ) {
            AnimatedContent(
                targetState = index,
                transitionSpec = {
                    (slideInVertically(tween(450)) { it / 2 } + fadeIn(tween(450)))
                        .togetherWith(slideOutVertically(tween(300)) { -it / 2 } + fadeOut(tween(250)))
                },
                label = "fact",
            ) { i ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(facts[i].first, fontSize = 40.sp)
                    Spacer(Modifier.width(16.dp))
                    Text(facts[i].second, color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            facts.indices.forEach { i ->
                val w by androidx.compose.animation.core.animateDpAsState(if (i == index) 18.dp else 6.dp, label = "dot")
                Box(
                    Modifier
                        .padding(horizontal = 3.dp)
                        .size(width = w, height = 6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (i == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
                )
            }
        }
    }
}

@Composable
private fun NearestShop(location: GeoPoint?, onOpenShops: () -> Unit) {
    val here = location ?: Default00138
    val nearest = remember(here) { shops.minBy { distanceMeters(here, it.lat, it.lon) } }
    val dist = remember(here, nearest) { distanceMeters(here, nearest.lat, nearest.lon) }
    Column(Modifier.padding(horizontal = 20.dp)) {
        Text("La bottega più vicina", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .bouncyClick(onOpenShops)
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            EmojiBubble(nearest.category.emoji, 56.dp, nearest.category.color.copy(alpha = 0.18f))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(nearest.name, style = MaterialTheme.typography.titleMedium)
                Text(nearest.category.label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                Pill(
                    if (location == null) "${formatDistance(dist)} dal centro 00138" else "a ${formatDistance(dist)} da te",
                    MaterialTheme.colorScheme.primaryContainer,
                    MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

private fun lerpColor(a: Color, b: Color, t: Float) = androidx.compose.ui.graphics.lerp(a, b, t.coerceIn(0f, 1f))

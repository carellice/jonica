package it.jonica.app.ui.today

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.HourglassBottom
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.jonica.app.data.Category
import it.jonica.app.data.FarmEvent
import it.jonica.app.data.Product
import it.jonica.app.data.facts
import it.jonica.app.data.farmEvents
import it.jonica.app.data.products
import it.jonica.app.data.seasonOf
import it.jonica.app.data.allShops
import it.jonica.app.location.Default00138
import it.jonica.app.location.GeoPoint
import it.jonica.app.location.distanceMeters
import it.jonica.app.location.formatDistance
import it.jonica.app.ui.components.IconBubble
import it.jonica.app.ui.components.Pill
import it.jonica.app.ui.components.ScreenPadding
import it.jonica.app.ui.components.SectionTitle
import it.jonica.app.ui.components.WavyHeader
import it.jonica.app.ui.theme.Mustard
import it.jonica.app.ui.theme.Olive
import it.jonica.app.ui.theme.Terracotta
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
        contentPadding = PaddingValues(bottom = 130.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        item { Hero(today, inSeasonCount) }

        if (events.isNotEmpty()) {
            item {
                SectionTitle("Cosa succede nei campi", "Tocca una card per girarla e leggere il consiglio")
                Spacer(Modifier.height(14.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = ScreenPadding),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    itemsIndexed(events) { i, e -> FlipEventCard(e, Modifier.staggeredEnter(i)) }
                }
            }
        }

        item { ProductRow("Frutta di stagione", "${fruit.size} prodotti al meglio adesso", fruit, onProduct, onOpenSeasons) }
        item { ProductRow("Verdura di stagione", "${veg.size} prodotti al meglio adesso", veg, onProduct, onOpenSeasons) }

        if (arriving.isNotEmpty()) {
            item {
                ChipGroup(Icons.Rounded.Spa, "Appena arrivati", "Le novità del mese", arriving,
                    MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer, onProduct)
            }
        }
        if (leaving.isNotEmpty()) {
            item {
                ChipGroup(Icons.Rounded.HourglassBottom, "Ultime settimane", "Approfittane prima che finiscano", leaving,
                    MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer, onProduct)
            }
        }

        item { FactCarousel() }

        item { NearestShop(location, onOpenShops) }
    }
}

@Composable
private fun Hero(today: LocalDate, count: Int) {
    val (season, seasonIcon) = seasonOf(today.monthValue)
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
    val sway by inf.animateFloat(-6f, 6f, infiniteRepeatable(tween(3200), RepeatMode.Reverse), label = "sway")

    // colori del brand fissi: il testo bianco resta leggibile anche in tema scuro
    val primary = Olive
    val secondary = Terracotta
    val tertiary = Mustard
    val floating = remember(today) {
        products.filter { it.inSeason(today.monthValue) }.shuffled(java.util.Random(today.toEpochDay())).take(6).map { it.icon }
    }

    WavyHeader(
        color = primary,
        secondColor = lerp(secondary, tertiary, shift * 0.6f),
        floating = floating,
    ) {
        Canvas(Modifier.matchParentSize()) {
            drawCircle(tertiary.copy(alpha = 0.18f), radius = size.width * (0.3f + shift * 0.03f), center = Offset(size.width * 0.86f, size.height * 0.26f))
        }
        Column(
            Modifier
                .statusBarsPadding()
                .padding(start = ScreenPadding, end = ScreenPadding, top = 24.dp, bottom = 56.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(greeting, color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.titleMedium)
                    Text(dateText, color = Color.White, style = MaterialTheme.typography.headlineLarge)
                }
                Image(
                    painterResource(seasonIcon),
                    contentDescription = season,
                    modifier = Modifier
                        .size(72.dp)
                        .graphicsLayer { translationY = bob; rotationZ = sway },
                )
            }
            Spacer(Modifier.height(20.dp))
            Row(
                Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                HeroStat(season, "stagione", Modifier.weight(1f))
                HeroStat("$count", "di stagione", Modifier.weight(1f))
                HeroStat("${allShops.size}", "botteghe", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun HeroStat(value: String, label: String, modifier: Modifier) {
    Column(
        modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.16f))
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(value, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, maxLines = 1)
        Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp, maxLines = 1)
    }
}

private val EventCardWidth = 280.dp
private val EventCardHeight = 232.dp

/** Card che si gira in 3D: davanti la descrizione, dietro il consiglio. */
@Composable
private fun FlipEventCard(e: FarmEvent, modifier: Modifier = Modifier) {
    var flipped by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(if (flipped) 180f else 0f, tween(550), label = "flip")
    val front = rotation <= 90f
    Box(
        modifier
            .size(EventCardWidth, EventCardHeight)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 14f * density
            }
            .clip(RoundedCornerShape(28.dp))
            .background(if (front) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primary)
            .bouncyClick { flipped = !flipped }
            .padding(20.dp),
    ) {
        if (front) {
            Column(Modifier.fillMaxSize()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBubble(e.icon, 52.dp, MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        e.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        maxLines = 2,
                        minLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    e.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.TouchApp, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Gira per il consiglio", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                }
            }
        } else {
            Column(Modifier.fillMaxSize().graphicsLayer { rotationY = 180f }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Lightbulb, null, tint = MaterialTheme.colorScheme.tertiary)
                    Spacer(Modifier.width(8.dp))
                    Text("Il consiglio", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimary)
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    e.advice,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    maxLines = 7,
                    overflow = TextOverflow.Ellipsis,
                )
            }
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
            Box(
                Modifier
                    .padding(end = ScreenPadding)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .bouncyClick(onMore),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, "Vedi tutto", tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.height(14.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = ScreenPadding),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            itemsIndexed(items) { i, p ->
                ProductCard(p, Modifier.staggeredEnter(i)) { onProduct(p) }
            }
        }
    }
}

@Composable
fun ProductCard(p: Product, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier
            .size(width = 112.dp, height = 140.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .bouncyClick(onClick)
            .padding(horizontal = 8.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconBubble(p.icon, 64.dp, MaterialTheme.colorScheme.surface)
        Spacer(Modifier.height(10.dp))
        Text(
            p.name,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            lineHeight = 16.sp,
            maxLines = 2,
            minLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipGroup(
    icon: ImageVector,
    title: String,
    subtitle: String,
    items: List<Product>,
    bg: Color,
    fg: Color,
    onProduct: (Product) -> Unit,
) {
    Column(
        Modifier
            .padding(horizontal = ScreenPadding)
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(bg)
            .padding(20.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = fg)
            Spacer(Modifier.width(10.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleLarge, color = fg)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = fg.copy(alpha = 0.8f))
            }
        }
        Spacer(Modifier.height(14.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items.forEachIndexed { i, p ->
                Row(
                    Modifier
                        .staggeredEnter(i)
                        .height(40.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.75f))
                        .bouncyClick { onProduct(p) }
                        .padding(start = 4.dp, end = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconBubble(p.icon, 32.dp, Color.Transparent)
                    Spacer(Modifier.width(4.dp))
                    Text(p.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
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
    Column(Modifier.padding(horizontal = ScreenPadding)) {
        Text("Lo sapevi?", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(14.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(148.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.primary)
                .bouncyClick { index = (index + 1) % facts.size }
                .padding(20.dp),
            contentAlignment = Alignment.CenterStart,
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
                    IconBubble(facts[i].first, 64.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f))
                    Spacer(Modifier.width(16.dp))
                    Text(
                        facts[i].second,
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 5,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            facts.indices.forEach { i ->
                val w by animateDpAsState(if (i == index) 18.dp else 6.dp, label = "dot")
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
    val nearest = remember(here) { allShops.minBy { distanceMeters(here, it.lat, it.lon) } }
    val dist = remember(here, nearest) { distanceMeters(here, nearest.lat, nearest.lon) }
    Column(Modifier.padding(horizontal = ScreenPadding)) {
        Text("La bottega più vicina", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(14.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .bouncyClick(onOpenShops)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconBubble(nearest.category.icon, 60.dp, nearest.category.color.copy(alpha = 0.16f))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(nearest.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
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

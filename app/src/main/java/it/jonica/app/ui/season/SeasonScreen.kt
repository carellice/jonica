package it.jonica.app.ui.season

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.jonica.app.data.Category
import it.jonica.app.data.Product
import it.jonica.app.data.monthNames
import it.jonica.app.data.products
import it.jonica.app.ui.components.IconBubble
import it.jonica.app.ui.components.ScreenPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Place
import it.jonica.app.ui.components.MonthsBar
import it.jonica.app.ui.components.Pill
import it.jonica.app.ui.components.WavyHeader
import it.jonica.app.ui.theme.Olive
import it.jonica.app.ui.theme.OliveDeep
import androidx.compose.ui.graphics.Color
import it.jonica.app.ui.components.bouncyClick
import it.jonica.app.ui.components.staggeredEnter
import java.time.LocalDate

@Composable
fun SeasonScreen(onProduct: (Product) -> Unit) {
    val currentMonth = remember { LocalDate.now().monthValue }
    var month by rememberSaveable { mutableIntStateOf(currentMonth) }
    var category by rememberSaveable { mutableStateOf(Category.FRUTTA) }
    val monthState = rememberLazyListState()
    LaunchedEffect(Unit) { monthState.scrollToItem((currentMonth - 3).coerceAtLeast(0)) }

    Column(Modifier.fillMaxSize()) {
        val floating = remember(month, category) { products.filter { it.category == category && it.inSeason(month) }.take(6).map { it.icon } }
        WavyHeader(color = Olive, secondColor = OliveDeep, floating = floating) {
            Column(Modifier.statusBarsPadding().padding(start = ScreenPadding, end = ScreenPadding, top = 20.dp, bottom = 44.dp)) {
                Text("Calendario delle stagioni", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                Text(
                    "Scegli un mese e scopri cosa offre la terra (e il mare) del Centro-Sud.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.88f),
                )
            }
        }
        LazyRow(
            state = monthState,
            contentPadding = PaddingValues(horizontal = ScreenPadding),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items((1..12).toList()) { m ->
                val sel = m == month
                val bg by animateColorAsState(
                    if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer,
                    tween(250), label = "m",
                )
                val fg by animateColorAsState(
                    if (sel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    tween(250), label = "mf",
                )
                Column(
                    Modifier
                        .size(width = 96.dp, height = 56.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(bg)
                        .bouncyClick { month = m },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(monthNames[m - 1], color = fg, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                    Spacer(Modifier.height(4.dp))
                    // pallino "oggi": stesso spazio su tutti i chip, così hanno la stessa altezza
                    Box(
                        Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(if (m == currentMonth) fg else Color.Transparent),
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        Row(
            Modifier.padding(horizontal = ScreenPadding).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Category.entries.forEach { c ->
                val sel = c == category
                val bg by animateColorAsState(
                    if (sel) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                    tween(250), label = "c",
                )
                Column(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(bg)
                        .bouncyClick { category = c }
                        .height(76.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    androidx.compose.foundation.Image(
                        androidx.compose.ui.res.painterResource(c.icon), null, Modifier.size(32.dp),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        c.label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (sel) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))

        AnimatedContent(
            targetState = month to category,
            transitionSpec = { (fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.97f)).togetherWith(fadeOut(tween(150))) },
            label = "grid",
        ) { (m, c) ->
            val list = remember(m, c) {
                products.filter { it.category == c && it.inSeason(m) }.sortedBy { it.name }
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(start = ScreenPadding, end = ScreenPadding, top = 8.dp, bottom = 120.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        "${list.size} prodotti di stagione a ${monthNames[m - 1].lowercase()}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (list.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text("Nessun prodotto in questo mese.", modifier = Modifier.padding(24.dp))
                    }
                }
                itemsIndexed(list, key = { _, p -> p.name }) { i, p ->
                    ProductTile(p, m, Modifier.staggeredEnter(i, m to c)) { onProduct(p) }
                }
            }
        }
    }
}

@Composable
private fun ProductTile(p: Product, month: Int, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .bouncyClick(onClick),
    ) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 6.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            IconBubble(p.icon, 64.dp, MaterialTheme.colorScheme.surface)
            Spacer(Modifier.height(10.dp))
            Text(
                p.name,
                fontSize = 13.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                minLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            )
        }
        when {
            p.isArriving(month) -> Badge("NEW", MaterialTheme.colorScheme.primary)
            p.isLeaving(month) -> Badge("ULTIMI", MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.BoxScope.Badge(text: String, color: Color) {
    Text(
        text,
        fontSize = 9.sp,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(8.dp)
            .clip(RoundedCornerShape(50))
            .background(color)
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductSheet(p: Product, onDismiss: () -> Unit) {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val month = remember { LocalDate.now().monthValue }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = state, containerColor = MaterialTheme.colorScheme.surface) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBubble(p.icon, 76.dp, MaterialTheme.colorScheme.primaryContainer)
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(p.name, style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(4.dp))
                    Pill(
                        if (p.inSeason(month)) "Di stagione adesso" else "Fuori stagione",
                        if (p.inSeason(month)) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                        if (p.inSeason(month)) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
            Text("Quando è di stagione", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            MonthsBar(p.months, month, Modifier.fillMaxWidth())
            Spacer(Modifier.height(20.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.tertiaryContainer)
                    .padding(16.dp),
            ) {
                Row {
                    androidx.compose.material3.Icon(
                        androidx.compose.material.icons.Icons.Rounded.Lightbulb, null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer, modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(p.tip, color = MaterialTheme.colorScheme.onTertiaryContainer, style = MaterialTheme.typography.bodyLarge)
                }
            }
            p.origin?.let {
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.Icon(
                        androidx.compose.material.icons.Icons.Rounded.Place, null,
                        tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Da cercare: $it", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (!p.inSeason(month)) {
                Spacer(Modifier.height(12.dp))
                Text(
                    "Se lo trovi adesso, probabilmente è cresciuto in serra o ha viaggiato a lungo. Prova un'alternativa di stagione!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
            Spacer(Modifier.size(8.dp))
        }
    }
}

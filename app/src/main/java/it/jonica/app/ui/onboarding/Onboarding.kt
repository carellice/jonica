package it.jonica.app.ui.onboarding

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.jonica.app.R
import it.jonica.app.data.allShops
import it.jonica.app.data.monthNames
import it.jonica.app.ui.aware.euros
import it.jonica.app.ui.theme.Cream
import it.jonica.app.ui.theme.Mustard
import it.jonica.app.ui.theme.Olive
import it.jonica.app.ui.theme.Sky
import it.jonica.app.ui.theme.Terracotta
import it.jonica.app.ui.components.bouncyClick
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private data class Page(val color: Color, val kicker: String, val title: String, val text: String)

private val pages = listOf(
    Page(Olive, "Benvenuto", "Jonica", "La spesa che fa bene a te, a chi coltiva e al tuo quartiere."),
    Page(Color(0xFF55772F), "Stagioni", "Ogni mese ha i suoi sapori", "Scopri cosa è davvero di stagione oggi: più gusto, meno serre riscaldate, meno chilometri."),
    Page(Sky, "Botteghe", "Compra vicino", "Fruttivendoli, forni, caseifici, frantoi e mercati del Lazio, dal più vicino a te."),
    Page(Terracotta, "Filiera", "Ogni euro conta", "All'agricoltore arrivano circa 20 centesimi di ogni euro speso: accorciamo la filiera insieme."),
    Page(Color(0xFF2E4420), "Si parte", "Pronti?", "Piccoli gesti, ogni settimana. Jonica ti accompagna."),
)

/** Introduzione all'app: 5 pagine con illustrazioni animate e indicatore "liquido". */
@Composable
fun Onboarding(onDone: () -> Unit) {
    val pager = rememberPagerState { pages.size }
    val scope = rememberCoroutineScope()
    // posizione continua del pager (es. 2.4 = tra la terza e la quarta pagina); letta solo nei
    // lambda di disegno, così lo swipe non ricompone l'intera schermata a ogni frame
    val position = { pager.currentPage + pager.currentPageOffsetFraction }
    val last by remember { derivedStateOf { pager.currentPage == pages.lastIndex } }

    Box(
        Modifier
            .fillMaxSize()
            // lo sfondo sfuma da una pagina all'altra seguendo il dito
            .drawBehind {
                val pos = position().coerceIn(0f, pages.lastIndex.toFloat())
                val i = pos.toInt().coerceAtMost(pages.lastIndex)
                val next = (i + 1).coerceAtMost(pages.lastIndex)
                drawRect(lerp(pages[i].color, pages[next].color, pos - i))
            }
            .grain(),
    ) {
        HorizontalPager(
            state = pager,
            beyondViewportPageCount = 1,
            modifier = Modifier.fillMaxSize(),
        ) { index ->
            PageContent(index, pager)
        }

        // "Salta" in alto a destra, sparisce sull'ultima pagina
        AnimatedVisibility(
            visible = !last,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(12.dp),
        ) {
            Text(
                "Salta",
                color = Cream,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.14f))
                    .pressable { onDone() }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }

        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 28.dp, vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LiquidIndicator(position, pages.size, Modifier.weight(1f))
            NextButton(last) {
                if (last) onDone() else scope.launch { pager.animateScrollToPage(pager.currentPage + 1, animationSpec = spring(dampingRatio = 0.85f, stiffness = 250f)) }
            }
        }
    }
}

@Composable
private fun PageContent(index: Int, pager: PagerState) {
    val page = pages[index]
    // distanza della pagina dal centro: 0 = visibile, ±1 = fuori
    val offset = { (pager.currentPage - index) + pager.currentPageOffsetFraction }
    // entrata: parte appena lo swipe punta a questa pagina, così è già viva mentre arriva
    val enter = remember { Animatable(0f) }
    val active by remember { derivedStateOf { pager.targetPage == index || pager.currentPage == index } }
    LaunchedEffect(active) {
        if (active) enter.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
    }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = 56.dp, bottom = 120.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BoxWithConstraints(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                // parallasse: l'illustrazione scorre più lentamente del testo
                .graphicsLayer {
                    val o = offset()
                    translationX = o * size.width * 0.3f
                    alpha = (1f - abs(o) * 1.3f).coerceIn(0f, 1f)
                    val s = 1f - 0.12f * abs(o).coerceIn(0f, 1f)
                    scaleX = s; scaleY = s
                },
            contentAlignment = Alignment.Center,
        ) {
            val side = minOf(maxWidth, maxHeight) * 0.92f
            Box(Modifier.size(side), contentAlignment = Alignment.Center) {
                when (index) {
                    0 -> WelcomeArt(enter.value)
                    1 -> SeasonWheel(enter.value, offset)
                    2 -> MapPinArt(enter.value)
                    3 -> EuroArt(enter.value)
                    else -> ReadyArt(enter.value)
                }
            }
        }
        Column(
            Modifier
                .padding(horizontal = 32.dp)
                .graphicsLayer {
                    val o = offset()
                    // il testo esce un po' più veloce della pagina: effetto profondità
                    translationX = -o * size.width * 0.2f
                    alpha = (1f - abs(o) * 1.5f).coerceIn(0f, 1f)
                },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                page.kicker.uppercase(),
                color = Mustard,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                letterSpacing = 3.sp,
                modifier = Modifier.rise(enter.value, 0f),
            )
            Spacer(Modifier.height(10.dp))
            Text(
                page.title,
                style = MaterialTheme.typography.displaySmall,
                color = Cream,
                textAlign = TextAlign.Center,
                modifier = Modifier.rise(enter.value, 0.12f),
            )
            Spacer(Modifier.height(12.dp))
            Text(
                page.text,
                style = MaterialTheme.typography.bodyLarge,
                color = Cream.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                modifier = Modifier.rise(enter.value, 0.24f),
            )
        }
    }
}

// ── illustrazioni ─────────────────────────────────────────────────────

private val orbitIcons = listOf(
    R.drawable.ic_arance, R.drawable.ic_pane, R.drawable.ic_olio, R.drawable.ic_pomodori,
    R.drawable.ic_formaggio, R.drawable.ic_carciofo, R.drawable.ic_uva, R.drawable.ic_pesce,
)

/** Il logo al centro e i prodotti che gli girano intorno come pianeti. */
@Composable
private fun WelcomeArt(enter: Float) {
    val inf = rememberInfiniteTransition(label = "orbit")
    val spin by inf.animateFloat(0f, 360f, infiniteRepeatable(tween(36_000, easing = LinearEasing)), label = "spin")
    val breathe by inf.animateFloat(0.97f, 1.03f, infiniteRepeatable(tween(2400), RepeatMode.Reverse), label = "b")
    BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val r = maxWidth * 0.38f
        Canvas(Modifier.fillMaxSize()) {
            val c = center
            drawCircle(Color.White.copy(alpha = 0.07f * enter), radius = r.toPx(), center = c, style = Stroke(1.5.dp.toPx()))
            drawCircle(Color.White.copy(alpha = 0.05f * enter), radius = r.toPx() * 0.72f, center = c, style = Stroke(1.dp.toPx()))
        }
        orbitIcons.forEachIndexed { i, icon ->
            val local = ((enter * 1.6f) - i * 0.08f).coerceIn(0f, 1f)
            val angle = Math.toRadians((spin + i * 360f / orbitIcons.size).toDouble())
            val bob = sin(Math.toRadians(spin * 4.0 + i * 50)).toFloat() * 6f
            Image(
                painterResource(icon), null,
                Modifier
                    .size(54.dp)
                    .graphicsLayer {
                        val radius = r.toPx() * (0.4f + 0.6f * local)
                        translationX = (cos(angle) * radius).toFloat()
                        translationY = (sin(angle) * radius).toFloat() + bob
                        scaleX = local; scaleY = local
                        alpha = local
                    },
            )
        }
        Image(
            painterResource(R.drawable.logo), null,
            Modifier
                .size(maxWidth * 0.42f)
                .graphicsLayer {
                    val s = (0.6f + 0.4f * enter) * breathe
                    scaleX = s; scaleY = s
                    rotationZ = (1f - enter) * -25f
                }
                .shadow(24.dp, CircleShape),
        )
    }
}

private val wheelIcons = listOf(
    R.drawable.ic_arance, R.drawable.ic_carciofo, R.drawable.ic_asparagi, R.drawable.ic_fave,
    R.drawable.ic_fragole, R.drawable.ic_ciliegie, R.drawable.ic_pomodori, R.drawable.ic_anguria,
    R.drawable.ic_uva, R.drawable.ic_castagne, R.drawable.ic_olio, R.drawable.ic_mandarini,
)

/** La ruota dell'anno: gira con lo swipe e si ferma con il mese corrente in alto. */
@Composable
private fun SeasonWheel(enter: Float, offset: () -> Float) {
    val month = remember { LocalDate.now().monthValue }
    val inf = rememberInfiniteTransition(label = "wheel")
    val pulse by inf.animateFloat(1f, 1.12f, infiniteRepeatable(tween(1100), RepeatMode.Reverse), label = "pulse")
    // a fine entrata il mese di oggi è in cima (-90°)
    val target = -90f - (month - 1) * 30f
    val rotation = { target + (1f - enter) * 140f + offset() * 90f }
    BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val r = maxWidth * 0.36f
        Canvas(Modifier.fillMaxSize().graphicsLayer { alpha = enter }) {
            val c = center
            val rot = rotation()
            val ring = r.toPx()
            drawCircle(Color.White.copy(alpha = 0.12f), radius = ring, center = c, style = Stroke(2.dp.toPx()))
            for (m in 0 until 12) {
                val a = Math.toRadians((rot + m * 30f).toDouble())
                val p = Offset(c.x + (cos(a) * ring).toFloat(), c.y + (sin(a) * ring).toFloat())
                val current = m == month - 1
                drawCircle(
                    color = if (current) Mustard else Color.White.copy(alpha = 0.16f),
                    radius = (if (current) 32.dp else 24.dp).toPx() * enter,
                    center = p,
                )
            }
            // puntatore in alto
            drawCircle(Mustard, radius = 6.dp.toPx(), center = Offset(c.x, c.y - ring - 38.dp.toPx()))
        }
        wheelIcons.forEachIndexed { m, icon ->
            val current = m == month - 1
            Image(
                painterResource(icon), null,
                Modifier
                    .size(if (current) 46.dp else 34.dp)
                    .graphicsLayer {
                        val a = Math.toRadians((rotation() + m * 30f).toDouble())
                        translationX = (cos(a) * r.toPx()).toFloat()
                        translationY = (sin(a) * r.toPx()).toFloat()
                        val s = enter * if (current) pulse else 1f
                        scaleX = s; scaleY = s
                    },
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.graphicsLayer { alpha = enter }) {
            Text("oggi è", color = Cream.copy(alpha = 0.7f), fontSize = 14.sp)
            Text(monthNames[month - 1], color = Cream, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp)
        }
    }
}

private val pinIcons = listOf(
    R.drawable.ic_cassetta, R.drawable.ic_pane, R.drawable.ic_carne, R.drawable.ic_ricotta,
    R.drawable.ic_olio, R.drawable.ic_salumi, R.drawable.ic_pesce, R.drawable.ic_bancarella,
)

/** Una mappa stilizzata: lo spillo cade con un rimbalzo e le botteghe spuntano intorno. */
@Composable
private fun MapPinArt(enter: Float) {
    val inf = rememberInfiniteTransition(label = "pin")
    val ripple by inf.animateFloat(0f, 1f, infiniteRepeatable(tween(1800, easing = LinearEasing)), label = "ripple")
    val drop = remember { Animatable(0f) }
    LaunchedEffect(enter > 0.2f) {
        if (enter > 0.2f) drop.animateTo(1f, spring(dampingRatio = 0.42f, stiffness = 260f))
    }
    BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val card = maxWidth * 0.82f
        Box(
            Modifier
                .size(card)
                .graphicsLayer {
                    rotationX = 38f * enter
                    rotationZ = -10f * enter
                    cameraDistance = 18f * density
                    alpha = enter
                    scaleX = 0.8f + 0.2f * enter; scaleY = 0.8f + 0.2f * enter
                }
                .clip(RoundedCornerShape(36.dp))
                .background(Color.White.copy(alpha = 0.14f))
                .drawBehind {
                    val road = Color.White.copy(alpha = 0.22f)
                    val w = size.width
                    val h = size.height
                    drawLine(road, Offset(0f, h * 0.35f), Offset(w, h * 0.55f), 14.dp.toPx(), StrokeCap.Round)
                    drawLine(road, Offset(w * 0.3f, 0f), Offset(w * 0.45f, h), 10.dp.toPx(), StrokeCap.Round)
                    drawLine(road, Offset(w * 0.7f, 0f), Offset(w * 0.85f, h), 6.dp.toPx(), StrokeCap.Round)
                    drawCircle(Color(0x334E8F3A), radius = w * 0.16f, center = Offset(w * 0.78f, h * 0.22f))
                    // onde che partono dal punto in cui cade lo spillo
                    val p = Offset(w * 0.5f, h * 0.48f)
                    for (k in 0..1) {
                        val t = (ripple + k * 0.5f) % 1f
                        drawCircle(Mustard.copy(alpha = (1f - t) * 0.5f * drop.value), radius = w * 0.08f + w * 0.3f * t, center = p, style = Stroke(3.dp.toPx()))
                    }
                },
        )
        // botteghe che spuntano intorno allo spillo
        pinIcons.forEachIndexed { i, icon ->
            val local = ((drop.value * 1.4f) - i * 0.09f).coerceIn(0f, 1.2f)
            val a = Math.toRadians(i * 45.0 + 20)
            val dist = card * 0.36f
            Box(
                Modifier
                    .graphicsLayer {
                        translationX = (cos(a) * dist.toPx()).toFloat()
                        translationY = (sin(a) * dist.toPx() * 0.7f).toFloat()
                        val s = local.coerceAtMost(1f)
                        scaleX = s; scaleY = s
                    }
                    .size(44.dp)
                    .shadow(6.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Cream),
                contentAlignment = Alignment.Center,
            ) {
                Image(painterResource(icon), null, Modifier.size(30.dp))
            }
        }
        // lo spillo
        Canvas(
            Modifier
                .size(64.dp, 84.dp)
                .graphicsLayer {
                    translationY = (1f - drop.value) * -420f - 30.dp.toPx()
                    alpha = if (drop.value > 0f) 1f else 0f
                },
        ) {
            val w = size.width
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(w / 2, size.height)
                cubicTo(w * 0.1f, size.height * 0.55f, 0f, size.height * 0.4f, 0f, w / 2)
                arcTo(androidx.compose.ui.geometry.Rect(0f, 0f, w, w), 180f, 180f, false)
                cubicTo(w, size.height * 0.4f, w * 0.9f, size.height * 0.55f, w / 2, size.height)
                close()
            }
            drawPath(path, Terracotta)
            drawCircle(Cream, radius = w * 0.2f, center = Offset(w / 2, w / 2))
        }
        Text(
            "${allShops.size}",
            color = Cream,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 22.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .graphicsLayer { alpha = drop.value.coerceIn(0f, 1f) }
                .clip(RoundedCornerShape(50))
                .background(Color.Black.copy(alpha = 0.18f))
                .padding(horizontal = 16.dp, vertical = 6.dp),
        )
    }
}

/** La ciambella dei 20 centesimi che si riempie. */
@Composable
private fun EuroArt(enter: Float) {
    val inf = rememberInfiniteTransition(label = "euro")
    val float by inf.animateFloat(-8f, 8f, infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "f")
    BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val r = maxWidth * 0.32f
        Canvas(Modifier.size(r * 2.4f)) {
            val stroke = 34.dp.toPx()
            val inset = stroke / 2
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(Color.White.copy(alpha = 0.16f), -90f, 360f * enter, false, Offset(inset, inset), arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
            drawArc(Mustard, -90f, 72f * ((enter - 0.25f) / 0.75f).coerceIn(0f, 1f), false, Offset(inset, inset), arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painterResource(R.drawable.ic_contadino), null, Modifier.size(56.dp).graphicsLayer { translationY = float; alpha = enter })
            Text(
                euros((20 * ((enter - 0.25f) / 0.75f).coerceIn(0f, 1f)).toInt()),
                color = Cream,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 44.sp,
            )
            Text("su 1 €", color = Cream.copy(alpha = 0.7f), fontSize = 14.sp)
        }
        // il viaggio del prodotto: camion e banco che si allontanano dall'agricoltore
        Image(
            painterResource(R.drawable.ic_camion), null,
            Modifier.size(60.dp).graphicsLayer {
                translationX = r.toPx() * 1.05f
                translationY = -r.toPx() * 0.95f + float
                alpha = enter
                rotationZ = -8f
            },
        )
        Image(
            painterResource(R.drawable.ic_bancarella), null,
            Modifier.size(60.dp).graphicsLayer {
                translationX = -r.toPx() * 1.05f
                translationY = r.toPx() * 0.95f - float
                alpha = enter
                rotationZ = 6f
            },
        )
    }
}

/** Il cestino della spesa che rimbalza e tre buoni propositi che si spuntano. */
@Composable
private fun ReadyArt(enter: Float) {
    val inf = rememberInfiniteTransition(label = "ready")
    val hop by inf.animateFloat(0f, 1f, infiniteRepeatable(tween(1400), RepeatMode.Restart), label = "hop")
    val items = listOf(
        R.drawable.ic_arance to "Frutta e verdura di stagione",
        R.drawable.ic_bancarella to "Una spesa in bottega",
        R.drawable.ic_contadino to "Chiedere da dove arriva",
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        val jump = 4f * hop * (1f - hop)
        Image(
            painterResource(R.drawable.ic_cesto), null,
            Modifier
                .size(130.dp)
                .graphicsLayer {
                    translationY = -jump * 40.dp.toPx()
                    // si schiaccia un po' quando tocca terra
                    val squash = if (hop < 0.08f || hop > 0.92f) 0.08f else 0f
                    scaleX = enter * (1f + squash); scaleY = enter * (1f - squash)
                },
        )
        Spacer(Modifier.height(28.dp))
        items.forEachIndexed { i, (icon, text) ->
            CheckRow(icon, text, ((enter * 1.8f) - 0.3f - i * 0.25f).coerceIn(0f, 1f))
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun CheckRow(@DrawableRes icon: Int, text: String, progress: Float) {
    Row(
        Modifier
            .graphicsLayer { alpha = progress; translationX = (1f - progress) * 60.dp.toPx() }
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.12f))
            .padding(start = 6.dp, end = 16.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(painterResource(icon), null, Modifier.size(32.dp))
        Spacer(Modifier.width(10.dp))
        Text(text, color = Cream, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Spacer(Modifier.width(10.dp))
        Box(
            Modifier
                .size(22.dp)
                .graphicsLayer { val s = ((progress - 0.6f) / 0.4f).coerceIn(0f, 1f); scaleX = s; scaleY = s }
                .clip(CircleShape)
                .background(Mustard),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.Check, null, tint = Olive, modifier = Modifier.size(16.dp))
        }
    }
}

// ── controlli ─────────────────────────────────────────────────────────

/**
 * Indicatore "liquido": durante lo swipe il bordo di testa scatta in avanti e quello di coda
 * lo raggiunge dopo, così il segno si allunga come una goccia tra un pallino e l'altro.
 */
@Composable
private fun LiquidIndicator(position: () -> Float, count: Int, modifier: Modifier = Modifier) {
    Canvas(modifier.height(12.dp)) {
        val dot = 8.dp.toPx()
        val gap = 14.dp.toPx()
        val pill = 26.dp.toPx()
        val step = dot + gap
        val y = size.height / 2
        for (i in 0 until count) {
            drawCircle(Color.White.copy(alpha = 0.3f), radius = dot / 2, center = Offset(i * step + dot / 2, y))
        }
        val pos = position().coerceIn(0f, (count - 1).toFloat())
        val i = pos.toInt().coerceIn(0, count - 1)
        val t = (pos - i).coerceIn(0f, 1f)
        val lead = sqrt(t)        // il bordo davanti corre
        val trail = t * t         // quello dietro insegue
        val left = (i + trail) * step
        val right = (i + lead) * step + pill
        val h = dot * (1f - 0.25f * (lead - trail))  // si assottiglia mentre si allunga
        drawRoundRect(Cream, topLeft = Offset(left, y - h / 2), size = Size(right - left, h), cornerRadius = CornerRadius(h / 2))
    }
}

@Composable
private fun NextButton(last: Boolean, onClick: () -> Unit) {
    // si allarga solo quando arriva all'ultima pagina; per il resto resta fermo
    val width by animateDpAsState(if (last) 168.dp else 64.dp, spring(dampingRatio = 0.85f, stiffness = 300f), label = "w")
    Box(Modifier.height(64.dp), contentAlignment = Alignment.CenterEnd) {
        Row(
            Modifier
                .size(width, 64.dp)
                .shadow(12.dp, RoundedCornerShape(50))
                .clip(RoundedCornerShape(50))
                .background(Cream)
                .pressable(onClick),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AnimatedVisibility(last, enter = fadeIn(tween(250, delayMillis = 120)), exit = fadeOut(tween(100))) {
                Text("Inizia", color = Olive, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, modifier = Modifier.padding(end = 8.dp))
            }
            Icon(Icons.AutoMirrored.Rounded.ArrowForward, if (last) "Inizia" else "Avanti", tint = Olive)
        }
    }
}

// ── piccoli helper ────────────────────────────────────────────────────

/** Testo che sale e appare, con un ritardo relativo dentro l'animazione d'entrata. */
private fun Modifier.rise(enter: Float, delay: Float): Modifier = graphicsLayer {
    val p = ((enter - delay) / (1f - delay)).coerceIn(0f, 1f)
    alpha = p
    translationY = (1f - p) * 40.dp.toPx()
}

private fun Modifier.pressable(onClick: () -> Unit): Modifier = bouncyClick(onClick)

/** Grana leggera, come la carta di un sacchetto del pane. */
private fun Modifier.grain(): Modifier = drawBehind {
    val rnd = java.util.Random(11)
    repeat(260) {
        drawCircle(
            Color.White.copy(alpha = 0.03f + rnd.nextFloat() * 0.04f),
            radius = 1.dp.toPx() + rnd.nextFloat() * 1.5f.dp.toPx(),
            center = Offset(rnd.nextFloat() * size.width, rnd.nextFloat() * size.height),
        )
    }
}

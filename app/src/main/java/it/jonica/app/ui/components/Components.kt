package it.jonica.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.jonica.app.data.monthShort
import kotlin.math.sin

/** Margine orizzontale unico per tutte le schermate. */
val ScreenPadding = 20.dp

/** Click con rimbalzo: l'elemento si comprime leggermente mentre è premuto. */
fun Modifier.bouncyClick(onClick: () -> Unit): Modifier = composed {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (pressed) 0.95f else 1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "bounce",
    )
    this
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .clickable(interactionSource = source, indication = null, onClick = onClick)
}

/** Entrata a cascata: fade + slide dal basso, ritardata in base all'indice. */
fun Modifier.staggeredEnter(index: Int, key: Any? = Unit): Modifier = composed {
    val progress = remember(key) { Animatable(0f) }
    LaunchedEffect(key) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(durationMillis = 420, delayMillis = (index.coerceAtMost(12)) * 45))
    }
    graphicsLayer {
        alpha = progress.value
        translationY = (1f - progress.value) * 48f
        val s = 0.96f + 0.04f * progress.value
        scaleX = s; scaleY = s
    }
}

@Composable
fun SectionTitle(title: String, subtitle: String? = null, modifier: Modifier = Modifier) {
    Column(modifier.padding(horizontal = ScreenPadding)) {
        Text(title, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
        if (subtitle != null) {
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Icona illustrata dentro un cerchio. */
@Composable
fun IconBubble(@DrawableRes icon: Int, size: Dp = 52.dp, background: Color = MaterialTheme.colorScheme.surfaceVariant) {
    Box(
        Modifier.size(size).clip(CircleShape).background(background),
        contentAlignment = Alignment.Center,
    ) {
        Image(painterResource(icon), contentDescription = null, modifier = Modifier.size(size * 0.68f))
    }
}

@Composable
fun Pill(text: String, color: Color, contentColor: Color, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(50))
            .background(color)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = contentColor, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, textAlign = TextAlign.Center)
    }
}

/** Barra dei 12 mesi con i mesi di stagione evidenziati. */
@Composable
fun MonthsBar(months: Set<Int>, current: Int, modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        for (m in 1..12) {
            val on = m in months
            val anim = remember { Animatable(0f) }
            LaunchedEffect(months) { anim.snapTo(0f); anim.animateTo(1f, tween(300, delayMillis = m * 40)) }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(width = 22.dp, height = 30.dp), contentAlignment = Alignment.BottomCenter) {
                    Box(
                        Modifier
                            .graphicsLayer { scaleY = if (on) anim.value else 1f; transformOrigin = TransformOrigin(0.5f, 1f) }
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when {
                                    on && m == current -> MaterialTheme.colorScheme.secondary
                                    on -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                },
                            )
                            .size(width = 22.dp, height = if (on) 30.dp else 10.dp),
                    )
                }
                Text(
                    monthShort[m - 1],
                    fontSize = 11.sp,
                    fontWeight = if (m == current) FontWeight.ExtraBold else FontWeight.Medium,
                    color = if (m == current) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

/**
 * Intestazione organica: fondo colorato con bordo inferiore a onda che si muove lentamente
 * e illustrazioni che fluttuano sullo sfondo.
 */
@Composable
fun WavyHeader(
    color: Color,
    floating: List<Int>,
    modifier: Modifier = Modifier,
    secondColor: Color = color,
    content: @Composable BoxScope.() -> Unit,
) {
    val inf = rememberInfiniteTransition(label = "wave")
    val phase by inf.animateFloat(
        0f, (2 * Math.PI).toFloat(),
        infiniteRepeatable(tween(7000, easing = LinearEasing)),
        label = "phase",
    )
    val drift by inf.animateFloat(0f, 1f, infiniteRepeatable(tween(14000, easing = LinearEasing)), label = "drift")
    Box(
        modifier
            .fillMaxWidth()
            .drawBehind {
                val amp = 12.dp.toPx()
                val base = size.height - amp * 2
                val path = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width, 0f)
                    lineTo(size.width, base)
                    val steps = 48
                    for (i in steps downTo 0) {
                        val x = size.width * i / steps
                        val y = base + amp * sin(phase + x / size.width * 2f * Math.PI.toFloat())
                        lineTo(x, y)
                    }
                    close()
                }
                drawPath(path, Brush.linearGradient(listOf(color, secondColor), end = Offset(size.width, size.height)))
                // texture a puntini, come carta di un sacchetto del pane
                val rnd = java.util.Random(7)
                repeat(140) {
                    val x = rnd.nextFloat() * size.width
                    val y = rnd.nextFloat() * base
                    drawCircle(Color.White.copy(alpha = 0.04f + rnd.nextFloat() * 0.05f), radius = 1.dp.toPx() + rnd.nextFloat() * 1.5f.dp.toPx(), center = Offset(x, y))
                }
            },
    ) {
        BoxWithConstraints(Modifier.matchParentSize()) {
            val w = maxWidth
            val h = maxHeight
            floating.forEachIndexed { i, icon ->
                // colonne equidistanti
                val col = (i + 0.5f) / floating.size
                val t = (drift + (i * 0.37f) % 1f) % 1f
                Image(
                    painterResource(icon),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = (w * col - 18.dp).coerceAtLeast(0.dp), top = h * (0.8f - 0.7f * t))
                        .size(36.dp)
                        .graphicsLayer {
                            alpha = 0.3f * sin(t * Math.PI).toFloat()
                            rotationZ = 16f * sin(t * 6f + i)
                        },
                )
            }
        }
        content()
    }
}

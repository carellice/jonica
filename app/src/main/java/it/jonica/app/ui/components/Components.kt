package it.jonica.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.draw.drawBehind
import androidx.compose.animation.core.animateFloat
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Click con rimbalzo: l'elemento si comprime leggermente mentre è premuto. */
fun Modifier.bouncyClick(onClick: () -> Unit): Modifier = composed {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (pressed) 0.94f else 1f,
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
    Column(modifier.padding(horizontal = 20.dp)) {
        Text(title, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onBackground)
        if (subtitle != null) {
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun EmojiBubble(emoji: String, size: Dp = 52.dp, background: Color = MaterialTheme.colorScheme.surfaceVariant) {
    Box(
        Modifier.size(size).clip(CircleShape).background(background),
        contentAlignment = Alignment.Center,
    ) {
        Text(emoji, fontSize = (size.value * 0.5f).sp)
    }
}

@Composable
fun Pill(text: String, color: Color, contentColor: Color, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(50))
            .background(color)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(text, color = contentColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
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
                Box(
                    Modifier
                        .padding(bottom = 4.dp)
                        .graphicsLayer { scaleY = if (on) anim.value else 1f; transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 1f) }
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when {
                                on && m == current -> MaterialTheme.colorScheme.secondary
                                on -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            },
                        )
                        .size(width = 22.dp, height = if (on) 30.dp else 12.dp),
                )
                Text(
                    it.jonica.app.data.monthShort[m - 1],
                    fontSize = 11.sp,
                    fontWeight = if (m == current) FontWeight.Bold else FontWeight.Normal,
                    color = if (m == current) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * Intestazione organica: fondo colorato con bordo inferiore a onda che si muove lentamente
 * ed emoji che fluttuano sullo sfondo.
 */
@Composable
fun WavyHeader(
    color: Color,
    floating: List<String>,
    modifier: Modifier = Modifier,
    secondColor: Color = color,
    content: @Composable androidx.compose.foundation.layout.BoxScope.() -> Unit,
) {
    val inf = androidx.compose.animation.core.rememberInfiniteTransition(label = "wave")
    val phase by inf.animateFloat(
        0f, (2 * Math.PI).toFloat(),
        androidx.compose.animation.core.infiniteRepeatable(tween(7000, easing = androidx.compose.animation.core.LinearEasing)),
        label = "phase",
    )
    val drift by inf.animateFloat(
        0f, 1f,
        androidx.compose.animation.core.infiniteRepeatable(tween(14000, easing = androidx.compose.animation.core.LinearEasing)),
        label = "drift",
    )
    Box(
        modifier
            .fillMaxWidth()
            .drawBehind {
                val amp = 14.dp.toPx()
                val base = size.height - amp * 2
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width, 0f)
                    lineTo(size.width, base)
                    val steps = 48
                    for (i in steps downTo 0) {
                        val x = size.width * i / steps
                        val y = base + amp * kotlin.math.sin(phase + x / size.width * 2.2f * Math.PI.toFloat()) +
                            amp * 0.4f * kotlin.math.sin(phase * 2 + x / size.width * 5f)
                        lineTo(x, y)
                    }
                    close()
                }
                drawPath(path, androidx.compose.ui.graphics.Brush.linearGradient(listOf(color, secondColor), end = androidx.compose.ui.geometry.Offset(size.width, size.height)))
                // texture a puntini, come carta di un sacchetto del pane
                val rnd = java.util.Random(7)
                repeat(140) {
                    val x = rnd.nextFloat() * size.width
                    val y = rnd.nextFloat() * base
                    drawCircle(Color.White.copy(alpha = 0.05f + rnd.nextFloat() * 0.06f), radius = 1.dp.toPx() + rnd.nextFloat() * 1.5f.dp.toPx(), center = androidx.compose.ui.geometry.Offset(x, y))
                }
            },
    ) {
      androidx.compose.foundation.layout.BoxWithConstraints(Modifier.matchParentSize()) {
        val w = maxWidth
        val h = maxHeight
        floating.forEachIndexed { i, e ->
            val seed = (i * 37 + 11) % 100 / 100f
            val t = (drift + i / floating.size.toFloat()) % 1f
            Text(
                e,
                fontSize = (22 + (i % 3) * 8).sp,
                modifier = Modifier
                    .padding(start = w * (0.08f + 0.84f * seed), top = h * (0.85f - 0.75f * t))
                    .graphicsLayer {
                        alpha = 0.3f * kotlin.math.sin(t * Math.PI).toFloat()
                        rotationZ = 20f * kotlin.math.sin(t * 6f + i)
                    },
            )
        }
      }
        content()
    }
}

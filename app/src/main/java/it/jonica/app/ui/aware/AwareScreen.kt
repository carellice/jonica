package it.jonica.app.ui.aware

import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.jonica.app.ui.components.WavyHeader
import it.jonica.app.ui.components.IconBubble
import it.jonica.app.R
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import it.jonica.app.ui.components.bouncyClick
import it.jonica.app.ui.components.staggeredEnter

private data class Slice(val label: String, val cents: Int, val color: Color, @DrawableRes val icon: Int)

// Ripartizione indicativa di 1 € di frutta e verdura fresca acquistata nella grande distribuzione.
private val slices = listOf(
    Slice("Agricoltore", 20, Color(0xFF5C8A3A), R.drawable.ic_contadino),
    Slice("Trasporto e logistica", 15, Color(0xFFE3A93B), R.drawable.ic_camion),
    Slice("Intermediari e ingrosso", 15, Color(0xFF3E6B8C), R.drawable.ic_cassetta),
    Slice("Distribuzione e vendita", 40, Color(0xFFC8643B), R.drawable.ic_bancarella),
    Slice("Imballaggi e altro", 10, Color(0xFF8C7A6B), R.drawable.ic_cesto),
)

private val pledges = listOf(
    R.drawable.ic_pane to "Questa settimana faccio almeno una spesa in una bottega di quartiere",
    R.drawable.ic_arance to "Compro solo frutta e verdura di stagione",
    R.drawable.ic_contadino to "Chiedo da dove arrivano i prodotti che compro",
    R.drawable.ic_bancarella to "Vado almeno una volta al mercato rionale",
    R.drawable.ic_olio to "Leggo l'origine in etichetta prima di mettere nel carrello",
    R.drawable.ic_pomodori to "Non spreco: congelo, riuso gli avanzi, compro il giusto",
    R.drawable.ic_cesto to "Porto la mia borsa e scelgo prodotti sfusi",
)

private val labelTips = listOf(
    "Frutta e verdura" to "Il Paese d'origine è obbligatorio sul cartellino. Se manca, chiedilo.",
    "Olio" to "'Extravergine 100% italiano' è diverso da 'miscela di oli comunitari'.",
    "Pesce" to "Cerca zona FAO 37 (Mediterraneo) e il metodo: pescato o allevato.",
    "Uova" to "Primo numero: 0 bio, 1 all'aperto, 2 a terra, 3 in gabbia. Poi la sigla IT.",
    "Pasta" to "Per legge va indicato il Paese di coltivazione del grano.",
    "Miele" to "Diffida di 'miscela di mieli UE e non UE'.",
)

@Composable
fun AwareScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("jonica", Context.MODE_PRIVATE) }
    var done by remember { mutableStateOf(prefs.getStringSet("pledges", emptySet())!!.toSet()) }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item {
            WavyHeader(
                color = MaterialTheme.colorScheme.secondary,
                floating = listOf(R.drawable.ic_contadino, R.drawable.ic_grano, R.drawable.ic_olio, R.drawable.ic_camion, R.drawable.ic_pomodori),
            ) {
                Column(Modifier.statusBarsPadding().padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 56.dp)) {
                    Text("Filiera giusta", style = MaterialTheme.typography.headlineLarge, color = Color.White)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Ogni spesa è un voto. Ecco dove vanno i tuoi soldi e cosa puoi fare, da domani.",
                        color = Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }

        item { EuroBreakdown() }

        item {
            Column(Modifier.padding(horizontal = 20.dp)) {
                Text("Perché la stagionalità conta", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(12.dp))
                Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Reason(R.drawable.ic_estate, "Meno energia", "Niente serre riscaldate", Modifier.weight(1f))
                    Reason(R.drawable.ic_camion, "Meno viaggi", "Meno gasolio, prezzi stabili", Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
                Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Reason(R.drawable.ic_fragole, "Più gusto", "Matura sulla pianta", Modifier.weight(1f))
                    Reason(R.drawable.ic_cesto, "Prezzo giusto", "Abbondanza = costa meno", Modifier.weight(1f))
                }
            }
        }

        item {
            PledgeHeader(done.size, pledges.size)
        }
        itemsIndexed(pledges) { i, (icon, text) ->
            val checked = text in done
            Pledge(icon, text, checked, Modifier.staggeredEnter(i)) {
                done = if (checked) done - text else done + text
                prefs.edit().putStringSet("pledges", done).apply()
            }
        }

        item {
            Column(Modifier.padding(horizontal = 20.dp)) {
                Text("Leggi l'etichetta", style = MaterialTheme.typography.headlineSmall)
                Text("Pochi secondi che fanno la differenza", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                labelTips.forEachIndexed { i, (title, tip) -> LabelTip(title, tip, Modifier.staggeredEnter(i)) }
            }
        }

        item {
            Text(
                "Le cifre sono stime indicative basate su studi ISMEA sulla catena del valore agroalimentare: variano per prodotto e canale di vendita.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
        }
    }
}

@Composable
private fun EuroBreakdown() {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) { progress.animateTo(1f, tween(1400, easing = FastOutSlowInEasing)) }
    var selected by remember { mutableStateOf(0) }

    Column(
        Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(20.dp),
    ) {
        Text("Dove va 1 € della tua spesa", style = MaterialTheme.typography.titleLarge)
        Text("Frutta e verdura fresche al supermercato", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(150.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    var start = -90f
                    slices.forEachIndexed { i, s ->
                        val sweep = 360f * s.cents / 100f * progress.value
                        val w = if (i == selected) 30.dp.toPx() else 20.dp.toPx()
                        drawArc(
                            s.color, start + 1.5f, (sweep - 3f).coerceAtLeast(0f), false,
                            style = Stroke(width = w, cap = StrokeCap.Butt),
                            topLeft = androidx.compose.ui.geometry.Offset(15.dp.toPx(), 15.dp.toPx()),
                            size = androidx.compose.ui.geometry.Size(size.width - 30.dp.toPx(), size.height - 30.dp.toPx()),
                        )
                        start += 360f * s.cents / 100f * progress.value
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    androidx.compose.foundation.Image(androidx.compose.ui.res.painterResource(slices[selected].icon), null, Modifier.size(34.dp))
                    Text(
                        "${(slices[selected].cents * progress.value).toInt()}¢",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = slices[selected].color,
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                slices.forEachIndexed { i, s ->
                    val alpha by animateFloatAsState(if (i == selected) 1f else 0.6f, label = "a")
                    Row(
                        Modifier.graphicsLayer { this.alpha = alpha }.bouncyClick { selected = i },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(Modifier.size(10.dp).clip(CircleShape).background(s.color))
                        Spacer(Modifier.width(8.dp))
                        Text(s.label, fontSize = 12.sp, fontWeight = if (i == selected) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "All'agricoltore arrivano circa 20 centesimi. Comprando in bottega o dal produttore, la filiera si accorcia e una parte più grande resta a chi coltiva e a chi vende nel tuo quartiere.",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun Reason(@DrawableRes icon: Int, title: String, subtitle: String, modifier: Modifier) {
    Column(
        modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(16.dp),
    ) {
        IconBubble(icon, 48.dp, MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
        Spacer(Modifier.height(10.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
    }
}

@Composable
private fun PledgeHeader(done: Int, total: Int) {
    val p by animateFloatAsState(done / total.toFloat(), spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "pledge")
    val primary = MaterialTheme.colorScheme.primary
    val track = MaterialTheme.colorScheme.surfaceVariant
    Row(Modifier.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("I miei impegni", style = MaterialTheme.typography.headlineSmall)
            Text(
                when {
                    done == 0 -> "Inizia da uno. Uno basta."
                    done == total -> "Fantastico! Sei un esempio per il quartiere"
                    else -> "$done su $total: continua così!"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Box(Modifier.size(64.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                drawArc(track, 0f, 360f, false, style = Stroke(8.dp.toPx()))
                drawArc(primary, -90f, 360f * p, false, style = Stroke(8.dp.toPx(), cap = StrokeCap.Round))
            }
            Text("${(p * 100).toInt()}%", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
private fun Pledge(@DrawableRes icon: Int, text: String, checked: Boolean, modifier: Modifier, onToggle: () -> Unit) {
    val bg by animateColorAsState(
        if (checked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
        tween(300), label = "pbg",
    )
    val checkScale by animateFloatAsState(if (checked) 1f else 0f, spring(dampingRatio = 0.4f), label = "chk")
    Row(
        modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(bg)
            .bouncyClick(onToggle)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconBubble(icon, 44.dp, MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
        Spacer(Modifier.width(14.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 2, minLines = 2, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(10.dp))
        Box(
            Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Rounded.Check, null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(18.dp).graphicsLayer { scaleX = checkScale; scaleY = checkScale },
            )
        }
    }
}

@Composable
private fun LabelTip(title: String, tip: String, modifier: Modifier) {
    var open by remember { mutableStateOf(false) }
    Column(
        modifier
            .padding(bottom = 8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .bouncyClick { open = !open }
            .padding(16.dp)
            .animateContentSize(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(4.dp).height(18.dp).clip(RoundedCornerShape(2.dp)).background(MaterialTheme.colorScheme.secondary))
            Spacer(Modifier.width(10.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Text(if (open) "−" else "+", fontSize = 20.sp, color = MaterialTheme.colorScheme.secondary)
        }
        if (open) {
            Spacer(Modifier.height(8.dp))
            Text(tip, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

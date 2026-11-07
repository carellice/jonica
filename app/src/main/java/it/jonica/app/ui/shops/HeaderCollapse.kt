package it.jonica.app.ui.shops

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import kotlin.math.roundToInt

/**
 * Intestazione che si ritira mentre la lista scorre verso l'alto, così la lista ha tutto lo
 * schermo; resta solo la striscia colorata dietro la barra di stato.
 */
internal class HeaderCollapse {
    /** px di intestazione nascosti, da 0 a [max]. */
    val hidden = mutableFloatStateOf(0f)

    /** px nascondibili: altezza dell'intestazione meno la barra di stato, aggiornata nel layout. */
    var max = 0f

    val fraction: Float get() = if (max > 0f) (hidden.floatValue / max).coerceIn(0f, 1f) else 0f

    /** Nasconde (dy > 0) o mostra (dy < 0) l'intestazione; restituisce i px usati. */
    fun consume(dy: Float): Float {
        val c = hidden.floatValue
        val next = (c + dy).coerceIn(0f, max)
        hidden.floatValue = next
        return next - c
    }

    val partial: Boolean get() = hidden.floatValue > 0f && hidden.floatValue < max

    /** A fine fling l'intestazione si aggancia aperta o chiusa. */
    suspend fun settle(velocity: Float) {
        if (!partial) return
        val c = hidden.floatValue
        val target = when {
            velocity < -800f -> max
            velocity > 800f -> 0f
            c > max / 2 -> max
            else -> 0f
        }
        animate(c, target, animationSpec = spring(dampingRatio = 0.9f, stiffness = 420f)) { v, _ -> hidden.floatValue = v }
    }
}

/** Ritaglia l'intestazione dal basso verso l'alto secondo [state]; [scale] la richiude del tutto (es. mappa a schermo intero). */
internal fun Modifier.collapsingHeader(state: HeaderCollapse, statusPx: Int, scale: () -> Float = { 1f }) = layout { m, c ->
    val p = m.measure(c)
    state.max = (p.height - statusPx).coerceAtLeast(0).toFloat()
    val h = ((p.height - state.hidden.floatValue) * scale()).roundToInt().coerceAtLeast(0)
    layout(p.width, h) { p.placeRelativeWithLayer(0, h - p.height) }
}

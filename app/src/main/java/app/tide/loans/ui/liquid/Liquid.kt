package app.tide.loans.ui.liquid

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.tide.loans.ui.rememberReducedMotion
import kotlin.math.PI
import kotlin.math.sin
import kotlinx.coroutines.delay

private const val TWO_PI = (2 * PI).toFloat()
private const val STEP_PX = 8f

/** Animated water filling [level] of its bounds, from 0 to 1. Rises from [initialLevel] with a wobble. */
@Composable
fun Liquid(
    level: Float,
    color: Color,
    modifier: Modifier = Modifier,
    waveHeight: Dp = 14.dp,
    showBackWave: Boolean = false,
    levelDelayMs: Int = 0,
    initialLevel: Float = 0f,
) {
    val reducedMotion = rememberReducedMotion()
    val shown = remember { Animatable(if (reducedMotion) level else initialLevel) }
    LaunchedEffect(level) {
        if (reducedMotion) {
            shown.snapTo(level)
        } else {
            delay(levelDelayMs.toLong())
            shown.animateTo(level, spring(dampingRatio = 0.45f, stiffness = 60f))
        }
    }
    val tint by animateColorAsState(color, tween(600, levelDelayMs), label = "liquid colour")
    val waves = rememberInfiniteTransition(label = "waves")
    val front by waves.animateFloat(
        0f, TWO_PI, infiniteRepeatable(tween(5000, easing = LinearEasing), RepeatMode.Restart), label = "front wave",
    )
    val back by waves.animateFloat(
        TWO_PI, 0f, infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Restart), label = "back wave",
    )
    Canvas(modifier) {
        val filled = shown.value
        if (filled <= 0.002f) return@Canvas
        val waterline = size.height * (1 - filled)
        val amplitude = waveHeight.toPx() / 2
        if (showBackWave) drawWave(waterline - amplitude, amplitude * 1.3f, back, tint.copy(alpha = 0.45f))
        drawWave(waterline, amplitude, front, tint)
    }
}

private fun DrawScope.drawWave(waterline: Float, amplitude: Float, phase: Float, color: Color) {
    val wavenumber = TWO_PI / (size.width / 2)
    val path = Path().apply {
        moveTo(0f, size.height)
        var x = 0f
        while (x < size.width + STEP_PX) {
            lineTo(x, waterline + amplitude * sin(wavenumber * x + phase))
            x += STEP_PX
        }
        lineTo(size.width, size.height)
        close()
    }
    drawPath(path, color)
}

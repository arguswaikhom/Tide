package app.tide.loans.ui.liquid

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private const val FALL_MS = 620
private const val FADE_FROM = 0.88f

/** A teardrop falling down the middle of the screen to [targetY]. */
@Composable
fun Drop(color: Color, targetY: Dp, onFinished: () -> Unit, startY: Dp = 230.dp) {
    val fall = remember { Animatable(0f) }
    val finished by rememberUpdatedState(onFinished)
    LaunchedEffect(Unit) {
        fall.animateTo(1f, tween(FALL_MS, easing = CubicBezierEasing(0.6f, 0f, 0.9f, 0.6f)))
        finished()
    }
    Canvas(Modifier.fillMaxSize()) {
        val radius = 11.dp.toPx()
        val y = startY.toPx() + (targetY.toPx() - startY.toPx()) * fall.value
        val alpha = if (fall.value < FADE_FROM) 1f else (1 - fall.value) / (1 - FADE_FROM)
        val x = size.width / 2
        drawCircle(color, radius, Offset(x, y), alpha = alpha)
        val tip = Path().apply {
            moveTo(x - radius, y - radius * 0.2f)
            lineTo(x, y - radius * 2.2f)
            lineTo(x + radius, y - radius * 0.2f)
            close()
        }
        drawPath(tip, color, alpha = alpha)
    }
}

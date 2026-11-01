package app.tide.loans.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import app.tide.loans.R

object Tide {
    val Ground = Color(0xFF0A1622)
    val Panel = Color(0xFF12263A)
    val Chip = Color(0xFF1D3850)
    val Text = Color(0xFFEAF6FF)
    val Secondary = Color(0xFFD2E2EE)
    val Muted = Color(0xFFA9C2D6)
    val Blue = Color(0xFF7CCBFF)
    val Orange = Color(0xFFFFAE5C)
}

@OptIn(ExperimentalTextApi::class)
private fun variable(res: Int, weight: FontWeight) = Font(
    res,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

val Unbounded = FontFamily(
    variable(R.font.unbounded, FontWeight.Medium),
    variable(R.font.unbounded, FontWeight.SemiBold),
)

val Figtree = FontFamily(
    variable(R.font.figtree, FontWeight.Normal),
    variable(R.font.figtree, FontWeight.SemiBold),
    variable(R.font.figtree, FontWeight.Bold),
)

private val base = TextStyle(fontFamily = Figtree)

private val typography = Typography(
    bodyLarge = base,
    bodyMedium = base,
    bodySmall = base,
    labelLarge = base,
    labelMedium = base,
    labelSmall = base,
    titleLarge = base,
    titleMedium = base,
    titleSmall = base,
)

@Composable
fun TideTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Tide.Ground,
            surface = Tide.Panel,
            surfaceContainerLow = Tide.Panel,
            surfaceContainerHigh = Tide.Panel,
            surfaceContainerHighest = Tide.Chip,
            primary = Tide.Text,
            onPrimary = Tide.Ground,
            onBackground = Tide.Text,
            onSurface = Tide.Text,
            onSurfaceVariant = Tide.Muted,
            outline = Tide.Muted,
        ),
        typography = typography,
        content = content,
    )
}

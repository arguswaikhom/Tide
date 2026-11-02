package app.tide.loans.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp

/** Font size by character count: [baseSp] up to [fitChars] characters, then shrinking to [minSp]. */
fun amountSp(text: String, baseSp: Int, minSp: Int, fitChars: Int): Int =
    if (text.length <= fitChars) baseSp else (baseSp * fitChars / text.length).coerceAtLeast(minSp)

/** The largest size from [baseSp] down to [minSp] whose measured width fits [maxWidth]. */
fun fitSp(baseSp: Int, minSp: Int, maxWidth: Int, widthAt: (Int) -> Int): Int =
    (baseSp downTo minSp).firstOrNull { widthAt(it) <= maxWidth } ?: minSp

/** One line of text that shrinks to fit its width, then ellipsizes at [minSp]. */
@Composable
fun FitText(text: AnnotatedString, style: TextStyle, baseSp: Int, minSp: Int, modifier: Modifier = Modifier) {
    val measurer = rememberTextMeasurer()
    BoxWithConstraints(modifier) {
        val width = constraints.maxWidth
        val sp = remember(text, width) {
            fitSp(baseSp, minSp, width) { size ->
                measurer.measure(text, style.copy(fontSize = size.sp), maxLines = 1, softWrap = false).size.width
            }
        }
        Text(text, style = style.copy(fontSize = sp.sp), maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis)
    }
}

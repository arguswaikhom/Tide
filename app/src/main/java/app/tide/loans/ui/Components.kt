package app.tide.loans.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.tide.loans.domain.fitSp
import app.tide.loans.ui.theme.Figtree
import app.tide.loans.ui.theme.Tide

@Composable
fun PillButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    Box(
        modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(if (enabled) Tide.Text else Tide.Chip)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            color = if (enabled) Tide.Ground else Tide.Muted,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
        )
    }
}

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

@Composable
fun UnderlineField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = TextStyle(fontFamily = Figtree, fontSize = 15.sp),
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    placeholder: String = "",
    fieldModifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, color = Tide.Muted, fontSize = 13.sp)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = textStyle.copy(color = Tide.Text),
            cursorBrush = SolidColor(Tide.Text),
            keyboardOptions = keyboardOptions,
            modifier = fieldModifier.fillMaxWidth().padding(vertical = 8.dp),
            decorationBox = { field ->
                Box {
                    if (value.isEmpty()) Text(placeholder, style = textStyle, color = Color(0xFF7F97AB))
                    field()
                }
            },
        )
        HorizontalDivider(thickness = 2.dp, color = Tide.Muted)
    }
}

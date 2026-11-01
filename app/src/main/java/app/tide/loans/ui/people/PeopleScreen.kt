package app.tide.loans.ui.people

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.tide.loans.AppState
import app.tide.loans.domain.PersonSummary
import app.tide.loans.domain.amountSp
import app.tide.loans.domain.formatAmount
import app.tide.loans.domain.formatTotals
import app.tide.loans.ui.liquid.Liquid
import app.tide.loans.ui.sheets.PersonSheet
import app.tide.loans.ui.theme.Figtree
import app.tide.loans.ui.theme.Tide
import app.tide.loans.ui.theme.Unbounded

private val JarShape = RoundedCornerShape(28.dp)

fun liquidColor(balanceMinor: Long) = if (balanceMinor >= 0) Tide.Blue else Tide.Orange

@Composable
fun PeopleScreen(
    state: AppState,
    onOpenPerson: (Long) -> Unit,
    onAddPerson: (String) -> Unit,
    onSearch: () -> Unit,
    onSettings: () -> Unit,
    fillFromEmpty: Boolean = true,
    jarModifier: @Composable (Long) -> Modifier = { Modifier },
) {
    var adding by remember { mutableStateOf(false) }
    val grid = rememberLazyGridState()
    Column(Modifier.fillMaxSize().background(Tide.Ground).statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.End) {
            IconButton(onSearch) { Icon(Icons.Default.Search, "Search", tint = Tide.Text) }
            IconButton(onSettings) { Icon(Icons.Default.Settings, "Settings", tint = Tide.Text) }
        }
        Row(
            Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TotalColumn("Owed to you", formatTotals(state.totals.owedToYou), Tide.Blue, Modifier.weight(1f))
            TotalColumn("You owe", formatTotals(state.totals.youOwe), Tide.Orange, Modifier.weight(1f))
        }
        val bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            state = grid,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = bottom + 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            itemsIndexed(state.summaries, key = { _, it -> it.person.id }) { index, summary ->
                Jar(summary, index, fillFromEmpty, jarModifier(summary.person.id)) { onOpenPerson(summary.person.id) }
            }
            item(key = "add") { AddPersonTile { adding = true } }
        }
    }
    if (adding) {
        PersonSheet(
            initialName = "",
            onSave = { name, _ ->
                onAddPerson(name)
                adding = false
            },
            onDismiss = { adding = false },
        )
    }
}

@Composable
private fun TotalColumn(label: String, text: String, color: Color, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, color = Tide.Muted, fontSize = 13.sp)
        Text(
            text,
            color = color,
            fontFamily = Unbounded,
            fontWeight = FontWeight.SemiBold,
            fontSize = amountSp(text, 22, 13, 9).sp,
            lineHeight = (amountSp(text, 22, 13, 9) + 6).sp,
        )
    }
}

@Composable
private fun Jar(summary: PersonSummary, index: Int, fillFromEmpty: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val amount = formatAmount(summary.balanceMinor)
    val tag = when {
        summary.balanceMinor > 0 -> "owes you"
        summary.balanceMinor < 0 -> "you owe"
        else -> "all square"
    }
    Box(
        modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(JarShape)
            .background(Tide.Panel)
            .clickable(onClick = onClick),
    ) {
        Liquid(
            level = summary.jarLevel,
            color = liquidColor(summary.balanceMinor),
            modifier = Modifier.fillMaxSize(),
            waveHeight = 12.dp,
            levelDelayMs = if (fillFromEmpty) index * 90 else 0,
            initialLevel = if (fillFromEmpty) 0f else summary.jarLevel,
        )
        Column(
            Modifier.padding(start = 16.dp, end = 12.dp, top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                summary.person.name,
                color = Tide.Text,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                buildAnnotatedString {
                    append(amount)
                    withStyle(SpanStyle(fontFamily = Figtree, fontSize = 12.sp, color = Tide.Muted)) {
                        append(" " + summary.currency)
                    }
                },
                color = Tide.Text,
                fontFamily = Unbounded,
                fontWeight = FontWeight.Medium,
                fontSize = amountSp(amount, 20, 11, 8).sp,
                maxLines = 1,
                softWrap = false,
            )
            Text(tag, color = Tide.Muted, fontSize = 13.sp)
        }
    }
}

@Composable
private fun AddPersonTile(onClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(210.dp)
            .clip(JarShape)
            .drawBehind {
                drawRoundRect(
                    color = Tide.Muted,
                    cornerRadius = CornerRadius(28.dp.toPx()),
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10.dp.toPx(), 8.dp.toPx())),
                    ),
                )
            }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.Add, null, tint = Tide.Text, modifier = Modifier.size(28.dp))
            Text("Add person", color = Tide.Text, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
    }
}

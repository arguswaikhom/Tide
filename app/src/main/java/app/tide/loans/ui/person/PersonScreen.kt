package app.tide.loans.ui.person

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.tide.loans.data.Direction
import app.tide.loans.data.Entry
import app.tide.loans.domain.PersonSummary
import app.tide.loans.domain.amountSp
import app.tide.loans.domain.formatAmount
import app.tide.loans.domain.formatWhen
import app.tide.loans.domain.historyOrder
import app.tide.loans.domain.personLevel
import app.tide.loans.ui.PillButton
import app.tide.loans.ui.liquid.Liquid
import app.tide.loans.ui.people.liquidColor
import app.tide.loans.ui.sheets.ConfirmDialog
import app.tide.loans.ui.sheets.EntrySheet
import app.tide.loans.ui.sheets.PersonSheet
import app.tide.loans.ui.theme.Figtree
import app.tide.loans.ui.theme.Tide
import app.tide.loans.ui.theme.Unbounded
import java.time.ZoneId

private sealed interface Modal {
    data object AddEntry : Modal
    data class EditEntry(val entry: Entry) : Modal
    data class ConfirmDeleteEntry(val entry: Entry) : Modal
    data object EditPerson : Modal
    data object ConfirmDeletePerson : Modal
}

@Composable
fun PersonScreen(
    summary: PersonSummary,
    entries: List<Entry>,
    defaultCurrency: String,
    onBack: () -> Unit,
    onSaveEntry: (id: Long?, Direction, amountMinor: Long, note: String?, occurredAt: Long) -> Unit,
    onDeleteEntry: (Long) -> Unit,
    onSavePerson: (name: String, currencyOverride: String?) -> Unit,
    onDeletePerson: () -> Unit,
) {
    var modal by remember { mutableStateOf<Modal?>(null) }
    val name = summary.person.name

    Box(Modifier.fillMaxSize().background(Tide.Ground)) {
        PersonLiquid(summary.balanceMinor)
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back to people", tint = Tide.Text)
                }
                Text(
                    name,
                    Modifier.weight(1f),
                    color = Tide.Text,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
                IconButton({ modal = Modal.EditPerson }) {
                    Icon(Icons.Default.Edit, "Edit $name", tint = Tide.Text)
                }
            }
            Balance(summary)
            History(entries, summary.currency, Modifier.weight(1f)) { modal = Modal.EditEntry(it) }
            PillButton(
                "Add entry",
                Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
            ) { modal = Modal.AddEntry }
        }
    }

    when (val current = modal) {
        null -> Unit
        Modal.AddEntry -> EntrySheet(
            existing = null,
            onSave = { direction, amount, note, at ->
                onSaveEntry(null, direction, amount, note, at)
                modal = null
            },
            onDelete = null,
            onDismiss = { modal = null },
        )
        is Modal.EditEntry -> EntrySheet(
            existing = current.entry,
            onSave = { direction, amount, note, at ->
                onSaveEntry(current.entry.id, direction, amount, note, at)
                modal = null
            },
            onDelete = { modal = Modal.ConfirmDeleteEntry(current.entry) },
            onDismiss = { modal = null },
        )
        is Modal.ConfirmDeleteEntry -> ConfirmDialog(
            title = "Delete this entry?",
            body = "It will be removed from $name's history and the balance will change.",
            confirmLabel = "Delete",
            onConfirm = {
                onDeleteEntry(current.entry.id)
                modal = null
            },
            onDismiss = { modal = null },
        )
        Modal.EditPerson -> PersonSheet(
            initialName = name,
            onSave = {
                onSavePerson(it, summary.person.currencyOverride)
                modal = null
            },
            onDismiss = { modal = null },
            onDelete = { modal = Modal.ConfirmDeletePerson },
        )
        Modal.ConfirmDeletePerson -> ConfirmDialog(
            title = "Delete $name?",
            body = "This erases $name and their whole history. You can't undo this.",
            confirmLabel = "Delete",
            onConfirm = {
                modal = null
                onDeletePerson()
            },
            onDismiss = { modal = null },
        )
    }
}

@Composable
private fun PersonLiquid(balanceMinor: Long) {
    Liquid(
        level = personLevel(balanceMinor),
        color = liquidColor(balanceMinor),
        modifier = Modifier.fillMaxSize(),
        waveHeight = 16.dp,
        showBackWave = true,
    )
}

@Composable
private fun Balance(summary: PersonSummary) {
    val name = summary.person.name
    val balance = summary.balanceMinor
    val amount = formatAmount(balance)
    Column(
        Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            buildAnnotatedString {
                append(amount)
                withStyle(SpanStyle(fontFamily = Figtree, fontSize = 15.sp, color = Tide.Muted)) {
                    append(" " + summary.currency)
                }
            },
            color = if (balance == 0L) Tide.Text else liquidColor(balance),
            fontFamily = Unbounded,
            fontWeight = FontWeight.SemiBold,
            fontSize = amountSp(amount, 52, 24, 7).sp,
            maxLines = 1,
            softWrap = false,
        )
        Text(
            when {
                balance > 0 -> "$name owes you"
                balance < 0 -> "You owe $name"
                else -> "All square"
            },
            color = Tide.Muted,
            fontSize = 15.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun History(entries: List<Entry>, currency: String, modifier: Modifier, onTap: (Entry) -> Unit) {
    val now = System.currentTimeMillis()
    val zone = ZoneId.systemDefault()
    LazyColumn(
        modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(historyOrder(entries), key = { it.id }) { entry ->
            EntryCard(entry, currency, formatWhen(entry.occurredAt, now, zone)) { onTap(entry) }
        }
    }
}

@Composable
fun EntryCard(entry: Entry, currency: String, whenText: String, personName: String? = null, onClick: () -> Unit) {
    val gave = entry.direction == Direction.GAVE
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Tide.Panel)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            if (personName != null) {
                Text(personName, color = Tide.Text, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Text(
                if (gave) "You gave" else "You got",
                color = Tide.Text,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
            )
            entry.note?.let { Text(it, color = Tide.Secondary, fontSize = 14.sp, lineHeight = 19.sp) }
            Text(whenText, color = Tide.Muted, fontSize = 13.sp)
        }
        Text(
            buildAnnotatedString {
                append(formatAmount(entry.amountMinor))
                withStyle(SpanStyle(fontFamily = Figtree, fontSize = 11.sp, color = Tide.Muted)) {
                    append(" $currency")
                }
            },
            color = if (gave) Tide.Blue else Tide.Orange,
            fontFamily = Unbounded,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
        )
    }
}

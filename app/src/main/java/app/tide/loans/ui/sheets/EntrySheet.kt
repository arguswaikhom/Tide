package app.tide.loans.ui.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.tide.loans.data.Direction
import app.tide.loans.data.Entry
import app.tide.loans.domain.formatAmount
import app.tide.loans.domain.formatWhen
import app.tide.loans.domain.normalizeNote
import app.tide.loans.domain.parseAmount
import app.tide.loans.domain.resolveOccurredAt
import app.tide.loans.ui.PillButton
import app.tide.loans.ui.UnderlineField
import app.tide.loans.ui.theme.Tide
import app.tide.loans.ui.theme.Unbounded
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntrySheet(
    existing: Entry?,
    onSave: (Direction, Long, String?, Long) -> Unit,
    onDelete: (() -> Unit)?,
    onDismiss: () -> Unit,
) {
    var direction by rememberSaveable { mutableStateOf(existing?.direction ?: Direction.GAVE) }
    var amount by rememberSaveable {
        mutableStateOf(existing?.let { formatAmount(it.amountMinor).replace(",", "") } ?: "")
    }
    var note by rememberSaveable { mutableStateOf(existing?.note ?: "") }
    var picked by rememberSaveable { mutableStateOf<Long?>(null) }
    var pickingTime by rememberSaveable { mutableStateOf(false) }
    val parsed = parseAmount(amount)
    val amountFocus = remember { FocusRequester() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Tide.Panel,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            Modifier.imePadding().padding(start = 18.dp, end = 18.dp, bottom = 26.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DirectionChip("You gave", direction == Direction.GAVE, Tide.Blue, Modifier.weight(1f)) {
                    direction = Direction.GAVE
                }
                DirectionChip("You got", direction == Direction.GOT, Tide.Orange, Modifier.weight(1f)) {
                    direction = Direction.GOT
                }
            }
            UnderlineField(
                label = "Amount",
                value = amount,
                onValueChange = { amount = it },
                textStyle = TextStyle(fontFamily = Unbounded, fontWeight = FontWeight.SemiBold, fontSize = 30.sp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                placeholder = "0",
                fieldModifier = Modifier.focusRequester(amountFocus),
            )
            UnderlineField(
                label = "Note (optional)",
                value = note,
                onValueChange = { note = it },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                placeholder = "What was it for?",
            )
            Column(
                Modifier.fillMaxWidth().clickable { pickingTime = true },
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text("When", color = Tide.Muted, fontSize = 13.sp)
                val shown = picked ?: existing?.occurredAt
                Text(
                    if (shown == null) "Now" else formatWhen(shown, System.currentTimeMillis(), ZoneId.systemDefault()),
                    color = Tide.Text,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
                HorizontalDivider(thickness = 2.dp, color = Tide.Muted)
            }
            PillButton("Save", enabled = parsed != null) {
                if (parsed != null) {
                    val at = resolveOccurredAt(existing?.occurredAt, picked, System.currentTimeMillis())
                    onSave(direction, parsed, normalizeNote(note), at)
                }
            }
            if (onDelete != null) {
                TextButton(onDelete, Modifier.fillMaxWidth()) {
                    Text("Delete entry", color = Tide.Orange, fontWeight = FontWeight.Bold)
                }
            }
            if (existing == null) LaunchedEffect(Unit) { amountFocus.requestFocus() }
        }
    }

    if (pickingTime) {
        DateTimeDialogs(
            initial = picked ?: existing?.occurredAt ?: System.currentTimeMillis(),
            onPicked = {
                picked = it
                pickingTime = false
            },
            onDismiss = { pickingTime = false },
        )
    }
}

@Composable
private fun DirectionChip(text: String, selected: Boolean, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .height(50.dp)
            .clip(RoundedCornerShape(25.dp))
            .background(if (selected) color else Tide.Chip)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = if (selected) Tide.Ground else Tide.Text, fontWeight = FontWeight.Bold)
    }
}

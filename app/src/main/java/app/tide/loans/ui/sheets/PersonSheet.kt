package app.tide.loans.ui.sheets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.tide.loans.ui.PillButton
import app.tide.loans.ui.UnderlineField
import app.tide.loans.ui.settings.CurrencyPicker
import app.tide.loans.ui.theme.Tide
import app.tide.loans.ui.theme.Unbounded

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonSheet(
    initialName: String,
    onSave: (name: String, currencyOverride: String?) -> Unit,
    onDismiss: () -> Unit,
    onDelete: (() -> Unit)? = null,
    initialCurrency: String? = null,
    defaultCurrency: String? = null,
) {
    var name by rememberSaveable { mutableStateOf(initialName) }
    var currency by rememberSaveable { mutableStateOf(initialCurrency) }
    var pickingCurrency by rememberSaveable { mutableStateOf(false) }
    var confirmingDelete by rememberSaveable { mutableStateOf(false) }
    val focus = remember { FocusRequester() }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Tide.Panel,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            Modifier.imePadding().padding(start = 18.dp, end = 18.dp, bottom = 26.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            UnderlineField(
                label = "Name",
                value = name,
                onValueChange = { name = it },
                textStyle = TextStyle(fontFamily = Unbounded, fontWeight = FontWeight.SemiBold, fontSize = 22.sp),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                fieldModifier = Modifier.focusRequester(focus),
            )
            if (defaultCurrency != null) {
                Column(
                    Modifier.fillMaxWidth().clickable { pickingCurrency = true },
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text("Currency", color = Tide.Muted, fontSize = 13.sp)
                    Text(
                        currency ?: "Default ($defaultCurrency)",
                        color = Tide.Text,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                    HorizontalDivider(thickness = 2.dp, color = Tide.Muted)
                }
            }
            PillButton("Save", enabled = name.isNotBlank()) { onSave(name, currency) }
            if (onDelete != null) {
                TextButton({ confirmingDelete = true }, Modifier.fillMaxWidth()) {
                    Text("Delete person", color = Tide.Orange, fontWeight = FontWeight.Bold)
                }
            }
            if (initialName.isEmpty()) LaunchedEffect(Unit) { focus.requestFocus() }
        }
    }
    if (confirmingDelete && onDelete != null) {
        ConfirmDialog(
            title = "Delete $initialName?",
            body = "This erases $initialName and their whole history. You can't undo this.",
            confirmLabel = "Delete",
            onConfirm = {
                confirmingDelete = false
                onDelete()
            },
            onDismiss = { confirmingDelete = false },
        )
    }
    if (pickingCurrency && defaultCurrency != null) {
        CurrencyPicker(
            selected = currency,
            allowDefault = true,
            defaultCurrency = defaultCurrency,
            onPick = {
                currency = it
                pickingCurrency = false
            },
            onDismiss = { pickingCurrency = false },
        )
    }
}

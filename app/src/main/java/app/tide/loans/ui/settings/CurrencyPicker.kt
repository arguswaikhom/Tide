package app.tide.loans.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.tide.loans.domain.currencyOptions
import app.tide.loans.ui.UnderlineField
import app.tide.loans.ui.theme.Tide
import app.tide.loans.ui.theme.Unbounded

/** Passes the chosen code, or null for "Use default" when [allowDefault] is true. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyPicker(
    selected: String?,
    allowDefault: Boolean,
    defaultCurrency: String,
    onPick: (String?) -> Unit,
    onDismiss: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val options = remember(query) { currencyOptions(query.trim()) }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Tide.Panel,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(Modifier.imePadding().padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            UnderlineField(
                label = "Currency",
                value = query,
                onValueChange = { query = it },
                placeholder = "Search by code or name",
            )
            LazyColumn(Modifier.fillMaxWidth().height(380.dp)) {
                if (allowDefault && query.isBlank()) {
                    item {
                        CurrencyRow("Use default ($defaultCurrency)", null, selected == null) { onPick(null) }
                    }
                }
                items(options, key = { it.code }) {
                    CurrencyRow(it.code, it.name, it.code == selected) { onPick(it.code) }
                }
            }
        }
    }
}

@Composable
private fun CurrencyRow(title: String, subtitle: String?, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            color = Tide.Text,
            fontFamily = if (subtitle != null) Unbounded else null,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
        )
        Text(
            subtitle ?: "",
            Modifier.weight(1f),
            color = Tide.Muted,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (selected) Icon(Icons.Default.Check, "Selected", tint = Tide.Blue)
    }
}

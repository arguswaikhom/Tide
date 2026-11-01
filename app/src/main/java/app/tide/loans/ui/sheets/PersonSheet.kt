package app.tide.loans.ui.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
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
import app.tide.loans.ui.theme.Tide
import app.tide.loans.ui.theme.Unbounded

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonSheet(initialName: String, onSave: (String) -> Unit, onDismiss: () -> Unit) {
    var name by rememberSaveable { mutableStateOf(initialName) }
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
            PillButton("Save", enabled = name.isNotBlank()) { onSave(name) }
            LaunchedEffect(Unit) { focus.requestFocus() }
        }
    }
}

package app.tide.loans.ui.sheets

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import app.tide.loans.ui.theme.Tide

@Composable
fun ConfirmDialog(title: String, body: String, confirmLabel: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Tide.Panel,
        titleContentColor = Tide.Text,
        textContentColor = Tide.Secondary,
        title = { Text(title, fontWeight = FontWeight.SemiBold) },
        text = { Text(body) },
        confirmButton = {
            TextButton(onConfirm) { Text(confirmLabel, color = Tide.Orange, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onDismiss) { Text("Cancel", color = Tide.Text) }
        },
    )
}

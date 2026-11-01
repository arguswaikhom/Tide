package app.tide.loans.ui.sheets

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.tide.loans.ui.theme.Tide
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

/** Asks for a date, then a time, in the phone's time zone. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimeDialogs(initial: Long, onPicked: (Long) -> Unit, onDismiss: () -> Unit) {
    val zone = ZoneId.systemDefault()
    val start = Instant.ofEpochMilli(initial).atZone(zone)
    // The date picker works in UTC days, whatever the phone's zone is.
    val dateState = rememberDatePickerState(
        initialSelectedDateMillis = start.toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
    )
    val timeState = rememberTimePickerState(start.hour, start.minute)
    var pickedDate by remember { mutableStateOf<LocalDate?>(null) }

    val date = pickedDate
    if (date == null) {
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = {
                    pickedDate = dateState.selectedDateMillis
                        ?.let { Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate() }
                        ?: start.toLocalDate()
                }) { Text("Next", color = Tide.Text) }
            },
            dismissButton = { TextButton(onDismiss) { Text("Cancel", color = Tide.Text) } },
        ) {
            DatePicker(dateState)
        }
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = Tide.Panel,
            text = { TimePicker(timeState) },
            confirmButton = {
                TextButton(onClick = {
                    val time = LocalTime.of(timeState.hour, timeState.minute)
                    onPicked(date.atTime(time).atZone(zone).toInstant().toEpochMilli())
                }) { Text("Done", color = Tide.Text) }
            },
            dismissButton = { TextButton(onDismiss) { Text("Cancel", color = Tide.Text) } },
        )
    }
}

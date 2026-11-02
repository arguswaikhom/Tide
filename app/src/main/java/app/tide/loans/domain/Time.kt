package app.tide.loans.domain

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val TIME = DateTimeFormatter.ofPattern("h:mm a", Locale.US)
private val DAY = DateTimeFormatter.ofPattern("MMM d, h:mm a", Locale.US)
private val DAY_AND_YEAR = DateTimeFormatter.ofPattern("MMM d, yyyy, h:mm a", Locale.US)

/** A changed time always wins. Otherwise an edit keeps its time and a new entry takes the moment of saving. */
fun resolveOccurredAt(original: Long?, picked: Long?, now: Long): Long = picked ?: original ?: now

fun formatWhen(ms: Long, now: Long, zone: ZoneId): String {
    val time = Instant.ofEpochMilli(ms).atZone(zone)
    val today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
    return when {
        time.toLocalDate() == today -> "Today, " + TIME.format(time)
        time.toLocalDate() == today.minusDays(1) -> "Yesterday, " + TIME.format(time)
        time.year == today.year -> DAY.format(time)
        else -> DAY_AND_YEAR.format(time)
    }
}

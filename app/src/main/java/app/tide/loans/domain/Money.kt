package app.tide.loans.domain

import java.util.Currency
import java.util.Locale
import kotlin.math.abs

/** 1 trillion, in minor units. Keeps sums far from overflowing a Long. */
const val MAX_AMOUNT_MINOR = 100_000_000_000_000L

private val AMOUNT = Regex("""(\d*)(?:\.(\d{0,2}))?""")
private const val MAX_WHOLE_DIGITS = 13

/** Returns minor units, or null if the text isn't a positive amount within range. */
fun parseAmount(text: String): Long? {
    val match = AMOUNT.matchEntire(text.trim().replace(',', '.')) ?: return null
    val (whole, fraction) = match.destructured
    if (whole.isEmpty() && fraction.isEmpty()) return null
    if (whole.length > MAX_WHOLE_DIGITS) return null
    val minor = whole.ifEmpty { "0" }.toLong() * 100 + fraction.padEnd(2, '0').toLong()
    return minor.takeIf { it in 1..MAX_AMOUNT_MINOR }
}

/** Absolute value, grouped, with decimals only when they aren't zero. */
fun formatAmount(minor: Long): String {
    val value = abs(minor)
    val whole = String.format(Locale.US, "%,d", value / 100)
    val fraction = value % 100
    return if (fraction == 0L) whole else whole + String.format(Locale.US, ".%02d", fraction)
}

fun formatTotals(byCurrency: Map<String, Long>): String =
    if (byCurrency.isEmpty()) "0"
    else byCurrency.toSortedMap().entries.joinToString(" · ") { "${formatAmount(it.value)} ${it.key}" }

fun normalizeNote(text: String): String? = text.trim().ifEmpty { null }

data class CurrencyOption(val code: String, val name: String)

fun currencyOptions(query: String): List<CurrencyOption> =
    Currency.getAvailableCurrencies()
        .map { CurrencyOption(it.currencyCode, it.getDisplayName(Locale.ENGLISH)) }
        .filter { it.code.contains(query, ignoreCase = true) || it.name.contains(query, ignoreCase = true) }
        .sortedBy { it.code }

package app.tide.loans.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Locale

class TimeTest {
    private fun ms(text: String) = LocalDateTime.parse(text).toInstant(ZoneOffset.UTC).toEpochMilli()
    private val now = ms("2026-11-01T18:00:00")

    @Test fun resolvesWhenAnEntryHappened() {
        assertEquals(9000L, resolveOccurredAt(original = null, picked = null, now = 9000L))
        assertEquals(5000L, resolveOccurredAt(original = null, picked = 5000L, now = 9000L))
        assertEquals(3000L, resolveOccurredAt(original = 3000L, picked = null, now = 9000L))
        assertEquals(5000L, resolveOccurredAt(original = 3000L, picked = 5000L, now = 9000L))
    }

    @Test fun formatsToday() {
        assertEquals("Today, 9:41 AM", formatWhen(ms("2026-11-01T09:41:00"), now, ZoneOffset.UTC))
    }

    @Test fun formatsYesterday() {
        assertEquals("Yesterday, 9:41 AM", formatWhen(ms("2026-10-31T09:41:00"), now, ZoneOffset.UTC))
    }

    @Test fun formatsOlderDateThisYear() {
        assertEquals("Oct 28, 9:41 AM", formatWhen(ms("2026-10-28T09:41:00"), now, ZoneOffset.UTC))
    }

    @Test fun formatsAnotherYear() {
        assertEquals("Oct 28, 2025, 9:41 AM", formatWhen(ms("2025-10-28T09:41:00"), now, ZoneOffset.UTC))
    }

    @Test fun picksCurrencyFromRegionWithFallback() {
        assertEquals("USD", defaultCurrencyFor(Locale.US))
        assertEquals("INR", defaultCurrencyFor(Locale("en", "IN")))
        assertEquals("USD", defaultCurrencyFor(Locale("en")))
        assertEquals("USD", defaultCurrencyFor(Locale("en", "AQ")))
    }
}

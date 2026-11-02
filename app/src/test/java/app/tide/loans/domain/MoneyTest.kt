package app.tide.loans.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class MoneyTest {
    @Test fun parsesWholeAndDecimalAmounts() {
        assertEquals(150000L, parseAmount("1500"))
        assertEquals(150050L, parseAmount("1500.50"))
        assertEquals(150050L, parseAmount("1500,50"))
        assertEquals(50L, parseAmount(".5"))
        assertEquals(150000L, parseAmount(" 1500 "))
    }

    @Test fun rejectsInvalidAmounts() {
        listOf("", ".", "0", "0.00", "12.345", "abc", "-5", "1,500", "1.2.3", "99999999999999999999")
            .forEach { assertNull(it, parseAmount(it)) }
    }

    @Test fun acceptsTheMaximumAndRejectsAboveIt() {
        assertEquals(MAX_AMOUNT_MINOR, parseAmount("1000000000000"))
        assertNull(parseAmount("1000000000000.01"))
    }

    @Test fun formatsAbsoluteGroupedAmounts() {
        assertEquals("1,500", formatAmount(150000))
        assertEquals("1,500.50", formatAmount(-150050))
        assertEquals("0.05", formatAmount(5))
    }

    @Test fun formatsTotalsPerCurrency() {
        assertEquals("2,150 INR · 300 USD", formatTotals(mapOf("USD" to 30000L, "INR" to 215000L)))
        assertEquals("0", formatTotals(emptyMap()))
    }

    @Test fun normalizesNotes() {
        assertNull(normalizeNote("   "))
        assertEquals("Cash", normalizeNote(" Cash "))
    }

    @Test fun listsAndFiltersCurrencies() {
        assertTrue(currencyOptions("").any { it.code == "INR" })
        assertEquals("USD", currencyOptions("usd").first().code)
        assertTrue(currencyOptions("rupee").any { it.code == "INR" })
        assertTrue(currencyOptions("zzzz").isEmpty())
    }

    @Test fun picksCurrencyFromRegionWithFallback() {
        assertEquals("USD", defaultCurrencyFor(Locale.US))
        assertEquals("INR", defaultCurrencyFor(Locale("en", "IN")))
        assertEquals("USD", defaultCurrencyFor(Locale("en")))
        assertEquals("USD", defaultCurrencyFor(Locale("en", "AQ")))
    }
}

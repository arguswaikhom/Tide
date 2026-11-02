package app.tide.loans.domain

import app.tide.loans.data.Direction
import app.tide.loans.data.Entry
import app.tide.loans.data.Person
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchTest {
    private val maya = Person(id = 1, name = "Maya", createdAt = 0)
    private val sam = Person(id = 2, name = "Sam", currencyOverride = "USD", createdAt = 0)
    private val people = listOf(maya, sam)

    private fun entry(id: Long, amountMinor: Long, note: String? = null, at: Long = id, personId: Long = 1) =
        Entry(id = id, personId = personId, direction = Direction.GAVE, amountMinor = amountMinor, note = note, occurredAt = at)

    private val entries = listOf(
        entry(1, 50000, "Paid back at lunch"),
        entry(2, 150000),
        entry(3, 500000),
        entry(4, 75000, "Bought 500 stamps"),
        entry(5, 50000, personId = 2),
    )

    private fun ids(query: String) = search(query, people, entries, "INR").map { it.entry.id }

    @Test fun blankQueryFindsNothing() {
        assertTrue(ids("").isEmpty())
        assertTrue(ids("   ").isEmpty())
    }

    @Test fun amountMatchesExactly() {
        assertEquals(listOf(5L, 4L, 1L), ids("500"))
    }

    @Test fun amountAcceptsCommasAndDecimals() {
        assertEquals(listOf(2L), ids("1,500"))
        assertEquals(listOf(2L), ids("1500.00"))
    }

    @Test fun noteMatchIgnoresCase() {
        assertEquals(listOf(1L), ids("lunch"))
        assertEquals(listOf(1L), ids("LUNCH"))
    }

    @Test fun amountMatchesAnyCurrencyAndHitCarriesIt() {
        val hit = search("500", people, entries, "INR").first { it.entry.id == 5L }
        assertEquals("USD", hit.currency)
        assertEquals(sam, hit.person)
        assertEquals("INR", search("500", people, entries, "INR").first { it.entry.id == 1L }.currency)
    }

    @Test fun hitsAreNewestFirst() {
        val out = listOf(entry(1, 100, "cab", at = 5), entry(2, 100, "cab", at = 9))
        assertEquals(listOf(2L, 1L), search("cab", people, out, "INR").map { it.entry.id })
    }

    @Test fun commaDecimalMeansTheSameAsWhenEntering() {
        val out = listOf(entry(1, 1250), entry(2, 125000))
        assertEquals(listOf(1L), search("12,50", people, out, "INR").map { it.entry.id })
    }
}

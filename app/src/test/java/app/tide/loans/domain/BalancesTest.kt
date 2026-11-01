package app.tide.loans.domain

import app.tide.loans.data.Direction
import app.tide.loans.data.Entry
import app.tide.loans.data.Person
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private fun gave(amount: Long, at: Long = 0, personId: Long = 1, id: Long = 0) =
    Entry(id = id, personId = personId, direction = Direction.GAVE, amountMinor = amount, occurredAt = at)

private fun got(amount: Long, at: Long = 0, personId: Long = 1, id: Long = 0) =
    Entry(id = id, personId = personId, direction = Direction.GOT, amountMinor = amount, occurredAt = at)

private fun person(id: Long, currency: String? = null, createdAt: Long = 0) =
    Person(id = id, name = "P$id", currencyOverride = currency, createdAt = createdAt)

class BalancesTest {
    @Test fun balanceIsGaveMinusGot() {
        assertEquals(150000L, balanceMinor(listOf(gave(200000), got(50000))))
    }

    @Test fun jarLevelIsRelativeToLargestBalance() {
        assertEquals(0f, jarLevel(0, 400000), 0f)
        assertEquals(0.52f, jarLevel(-400000, 400000), 0.001f)
        assertEquals(0.31f, jarLevel(200000, 400000), 0.001f)
    }

    @Test fun personLevelUsesLogScale() {
        assertEquals(0f, personLevel(0), 0f)
        assertEquals(0.24f, personLevel(100), 0.001f)
        assertEquals(0.24f, personLevel(50), 0.001f)
        assertEquals(0.62f, personLevel(100_000_000), 0.001f)
        assertEquals(0.62f, personLevel(500_000_000), 0.001f)
        assertTrue(personLevel(600000) > personLevel(400000))
        assertEquals(personLevel(300000), personLevel(-300000), 0f)
    }

    @Test fun personWithoutOverrideFollowsDefaultAndKeepsBalance() {
        val people = listOf(person(1))
        val entries = listOf(gave(50000))
        val inr = summarize(people, entries, "INR").single()
        val usd = summarize(people, entries, "USD").single()
        assertEquals("INR", inr.currency)
        assertEquals("USD", usd.currency)
        assertEquals(inr.balanceMinor, usd.balanceMinor)
    }

    @Test fun overrideWinsOverDefault() {
        assertEquals("EUR", summarize(listOf(person(1, "EUR")), emptyList(), "INR").single().currency)
    }

    @Test fun changingOverrideMovesPersonToThatCurrencyWithSameBalance() {
        val entries = listOf(gave(50000))
        val before = summarize(listOf(person(1)), entries, "INR").single()
        val after = summarize(listOf(person(1, "USD")), entries, "INR").single()
        assertEquals("USD", after.currency)
        assertEquals(before.balanceMinor, after.balanceMinor)
    }

    @Test fun jarLevelsCompareOnlyWithinACurrency() {
        val people = listOf(person(1, "INR"), person(2, "USD"))
        val entries = listOf(got(400000, personId = 1), got(30000, personId = 2))
        summarize(people, entries, "INR").forEach { assertEquals(0.52f, it.jarLevel, 0.001f) }
    }

    @Test fun jarLevelScalesAgainstOthersInSameCurrency() {
        val people = listOf(person(1), person(2))
        val entries = listOf(gave(400000, personId = 1), gave(200000, personId = 2))
        val byId = summarize(people, entries, "INR").associateBy { it.person.id }
        assertEquals(0.52f, byId.getValue(1).jarLevel, 0.001f)
        assertEquals(0.31f, byId.getValue(2).jarLevel, 0.001f)
    }

    @Test fun ordersByMostRecentActivity() {
        val people = listOf(person(1, createdAt = 10), person(2, createdAt = 20))
        val entries = listOf(gave(100, at = 30, personId = 1), gave(100, at = 25, personId = 2))
        val summaries = summarize(people, entries, "INR")
        assertEquals(listOf(1L, 2L), summaries.map { it.person.id })
        assertEquals(30L, summaries.first().lastActivity)
    }

    @Test fun newPersonSortsAboveOlderEntries() {
        val people = listOf(person(1, createdAt = 10), person(2, createdAt = 100))
        val entries = listOf(gave(100, at = 50, personId = 1))
        assertEquals(listOf(2L, 1L), summarize(people, entries, "INR").map { it.person.id })
    }

    @Test fun backDatingAnEntryChangesTheOrder() {
        val people = listOf(person(1), person(2))
        val other = gave(100, at = 40, personId = 2)
        assertEquals(1L, summarize(people, listOf(gave(100, at = 50, personId = 1), other), "INR").first().person.id)
        assertEquals(2L, summarize(people, listOf(gave(100, at = 30, personId = 1), other), "INR").first().person.id)
    }

    @Test fun personWithNoEntriesStaysWithZeroLevel() {
        val summary = summarize(listOf(person(1)), emptyList(), "INR").single()
        assertEquals(0L, summary.balanceMinor)
        assertEquals(0f, summary.jarLevel, 0f)
    }

    @Test fun totalsGroupByCurrencyAndDirection() {
        val people = listOf(person(1, "INR"), person(2, "INR"), person(3, "USD"), person(4, "EUR"))
        val entries = listOf(
            gave(100, personId = 1), gave(200, personId = 2),
            got(300, personId = 3), gave(50, personId = 4), got(50, personId = 4),
        )
        val result = totals(summarize(people, entries, "INR"))
        assertEquals(mapOf("INR" to 300L), result.owedToYou)
        assertEquals(mapOf("USD" to 300L), result.youOwe)
        assertFalse(result.owedToYou.containsKey("EUR"))
        assertFalse(result.youOwe.containsKey("EUR"))
    }

    @Test fun historyIsNewestFirstWithTiesToHighestId() {
        val entries = listOf(gave(1, at = 10, id = 1), gave(1, at = 30, id = 2), gave(1, at = 30, id = 3))
        assertEquals(listOf(3L, 2L, 1L), historyOrder(entries).map { it.id })
    }

    @Test fun amountShrinksToFit() {
        assertEquals(52, amountSp("1,500", 52, 24, 7))
        assertEquals(26, amountSp("12,345,678,901", 52, 24, 7))
        assertEquals(24, amountSp("1,000,000,000,000", 52, 24, 7))
    }
}

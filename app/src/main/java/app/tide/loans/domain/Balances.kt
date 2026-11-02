package app.tide.loans.domain

import app.tide.loans.data.Direction
import app.tide.loans.data.Entry
import app.tide.loans.data.Person
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.max

data class PersonSummary(
    val person: Person,
    val currency: String,
    val balanceMinor: Long,
    val jarLevel: Float,
    val lastActivity: Long,
)

data class Totals(val owedToYou: Map<String, Long>, val youOwe: Map<String, Long>)

fun balanceMinor(entries: List<Entry>): Long =
    entries.sumOf { if (it.direction == Direction.GAVE) it.amountMinor else -it.amountMinor }

fun effectiveCurrency(person: Person, defaultCurrency: String): String =
    person.currencyOverride ?: defaultCurrency

fun jarLevel(balanceMinor: Long, maxAbsInCurrency: Long): Float {
    if (balanceMinor == 0L) return 0f
    return 0.10f + 0.42f * (abs(balanceMinor).toFloat() / maxAbsInCurrency)
}

/** Depends on this balance alone, so the liquid moves whenever the balance changes. */
fun personLevel(balanceMinor: Long): Float {
    if (balanceMinor == 0L) return 0f
    val major = abs(balanceMinor) / 100.0
    return (0.24 + 0.38 * (log10(max(1.0, major)) / 6.0).coerceAtMost(1.0)).toFloat()
}

fun summarize(people: List<Person>, entries: List<Entry>, defaultCurrency: String): List<PersonSummary> {
    val byPerson = entries.groupBy { it.personId }
    val balances = people.associate { it.id to balanceMinor(byPerson[it.id].orEmpty()) }
    val largest = people
        .groupBy { effectiveCurrency(it, defaultCurrency) }
        .mapValues { (_, group) -> group.maxOf { abs(balances.getValue(it.id)) } }
    return people.map { person ->
        val currency = effectiveCurrency(person, defaultCurrency)
        val balance = balances.getValue(person.id)
        PersonSummary(
            person = person,
            currency = currency,
            balanceMinor = balance,
            jarLevel = jarLevel(balance, largest.getValue(currency)),
            lastActivity = byPerson[person.id]?.maxOfOrNull { it.occurredAt } ?: person.createdAt,
        )
    }.sortedByDescending { it.lastActivity }
}

fun totals(summaries: List<PersonSummary>): Totals {
    fun sum(side: List<PersonSummary>) =
        side.groupBy { it.currency }.mapValues { (_, group) -> group.sumOf { abs(it.balanceMinor) } }
    return Totals(
        owedToYou = sum(summaries.filter { it.balanceMinor > 0 }),
        youOwe = sum(summaries.filter { it.balanceMinor < 0 }),
    )
}

fun historyOrder(entries: List<Entry>): List<Entry> =
    entries.sortedWith(compareByDescending<Entry> { it.occurredAt }.thenByDescending { it.id })

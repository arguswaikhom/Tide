package app.tide.loans.domain

import app.tide.loans.data.Entry
import app.tide.loans.data.Person

data class SearchHit(val entry: Entry, val person: Person, val currency: String)

fun search(query: String, people: List<Person>, entries: List<Entry>, defaultCurrency: String): List<SearchHit> {
    val text = query.trim()
    if (text.isEmpty()) return emptyList()
    val amount = parseAmount(text.replace(",", ""))
    val peopleById = people.associateBy { it.id }
    return historyOrder(entries)
        .filter { it.amountMinor == amount || it.note?.contains(text, ignoreCase = true) == true }
        .mapNotNull { entry ->
            peopleById[entry.personId]?.let { SearchHit(entry, it, effectiveCurrency(it, defaultCurrency)) }
        }
}

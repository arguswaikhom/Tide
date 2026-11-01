package app.tide.loans.data

enum class Direction { GAVE, GOT }

data class Person(
    val id: Long = 0,
    val name: String,
    val currencyOverride: String? = null,
    val createdAt: Long,
)

data class Entry(
    val id: Long = 0,
    val personId: Long,
    val direction: Direction,
    val amountMinor: Long,
    val note: String? = null,
    val occurredAt: Long,
)

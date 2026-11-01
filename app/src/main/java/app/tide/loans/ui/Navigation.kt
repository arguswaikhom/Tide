package app.tide.loans.ui

import androidx.compose.runtime.saveable.Saver

sealed interface Screen {
    data object People : Screen
    data class Person(val id: Long) : Screen
    data object Search : Screen
    data object Settings : Screen
}

/** Drops screens for people who no longer exist, so deleting someone always lands somewhere valid. */
fun pruneStack(stack: List<Screen>, existingPersonIds: Set<Long>): List<Screen> =
    stack.filter { it !is Screen.Person || it.id in existingPersonIds }

val StackSaver: Saver<List<Screen>, ArrayList<String>> = Saver(
    save = { stack ->
        ArrayList(
            stack.map {
                when (it) {
                    Screen.People -> "people"
                    Screen.Search -> "search"
                    Screen.Settings -> "settings"
                    is Screen.Person -> "person:${it.id}"
                }
            },
        )
    },
    restore = { saved ->
        saved.map {
            when (it) {
                "search" -> Screen.Search
                "settings" -> Screen.Settings
                "people" -> Screen.People
                else -> Screen.Person(it.removePrefix("person:").toLong())
            }
        }
    },
)

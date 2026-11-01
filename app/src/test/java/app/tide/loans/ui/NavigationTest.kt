package app.tide.loans.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class NavigationTest {
    @Test fun removesDeletedPersonFromTheStack() {
        val stack = listOf(Screen.People, Screen.Search, Screen.Person(7))
        assertEquals(listOf(Screen.People, Screen.Search), pruneStack(stack, setOf(1)))
    }

    @Test fun keepsExistingPeople() {
        val stack = listOf(Screen.People, Screen.Search, Screen.Person(7))
        assertEquals(stack, pruneStack(stack, setOf(7)))
    }

    @Test fun fallsBackToThePeopleScreen() {
        assertEquals(listOf(Screen.People), pruneStack(listOf(Screen.People, Screen.Person(7)), emptySet()))
    }
}

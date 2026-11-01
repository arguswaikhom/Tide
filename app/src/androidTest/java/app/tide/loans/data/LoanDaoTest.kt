package app.tide.loans.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoanDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var dao: LoanDao

    @Before fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        dao = db.dao()
    }

    @After fun tearDown() = db.close()

    private suspend fun addPerson() = dao.insert(Person(name = "Maya", createdAt = 1))

    private fun entry(personId: Long, id: Long = 0, amount: Long = 500) = Entry(
        id = id, personId = personId, direction = Direction.GOT, amountMinor = amount, note = "Cash", occurredAt = 2,
    )

    @Test fun savedEntryKeepsItsDirection() = runTest {
        val id = addPerson()
        dao.save(entry(id))
        assertEquals(Direction.GOT, dao.entries().first().single().direction)
    }

    @Test fun deletingAPersonDeletesTheirEntries() = runTest {
        val id = addPerson()
        dao.save(entry(id))
        dao.deletePerson(id)
        assertTrue(dao.people().first().isEmpty())
        assertTrue(dao.entries().first().isEmpty())
    }

    @Test fun settingsAreEmptyUntilSaved() = runTest {
        assertNull(dao.settings().first())
        dao.save(Settings(defaultCurrency = "INR"))
        assertEquals("INR", dao.settings().first()?.defaultCurrency)
    }

    @Test fun savingAnExistingEntryReplacesIt() = runTest {
        val id = addPerson()
        dao.save(entry(id))
        val saved = dao.entries().first().single()
        dao.save(saved.copy(amountMinor = 900))
        val entries = dao.entries().first()
        assertEquals(1, entries.size)
        assertEquals(900L, entries.single().amountMinor)
    }
}

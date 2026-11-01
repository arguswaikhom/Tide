package app.tide.loans

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.tide.loans.data.AppDatabase
import app.tide.loans.data.Settings
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppViewModelTest {
    private lateinit var db: AppDatabase

    @Before fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    }

    @After fun tearDown() = db.close()

    private suspend fun start(locale: Locale) = withContext(Dispatchers.Main) { AppViewModel(db.dao(), locale) }

    @Test fun firstLaunchSavesThePhoneRegionCurrency() = runBlocking {
        start(Locale("en", "IN"))
        val saved = withTimeout(3000) { db.dao().settings().filterNotNull().first() }
        assertEquals("INR", saved.defaultCurrency)
    }

    @Test fun laterLaunchesKeepTheSavedCurrency() = runBlocking {
        db.dao().save(Settings(defaultCurrency = "EUR"))
        start(Locale("en", "IN"))
        delay(300)
        assertEquals("EUR", db.dao().settings().first()?.defaultCurrency)
    }
}

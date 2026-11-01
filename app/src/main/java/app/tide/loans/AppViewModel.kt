package app.tide.loans

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.tide.loans.data.AppDatabase
import app.tide.loans.data.Direction
import app.tide.loans.data.Entry
import app.tide.loans.data.LoanDao
import app.tide.loans.data.Person
import app.tide.loans.data.Settings
import app.tide.loans.domain.PersonSummary
import app.tide.loans.domain.Totals
import app.tide.loans.domain.defaultCurrencyFor
import app.tide.loans.domain.summarize
import app.tide.loans.domain.totals
import java.util.Locale
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AppState(
    val loaded: Boolean = false,
    val people: List<Person> = emptyList(),
    val entries: List<Entry> = emptyList(),
    val summaries: List<PersonSummary> = emptyList(),
    val totals: Totals = Totals(emptyMap(), emptyMap()),
    val defaultCurrency: String = "USD",
)

class AppViewModel(private val dao: LoanDao, locale: Locale) : ViewModel() {
    val state: StateFlow<AppState> =
        combine(dao.people(), dao.entries(), dao.settings()) { people, entries, settings ->
            val currency = settings?.defaultCurrency ?: defaultCurrencyFor(locale)
            val summaries = summarize(people, entries, currency)
            AppState(true, people, entries, summaries, totals(summaries), currency)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, AppState())

    init {
        viewModelScope.launch {
            if (dao.settings().first() == null) dao.save(Settings(defaultCurrency = defaultCurrencyFor(locale)))
        }
    }

    fun addPerson(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch { dao.insert(Person(name = trimmed, createdAt = System.currentTimeMillis())) }
    }

    fun updatePerson(id: Long, name: String, currencyOverride: String?) {
        val trimmed = name.trim()
        val person = state.value.people.find { it.id == id } ?: return
        if (trimmed.isEmpty()) return
        viewModelScope.launch { dao.update(person.copy(name = trimmed, currencyOverride = currencyOverride)) }
    }

    fun deletePerson(id: Long) {
        viewModelScope.launch { dao.deletePerson(id) }
    }

    fun saveEntry(id: Long?, personId: Long, direction: Direction, amountMinor: Long, note: String?, occurredAt: Long) {
        viewModelScope.launch {
            dao.save(Entry(id ?: 0, personId, direction, amountMinor, note, occurredAt))
        }
    }

    fun deleteEntry(id: Long) {
        viewModelScope.launch { dao.deleteEntry(id) }
    }

    fun setDefaultCurrency(code: String) {
        viewModelScope.launch { dao.save(Settings(defaultCurrency = code)) }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AppViewModel(AppDatabase.get(context).dao(), Locale.getDefault()) as T
    }
}

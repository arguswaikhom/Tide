package app.tide.loans.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.tide.loans.data.Entry
import app.tide.loans.data.Person
import app.tide.loans.domain.formatWhen
import app.tide.loans.domain.search
import app.tide.loans.ui.UnderlineField
import app.tide.loans.ui.person.EntryCard
import app.tide.loans.ui.theme.Tide
import java.time.ZoneId

@Composable
fun SearchScreen(
    query: String,
    onQueryChange: (String) -> Unit,
    people: List<Person>,
    entries: List<Entry>,
    defaultCurrency: String,
    onOpenPerson: (Long) -> Unit,
    onBack: () -> Unit,
) {
    val hits = remember(query, people, entries, defaultCurrency) { search(query, people, entries, defaultCurrency) }
    val focus = remember { FocusRequester() }
    val now = System.currentTimeMillis()
    val zone = ZoneId.systemDefault()
    Column(Modifier.fillMaxSize().background(Tide.Ground).statusBarsPadding().navigationBarsPadding().imePadding()) {
        Row(
            Modifier.padding(start = 12.dp, end = 16.dp, top = 4.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IconButton(onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back to people", tint = Tide.Text) }
            UnderlineField(
                label = "Search",
                value = query,
                onValueChange = onQueryChange,
                placeholder = "Amount or note",
                modifier = Modifier.weight(1f),
                fieldModifier = Modifier.focusRequester(focus),
            )
        }
        if (query.isNotBlank() && hits.isEmpty()) {
            Text("Nothing found", color = Tide.Muted, fontSize = 15.sp, modifier = Modifier.padding(20.dp))
        }
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(hits, key = { it.entry.id }) { hit ->
                EntryCard(
                    hit.entry,
                    hit.currency,
                    formatWhen(hit.entry.occurredAt, now, zone),
                    personName = hit.person.name,
                ) { onOpenPerson(hit.person.id) }
            }
        }
    }
    LaunchedEffect(Unit) { if (query.isEmpty()) focus.requestFocus() }
}

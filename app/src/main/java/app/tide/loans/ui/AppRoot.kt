package app.tide.loans.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.tide.loans.AppViewModel
import app.tide.loans.ui.people.PeopleScreen
import app.tide.loans.ui.theme.Tide

@Composable
fun AppRoot(viewModel: AppViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var stack by rememberSaveable(stateSaver = StackSaver) { mutableStateOf(listOf<Screen>(Screen.People)) }
    val push = { screen: Screen -> if (stack.last() != screen) stack = stack + screen }
    val pop = { stack = stack.dropLast(1) }

    LaunchedEffect(state.loaded, state.people) {
        if (state.loaded) stack = pruneStack(stack, state.people.map { it.id }.toSet())
    }
    BackHandler(enabled = stack.size > 1) { pop() }

    if (!state.loaded) {
        Box(Modifier.fillMaxSize().background(Tide.Ground))
        return
    }
    AnimatedContent(stack.last(), label = "screen") { screen ->
        when (screen) {
            Screen.People -> PeopleScreen(
                state = state,
                onOpenPerson = { push(Screen.Person(it)) },
                onAddPerson = viewModel::addPerson,
                onSearch = { push(Screen.Search) },
                onSettings = { push(Screen.Settings) },
            )
            else -> Box(Modifier.fillMaxSize().background(Tide.Ground), Alignment.Center) {
                Text("Coming soon", color = Tide.Text)
            }
        }
    }
}

package app.tide.loans.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.tide.loans.AppViewModel
import app.tide.loans.ui.people.PeopleScreen
import app.tide.loans.ui.person.PersonScreen
import app.tide.loans.ui.theme.Tide
import kotlinx.coroutines.delay

private const val JAR_MS = 450

private fun isSideScreen(screen: Screen) = screen is Screen.Search || screen is Screen.Settings

private fun AnimatedContentTransitionScope<Screen>.screenTransition(): ContentTransform = when {
    isSideScreen(targetState) ->
        (slideInHorizontally(tween(280)) { it / 4 } + fadeIn(tween(280))) togetherWith fadeOut(tween(200))
    isSideScreen(initialState) ->
        fadeIn(tween(200)) togetherWith (slideOutHorizontally(tween(280)) { it / 4 } + fadeOut(tween(280)))
    else -> fadeIn(tween(JAR_MS)) togetherWith fadeOut(tween(JAR_MS))
}

/** The jar and the person screen share these bounds, so one grows into the other. */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.jarBounds(id: Long, scope: AnimatedContentScope, reducedMotion: Boolean): Modifier {
    if (reducedMotion) return Modifier
    val corner by scope.transition.animateDp(label = "jar corner") {
        if (it == EnterExitState.Visible) 0.dp else 28.dp
    }
    return Modifier.sharedBounds(
        rememberSharedContentState("jar-$id"),
        scope,
        boundsTransform = BoundsTransform { _, _ -> tween(JAR_MS, easing = FastOutSlowInEasing) },
        clipInOverlayDuringTransition = OverlayClip(RoundedCornerShape(corner)),
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppRoot(viewModel: AppViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var stack by rememberSaveable(stateSaver = StackSaver) { mutableStateOf(listOf<Screen>(Screen.People)) }
    var jarsHaveFilled by remember { mutableStateOf(false) }
    val reducedMotion = rememberReducedMotion()
    val push = { screen: Screen -> if (stack.last() != screen) stack = stack + screen }
    val pop = { stack = stack.dropLast(1) }

    LaunchedEffect(state.loaded, state.people) {
        if (state.loaded) {
            stack = pruneStack(stack, state.people.map { it.id }.toSet())
            jarsHaveFilled = true
        }
    }
    BackHandler(enabled = stack.size > 1) { pop() }

    if (!state.loaded) {
        Box(Modifier.fillMaxSize().background(Tide.Ground))
        return
    }
    SharedTransitionLayout {
        AnimatedContent(
            targetState = stack.last(),
            transitionSpec = { screenTransition() },
            label = "screen",
        ) { screen ->
            when (screen) {
                Screen.People -> PeopleScreen(
                    state = state,
                    onOpenPerson = { if (stack.last() == Screen.People) push(Screen.Person(it)) },
                    onAddPerson = viewModel::addPerson,
                    onSearch = { push(Screen.Search) },
                    onSettings = { push(Screen.Settings) },
                    fillFromEmpty = !jarsHaveFilled,
                    jarModifier = { jarBounds(it, this@AnimatedContent, reducedMotion) },
                )
                is Screen.Person -> {
                    val summary = state.summaries.find { it.person.id == screen.id }
                    if (summary == null) {
                        Box(Modifier.fillMaxSize().background(Tide.Ground))
                    } else {
                        PersonScreen(
                            summary = summary,
                            entries = state.entries.filter { it.personId == screen.id },
                            defaultCurrency = state.defaultCurrency,
                            onBack = { pop() },
                            onSaveEntry = { id, direction, amount, note, at ->
                                viewModel.saveEntry(id, screen.id, direction, amount, note, at)
                            },
                            onDeleteEntry = viewModel::deleteEntry,
                            onSavePerson = { name, currency -> viewModel.updatePerson(screen.id, name, currency) },
                            onDeletePerson = { viewModel.deletePerson(screen.id) },
                            modifier = jarBounds(screen.id, this@AnimatedContent, reducedMotion),
                        )
                    }
                }
                else -> Box(Modifier.fillMaxSize().background(Tide.Ground), Alignment.Center) {
                    Text("Coming soon", color = Tide.Text)
                }
            }
            var settled by remember { mutableStateOf(reducedMotion) }
            LaunchedEffect(Unit) {
                delay(JAR_MS.toLong())
                settled = true
            }
            if (!settled || transition.isRunning) TapBlocker()
        }
    }
}

/** Stops a quick second tap from landing on whatever the next screen has in that spot. */
@Composable
private fun TapBlocker() {
    Box(Modifier.fillMaxSize().clickable(interactionSource = null, indication = null) {})
}

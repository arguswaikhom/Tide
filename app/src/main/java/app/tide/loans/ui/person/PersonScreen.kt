package app.tide.loans.ui.person

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.tide.loans.data.Direction
import app.tide.loans.data.Entry
import app.tide.loans.domain.PersonSummary
import app.tide.loans.domain.formatAmount
import app.tide.loans.domain.formatWhen
import app.tide.loans.domain.historyOrder
import app.tide.loans.domain.interpolateMinor
import app.tide.loans.domain.personLevel
import app.tide.loans.ui.FitText
import app.tide.loans.ui.PillButton
import app.tide.loans.ui.liquid.Drop
import app.tide.loans.ui.liquid.Liquid
import app.tide.loans.ui.liquid.liquidColor
import app.tide.loans.ui.liquid.settledLiquidColor
import app.tide.loans.ui.rememberReducedMotion
import app.tide.loans.ui.sheets.EntrySheet
import app.tide.loans.ui.sheets.PersonSheet
import app.tide.loans.ui.theme.Figtree
import app.tide.loans.ui.theme.Tide
import app.tide.loans.ui.theme.Unbounded
import java.time.ZoneId
import kotlinx.coroutines.delay

private const val RISE_DELAY_MS = 420
private const val COUNT_MS = 900
private const val ROW_IN_MS = 500

internal sealed interface Modal {
    data object AddEntry : Modal
    data class EditEntry(val entryId: Long) : Modal
    data object EditPerson : Modal
}

internal fun modalToKey(modal: Modal?): String = when (modal) {
    null -> ""
    Modal.AddEntry -> "add"
    Modal.EditPerson -> "person"
    is Modal.EditEntry -> "entry:${modal.entryId}"
}

internal fun modalFromKey(key: String): Modal? = when {
    key == "add" -> Modal.AddEntry
    key == "person" -> Modal.EditPerson
    key.startsWith("entry:") -> Modal.EditEntry(key.removePrefix("entry:").toLong())
    else -> null
}

/** Keeps a half-typed entry open if Android removes the app from memory. */
private val ModalSaver: Saver<Modal?, String> = Saver(save = { modalToKey(it) }, restore = { modalFromKey(it) })

@Composable
fun PersonScreen(
    summary: PersonSummary,
    entries: List<Entry>,
    defaultCurrency: String,
    onBack: () -> Unit,
    onSaveEntry: (id: Long?, Direction, amountMinor: Long, note: String?, occurredAt: Long) -> Unit,
    onDeleteEntry: (Long) -> Unit,
    onSavePerson: (name: String, currencyOverride: String?) -> Unit,
    onDeletePerson: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var modal by rememberSaveable(stateSaver = ModalSaver) { mutableStateOf<Modal?>(null) }
    var dropColor by remember { mutableStateOf<Color?>(null) }
    val reducedMotion = rememberReducedMotion()
    val riseDelayMs = if (dropColor != null) RISE_DELAY_MS else 0
    val shownBalance = rememberCountedBalance(summary.balanceMinor, riseDelayMs, reducedMotion)
    val name = summary.person.name

    BoxWithConstraints(modifier.fillMaxSize().background(Tide.Ground)) {
        val screenHeight = maxHeight
        PersonLiquid(summary.balanceMinor, riseDelayMs, summary.jarLevel)
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back to people", tint = Tide.Text)
                }
                Text(
                    name,
                    Modifier.weight(1f),
                    color = Tide.Text,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
                IconButton({ modal = Modal.EditPerson }) {
                    Icon(Icons.Default.Edit, "Edit $name", tint = Tide.Text)
                }
            }
            Balance(summary, shownBalance)
            History(entries, summary.currency, reducedMotion, Modifier.weight(1f)) { modal = Modal.EditEntry(it.id) }
            PillButton(
                "Add entry",
                Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
            ) { modal = Modal.AddEntry }
        }
        dropColor?.let { color ->
            Drop(
                color = color,
                targetY = screenHeight * (1 - personLevel(summary.balanceMinor)),
                onFinished = { dropColor = null },
            )
        }
    }

    when (val current = modal) {
        null -> Unit
        Modal.AddEntry -> EntrySheet(
            existing = null,
            onSave = { direction, amount, note, at ->
                onSaveEntry(null, direction, amount, note, at)
                modal = null
                if (!reducedMotion) dropColor = if (direction == Direction.GAVE) Tide.Blue else Tide.Orange
            },
            onDelete = null,
            onDismiss = { modal = null },
        )
        is Modal.EditEntry -> entries.find { it.id == current.entryId }?.let { entry ->
            EntrySheet(
                existing = entry,
                onSave = { direction, amount, note, at ->
                    onSaveEntry(entry.id, direction, amount, note, at)
                    modal = null
                },
                onDelete = {
                    onDeleteEntry(entry.id)
                    modal = null
                },
                onDismiss = { modal = null },
                personName = name,
            )
        }
        Modal.EditPerson -> PersonSheet(
            initialName = name,
            onSave = { newName, currency ->
                onSavePerson(newName, currency)
                modal = null
            },
            onDismiss = { modal = null },
            onDelete = {
                modal = null
                onDeletePerson()
            },
            initialCurrency = summary.person.currencyOverride,
            defaultCurrency = defaultCurrency,
        )
    }
}

/** Counts from the balance on screen to [target] whenever it changes. Opening the screen shows it directly. */
@Composable
private fun rememberCountedBalance(target: Long, delayMs: Int, reducedMotion: Boolean): Long {
    var shown by remember { mutableLongStateOf(target) }
    LaunchedEffect(target) {
        if (reducedMotion) {
            shown = target
            return@LaunchedEffect
        }
        delay(delayMs.toLong())
        val from = shown
        Animatable(0f).animateTo(1f, tween(COUNT_MS, easing = CubicBezierEasing(0.33f, 1f, 0.68f, 1f))) {
            shown = interpolateMinor(from, target, value)
        }
    }
    return shown
}

@Composable
private fun PersonLiquid(balanceMinor: Long, levelDelayMs: Int, initialLevel: Float) {
    val lastColor = remember { mutableStateOf(liquidColor(balanceMinor)) }
    val color = settledLiquidColor(balanceMinor, lastColor.value)
    SideEffect { lastColor.value = color }
    Liquid(
        level = personLevel(balanceMinor),
        color = color,
        modifier = Modifier.fillMaxSize(),
        waveHeight = 16.dp,
        showBackWave = true,
        levelDelayMs = levelDelayMs,
        initialLevel = initialLevel,
    )
}

@Composable
private fun Balance(summary: PersonSummary, shownMinor: Long) {
    val name = summary.person.name
    val balance = shownMinor
    val amount = formatAmount(shownMinor)
    Column(
        Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        FitText(
            buildAnnotatedString {
                append(amount)
                withStyle(SpanStyle(fontFamily = Figtree, fontSize = 15.sp, color = Tide.Muted)) {
                    append(" " + summary.currency)
                }
            },
            style = TextStyle(
                color = if (balance == 0L) Tide.Text else liquidColor(balance),
                fontFamily = Unbounded,
                fontWeight = FontWeight.SemiBold,
            ),
            baseSp = 52,
            minSp = 20,
        )
        Row {
            if (balance < 0) StandingText("You owe ")
            if (balance == 0L) StandingText("All square")
            else StandingText(name, Modifier.weight(1f, fill = false))
            if (balance > 0) StandingText(" owes you")
        }
    }
}

@Composable
private fun StandingText(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier, color = Tide.Muted, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
}

@Composable
private fun History(
    entries: List<Entry>,
    currency: String,
    reducedMotion: Boolean,
    modifier: Modifier,
    onTap: (Entry) -> Unit,
) {
    val now = System.currentTimeMillis()
    val zone = ZoneId.systemDefault()
    val ordered = historyOrder(entries)
    val list = rememberLazyListState()
    val seen = remember { entries.mapTo(mutableSetOf()) { it.id } }
    LaunchedEffect(ordered.map { it.id }) {
        val added = ordered.indexOfFirst { it.id !in seen }
        seen += ordered.map { it.id }
        if (added >= 0) list.animateScrollToItem(added)
    }
    LazyColumn(
        modifier.fillMaxWidth(),
        state = list,
        contentPadding = PaddingValues(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(ordered, key = { it.id }) { entry ->
            val isNew = remember(entry.id) { entry.id !in seen }
            val entering = remember { Animatable(if (isNew && !reducedMotion) 0f else 1f) }
            LaunchedEffect(Unit) { entering.animateTo(1f, tween(ROW_IN_MS)) }
            EntryCard(
                entry,
                currency,
                formatWhen(entry.occurredAt, now, zone),
                modifier = Modifier.animateItem(fadeInSpec = null).graphicsLayer {
                    alpha = entering.value
                    translationY = (1 - entering.value) * 16.dp.toPx()
                },
            ) { onTap(entry) }
        }
    }
}

@Composable
fun EntryCard(
    entry: Entry,
    currency: String,
    whenText: String,
    personName: String? = null,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val gave = entry.direction == Direction.GAVE
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Tide.Panel)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            if (personName != null) {
                Text(personName, color = Tide.Text, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Text(
                if (gave) "You gave" else "You got",
                color = Tide.Text,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
            )
            entry.note?.let { Text(it, color = Tide.Secondary, fontSize = 14.sp, lineHeight = 19.sp) }
            Text(whenText, color = Tide.Muted, fontSize = 13.sp)
        }
        Text(
            buildAnnotatedString {
                append(formatAmount(entry.amountMinor))
                withStyle(SpanStyle(fontFamily = Figtree, fontSize = 11.sp, color = Tide.Muted)) {
                    append(" $currency")
                }
            },
            color = if (gave) Tide.Blue else Tide.Orange,
            fontFamily = Unbounded,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
        )
    }
}

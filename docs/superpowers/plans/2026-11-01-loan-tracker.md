# Tide Loan Tracker Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a personal Android app that tracks money lent and borrowed per person, in the animated Tide look.

**Architecture:** One Gradle app module. Pure Kotlin functions in `domain/` hold all the money logic and are unit tested. Room stores everything on the phone. A single `AppViewModel` turns database flows into `AppState`, and Compose screens render it. Navigation is a plain screen stack in `AnimatedContent`, inside a `SharedTransitionLayout` for the jar expansion.

**Tech Stack:** Kotlin 2.1.10, Jetpack Compose (BOM 2025.01.00), Material 3, Room 2.6.1 with KSP 2.1.10-1.0.29, AGP 8.13.2, Gradle 8.14.5, JDK 17, JUnit 4.

**Spec:** `docs/superpowers/specs/2026-11-01-loan-tracker-design.md`. Read it before starting. `docs/design/tide-mockup.dc.html` holds the exact look, and is the reference for colours, spacing and timing.

## Global Constraints

- These versions are all cached on this machine and work together. Don't upgrade them.
- Package and application ID: `app.tide.loans`. App name: `Tide`.
- `minSdk 26`, `compileSdk 36`, `targetSdk 36`. Test device: Pixel 7, Android 17, adb serial `29141FDH2009AN`.
- Build with JDK 17 (`C:\Program Files\Java\jdk-17`), never Android Studio's JDK 25: Gradle 8.14.5 can't run on it.
- Windows shell commands use `./gradlew`. Prefix shell commands with `rtk`, per the owner's global instructions.
- No internet permission. No analytics. No navigation, DI or image libraries.
- Amounts are `Long` minor units: value × 100 for every currency. Never use `Float` or `Double` for money, except inside `personLevel`.
- All copy is English. Use the spec's words exactly: "You gave", "You got", "owes you", "you owe", "all square", "Owed to you", "You owe", "Add person", "Add entry".
- Colours, from the mockup: ground `#0A1622`, panel `#12263A`, chip `#1D3850`, text `#EAF6FF`, secondary text `#D2E2EE`, muted `#A9C2D6`, liquid blue `#7CCBFF`, liquid orange `#FFAE5C`.
- Sample data from the mockup must not ship.
- Screen, sheet and row sizes come from the mockup. Touch targets are at least 44dp. Icon-only buttons have a content description.

## Review Focus

- A very long name or a very large amount must shrink or truncate, and never overflow a jar or the balance. Tested in Task 2 (`amountSp`).
- Amount input such as `0`, `0.00`, `.`, `12.345`, `abc`, or an absurdly large number must keep Save disabled and never crash. Tested in Task 2 (`parseAmount`).
- Deleting a person while their screen is on the stack, including when reached from Search, must return to a valid screen. Tested in Task 4 (`pruneStack`).
- Back-dating or editing an entry must reorder the history and people correctly, and a balance crossing zero by edit or delete must flip the colour. Tested in Task 2 (`summarize`, `historyOrder`).
- Changing a person's currency must move them to the right totals and jar scale immediately, with amounts unchanged. Tested in Task 2 (`summarize`, `totals`).

---

### Task 1: Project scaffold

**Files:**
- Create: `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`, `gradle/libs.versions.toml`, `.gitignore`, `local.properties`
- Create: `app/build.gradle.kts`, `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/java/app/tide/loans/MainActivity.kt`
- Create: `app/src/main/java/app/tide/loans/ui/theme/Theme.kt`
- Create: `app/src/main/res/font/unbounded.ttf`, `app/src/main/res/font/figtree.ttf`, `app/src/main/res/values/strings.xml`, `app/src/main/res/values/themes.xml`

**Interfaces:**
- Produces: `object Tide` holding the Global Constraints colours as `Color` vals (`Tide.Ground`, `Panel`, `Chip`, `Text`, `Secondary`, `Muted`, `Blue`, `Orange`).
- Produces: `val Unbounded: FontFamily`, `val Figtree: FontFamily`, `@Composable fun TideTheme(content: @Composable () -> Unit)`.

- [ ] **Step 1: Create the repo and Gradle wrapper**

Run `git init` in `C:\Workspace\loan-tracker`. Generate the wrapper with the cached Gradle, from a folder containing an empty `settings.gradle.kts`:

```bash
"$(ls -d /c/Users/bbake/.gradle/wrapper/dists/gradle-8.14.5-bin/*/gradle-8.14.5)/bin/gradle" wrapper --gradle-version 8.14.5
```

- [ ] **Step 2: Write the build files**

`gradle/libs.versions.toml` pins: `agp 8.13.2`, `kotlin 2.1.10`, `ksp 2.1.10-1.0.29`, `composeBom 2025.01.00`, `room 2.6.1`, `activityCompose 1.10.0`, `lifecycle 2.9.4`, `junit 4.13.2`, `androidxTestRunner 1.6.2`, `androidxTestExtJunit 1.2.1`, `coroutinesTest 1.9.0`.

`app/build.gradle.kts` applies `com.android.application`, `org.jetbrains.kotlin.android`, `org.jetbrains.kotlin.plugin.compose` and `com.google.devtools.ksp`. It depends on: Compose BOM, `ui`, `foundation`, `material3`, `material-icons-core`, `activity-compose`, `lifecycle-runtime-compose`, `lifecycle-viewmodel-compose`, `room-runtime`, `room-ktx`, KSP `room-compiler`, plus test dependencies `junit`, `androidx.test:runner`, `androidx.test.ext:junit`, `kotlinx-coroutines-test`, `room-testing`.

`gradle.properties` includes `org.gradle.java.home=C:/Program Files/Java/jdk-17`, `android.useAndroidX=true`, and `org.gradle.jvmargs=-Xmx2g`.

`local.properties` sets `sdk.dir=C\:\\Users\\bbake\\AppData\\Local\\Android\\Sdk`. `.gitignore` covers `.gradle/`, `build/`, `local.properties`, `.idea/`, `*.iml`.

- [ ] **Step 3: Add the fonts**

Download into `app/src/main/res/font/`, and confirm each file is over 100 KB:

```bash
curl -L -o app/src/main/res/font/unbounded.ttf "https://github.com/google/fonts/raw/main/ofl/unbounded/Unbounded%5Bwght%5D.ttf"
curl -L -o app/src/main/res/font/figtree.ttf "https://github.com/google/fonts/raw/main/ofl/figtree/Figtree%5Bwght%5D.ttf"
```

- [ ] **Step 4: Write `Theme.kt`, the manifest and `MainActivity`**

Each weight in `Unbounded` and `Figtree` is a `Font(R.font.x, weight = W, variationSettings = FontVariation.Settings(FontVariation.weight(W.weight)))`. Unbounded: 500, 600. Figtree: 400, 600, 700. `TideTheme` wraps a dark `MaterialTheme` with `background = Tide.Ground`, `surface = Tide.Panel`, `primary = Tide.Text`, `onSurface = Tide.Text`, and default text in Figtree.

The manifest locks `android:screenOrientation="portrait"`, sets `android:windowSoftInputMode="adjustResize"`, and declares no permissions. `MainActivity` calls `enableEdgeToEdge()` and shows "Tide" in `Unbounded` on `Tide.Ground`.

- [ ] **Step 5: Build, install and look**

Run: `./gradlew installDebug && adb -s 29141FDH2009AN shell am start -n app.tide.loans/.MainActivity`
Expected: `BUILD SUCCESSFUL`, and the phone shows "Tide" in Unbounded on dark navy. Take a screenshot with `adb -s 29141FDH2009AN exec-out screencap -p`, saved outside the repo, to confirm.

- [ ] **Step 6: Commit**

```bash
git add -A && git commit -m "chore: scaffold Tide Android app"
```

---

### Task 2: Domain logic

**Files:**
- Create: `app/src/main/java/app/tide/loans/data/Model.kt`
- Create: `app/src/main/java/app/tide/loans/domain/Money.kt`, `Balances.kt`, `Search.kt`, `Time.kt`
- Test: `app/src/test/java/app/tide/loans/domain/MoneyTest.kt`, `BalancesTest.kt`, `SearchTest.kt`, `TimeTest.kt`

**Interfaces:**
- Produces, in `data/Model.kt` (Room annotations are added in Task 3 on these same classes):

```kotlin
enum class Direction { GAVE, GOT }
data class Person(val id: Long = 0, val name: String, val currencyOverride: String? = null, val createdAt: Long)
data class Entry(val id: Long = 0, val personId: Long, val direction: Direction,
                 val amountMinor: Long, val note: String? = null, val occurredAt: Long)
```

- Produces, in `domain/`:

```kotlin
const val MAX_AMOUNT_MINOR = 100_000_000_000_000L   // 1 trillion
fun parseAmount(text: String): Long?                 // null when invalid, zero, or over MAX
fun formatAmount(minor: Long): String                // absolute value, grouped
fun formatTotals(byCurrency: Map<String, Long>): String
fun normalizeNote(text: String): String?             // trimmed, blank becomes null

data class PersonSummary(val person: Person, val currency: String, val balanceMinor: Long,
                         val jarLevel: Float, val lastActivity: Long)
data class Totals(val owedToYou: Map<String, Long>, val youOwe: Map<String, Long>)
fun balanceMinor(entries: List<Entry>): Long
fun effectiveCurrency(person: Person, defaultCurrency: String): String
fun jarLevel(balanceMinor: Long, maxAbsInCurrency: Long): Float
fun personLevel(balanceMinor: Long): Float
fun summarize(people: List<Person>, entries: List<Entry>, defaultCurrency: String): List<PersonSummary>
fun totals(summaries: List<PersonSummary>): Totals
fun historyOrder(entries: List<Entry>): List<Entry>  // newest first, then highest id
fun amountSp(text: String, baseSp: Int, minSp: Int, fitChars: Int): Int

data class SearchHit(val entry: Entry, val person: Person, val currency: String)
fun search(query: String, people: List<Person>, entries: List<Entry>, defaultCurrency: String): List<SearchHit>

fun resolveOccurredAt(original: Long?, picked: Long?, now: Long): Long
fun formatWhen(ms: Long, now: Long, zone: ZoneId): String
fun defaultCurrencyFor(locale: Locale): String
```

- [ ] **Step 1: Write the failing tests**

`MoneyTest`:

```kotlin
assertEquals(150000L, parseAmount("1500"))
assertEquals(150050L, parseAmount("1500.50"))
assertEquals(150050L, parseAmount("1500,50"))
assertEquals(50L, parseAmount(".5"))
assertEquals(150000L, parseAmount(" 1500 "))
listOf("", ".", "0", "0.00", "12.345", "abc", "-5", "1,500", "1.2.3", "99999999999999999999")
    .forEach { assertNull(it, parseAmount(it)) }
assertEquals("1,500", formatAmount(150000))
assertEquals("1,500.50", formatAmount(-150050))
assertEquals("0.05", formatAmount(5))
assertEquals("2,150 INR · 300 USD", formatTotals(mapOf("USD" to 30000L, "INR" to 215000L)))
assertEquals("0", formatTotals(emptyMap()))
assertNull(normalizeNote("   "))
assertEquals("Cash", normalizeNote(" Cash "))
```

`BalancesTest`, with `gave(amount, at)` and `got(amount, at)` test helpers:

```kotlin
assertEquals(150000L, balanceMinor(listOf(gave(200000), got(50000))))
// jar levels: 10–52% against the largest balance in the same currency
assertEquals(0f, jarLevel(0, 400000), 0f)
assertEquals(0.52f, jarLevel(-400000, 400000), 0.001f)
assertEquals(0.31f, jarLevel(200000, 400000), 0.001f)
// person level: log scale, 1 to 1,000,000, mapped to 24–62%
assertEquals(0f, personLevel(0), 0f)
assertEquals(0.24f, personLevel(100), 0.001f)
assertEquals(0.24f, personLevel(50), 0.001f)
assertEquals(0.62f, personLevel(100_000_000), 0.001f)
assertEquals(0.62f, personLevel(500_000_000), 0.001f)
assertTrue(personLevel(600000) > personLevel(400000))
assertEquals(personLevel(300000), personLevel(-300000), 0f)
```

`summarize` tests:

- A person with no override uses the default. Changing the default changes their currency and leaves `balanceMinor` the same.
- An override wins over the default.
- `jarLevel` compares only within a currency: Sam at 4,000 INR and Tom at 300 USD each get `0.52f`.
- Order is by `lastActivity` descending, where `lastActivity = max(createdAt, latest occurredAt)`. A person created just now sorts above someone with an older entry.
- Back-dating an entry changes the order to match.
- A person with no entries has balance 0 and level 0, and stays in the list.

`totals` tests:

- Amounts are grouped per currency and never added across currencies. Zero balances don't create a key.
- `historyOrder` sorts newest first, and ties go to the highest id.

`amountSp` tests:

```kotlin
assertEquals(52, amountSp("1,500", 52, 24, 7))
assertEquals(26, amountSp("12,345,678,901", 52, 24, 7))
assertEquals(24, amountSp("1,000,000,000,000", 52, 24, 7))
```

`SearchTest`:

- Blank query returns nothing.
- `"500"` matches an entry of exactly 500.00, and does not match 1,500 or 5,000.
- `"1,500"` and `"1500.00"` both match 1,500.00. Strip commas before `parseAmount`.
- `"lunch"` and `"LUNCH"` match the note "Paid back at lunch".
- `"500"` also matches a note containing "500".
- An amount matches in any currency, with the hit carrying that person's currency.
- Hits are newest first.

`TimeTest`:

```kotlin
assertEquals(9000L, resolveOccurredAt(original = null, picked = null, now = 9000L))
assertEquals(5000L, resolveOccurredAt(original = null, picked = 5000L, now = 9000L))
assertEquals(3000L, resolveOccurredAt(original = 3000L, picked = null, now = 9000L))
assertEquals(5000L, resolveOccurredAt(original = 3000L, picked = 5000L, now = 9000L))
```

`formatWhen`, with fixed `now` and `ZoneOffset.UTC`: `"Today, 9:41 AM"`, `"Yesterday, 9:41 AM"`, `"Oct 28, 9:41 AM"`, and `"Oct 28, 2025, 9:41 AM"` for another year.

`defaultCurrencyFor`: `Locale.US` gives `"USD"`, `Locale("en", "IN")` gives `"INR"`, `Locale("en")` gives `"USD"`, and `Locale("en", "AQ")` gives `"USD"`.

- [ ] **Step 2: Run them to confirm they fail**

Run: `./gradlew testDebugUnitTest`
Expected: FAIL, unresolved references.

- [ ] **Step 3: Implement `Model.kt` and the four domain files**

`amountSp`: `baseSp` when `text.length <= fitChars`, otherwise `(baseSp * fitChars / text.length)` floored at `minSp`.

`jarLevel`: 0 when balance is 0, otherwise `0.10f + 0.42f * (abs(balance) / maxAbsInCurrency)`.

`personLevel`:

```kotlin
if (balanceMinor == 0L) return 0f
val major = abs(balanceMinor) / 100.0
return (0.24 + 0.38 * (log10(max(1.0, major)) / 6.0).coerceAtMost(1.0)).toFloat()
```

`defaultCurrencyFor`: `Currency.getInstance(locale)`, falling back to `"USD"` on exception or null.

- [ ] **Step 4: Run the tests**

Run: `./gradlew testDebugUnitTest`
Expected: `BUILD SUCCESSFUL`, all pass.

- [ ] **Step 5: Commit**

```bash
git add -A && git commit -m "feat: add balance, currency, search and time logic"
```

---

### Task 3: Storage and app state

**Files:**
- Modify: `app/src/main/java/app/tide/loans/data/Model.kt`
- Create: `app/src/main/java/app/tide/loans/data/Database.kt`, `app/src/main/java/app/tide/loans/AppViewModel.kt`
- Test: `app/src/androidTest/java/app/tide/loans/data/LoanDaoTest.kt`

**Interfaces:**
- Consumes: `Person`, `Entry`, `summarize`, `totals`, `defaultCurrencyFor` from Task 2.
- Produces:

```kotlin
@Entity(tableName = "settings") data class Settings(@PrimaryKey val id: Int = 1, val defaultCurrency: String)

@Dao interface LoanDao {
    @Query("SELECT * FROM person") fun people(): Flow<List<Person>>
    @Query("SELECT * FROM entry") fun entries(): Flow<List<Entry>>
    @Query("SELECT * FROM settings WHERE id = 1") fun settings(): Flow<Settings?>
    @Insert suspend fun insert(person: Person): Long
    @Update suspend fun update(person: Person)
    @Query("DELETE FROM person WHERE id = :id") suspend fun deletePerson(id: Long)
    @Upsert suspend fun save(entry: Entry)
    @Query("DELETE FROM entry WHERE id = :id") suspend fun deleteEntry(id: Long)
    @Upsert suspend fun save(settings: Settings)
}

data class AppState(val loaded: Boolean = false, val people: List<Person> = emptyList(),
                    val entries: List<Entry> = emptyList(), val summaries: List<PersonSummary> = emptyList(),
                    val totals: Totals = Totals(emptyMap(), emptyMap()), val defaultCurrency: String = "USD")

class AppViewModel(private val dao: LoanDao, locale: Locale) : ViewModel() {
    val state: StateFlow<AppState>
    fun addPerson(name: String)
    fun updatePerson(id: Long, name: String, currencyOverride: String?)
    fun deletePerson(id: Long)
    fun saveEntry(id: Long?, personId: Long, direction: Direction, amountMinor: Long, note: String?, occurredAt: Long)
    fun deleteEntry(id: Long)
    fun setDefaultCurrency(code: String)
}
```

- [ ] **Step 1: Write the failing instrumented test**

`LoanDaoTest` uses `Room.inMemoryDatabaseBuilder`:

- `save` then `entries().first()` returns the entry with `direction == Direction.GOT` intact.
- `deletePerson` removes that person's entries (foreign key cascade).
- `settings().first()` is null at first, and returns the saved value afterwards.
- Saving an `Entry` with an existing `id` replaces it, and doesn't add a second row.

- [ ] **Step 2: Run it to confirm it fails**

Run: `./gradlew connectedDebugAndroidTest`
Expected: FAIL at compile time, `LoanDao` not found.

- [ ] **Step 3: Implement the database and view model**

Annotate `Person` (`tableName = "person"`) and `Entry` (`tableName = "entry"`, `ForeignKey` to `Person` with `onDelete = CASCADE`, `@Index("personId")`). `@PrimaryKey(autoGenerate = true)` on both IDs. `AppDatabase` is version 1, `exportSchema = false`, with `Direction` left to Room's built-in enum conversion. `AppDatabase.get(context)` returns a process-wide singleton named `tide.db`.

`AppViewModel.state` combines the three DAO flows, using `defaultCurrencyFor(locale)` while `settings` is null, and runs `summarize` and `totals`. `AppViewModel.Factory` is a small `ViewModelProvider.Factory` taking `Context`. `addPerson` trims the name, ignores blank names, and sets `createdAt = System.currentTimeMillis()`.

- [ ] **Step 4: Run the test**

Run: `./gradlew connectedDebugAndroidTest`
Expected: `BUILD SUCCESSFUL`, on the Pixel 7.

- [ ] **Step 5: Commit**

```bash
git add -A && git commit -m "feat: store people, entries and settings on the phone"
```

---

### Task 4: People screen, jars and navigation

**Files:**
- Create: `app/src/main/java/app/tide/loans/ui/Navigation.kt`, `ui/AppRoot.kt`
- Create: `app/src/main/java/app/tide/loans/ui/liquid/Liquid.kt`
- Create: `app/src/main/java/app/tide/loans/ui/people/PeopleScreen.kt`
- Create: `app/src/main/java/app/tide/loans/ui/sheets/PersonSheet.kt`
- Modify: `app/src/main/java/app/tide/loans/MainActivity.kt`
- Test: `app/src/test/java/app/tide/loans/ui/NavigationTest.kt`

**Interfaces:**
- Consumes: `AppViewModel`, `AppState`, `PersonSummary`, `formatAmount`, `formatTotals`, `amountSp`.
- Produces:

```kotlin
sealed interface Screen { data object People : Screen; data class Person(val id: Long) : Screen
                          data object Search : Screen; data object Settings : Screen }
fun pruneStack(stack: List<Screen>, existingPersonIds: Set<Long>): List<Screen>

@Composable fun Liquid(level: Float, color: Color, modifier: Modifier = Modifier,
                       waveHeight: Dp = 14.dp, showBackWave: Boolean = false)
@Composable fun PersonSheet(initialName: String, onSave: (String) -> Unit, onDismiss: () -> Unit)
```

`AppRoot` keeps `stack: List<Screen>` in `rememberSaveable` with a custom `Saver`, and exposes `push`/`pop` lambdas. `BackHandler` pops when the stack has more than one screen.

- [ ] **Step 1: Write the failing test**

`NavigationTest`:

```kotlin
val stack = listOf(Screen.People, Screen.Search, Screen.Person(7))
assertEquals(listOf(Screen.People, Screen.Search), pruneStack(stack, setOf(1)))
assertEquals(stack, pruneStack(stack, setOf(7)))
assertEquals(listOf(Screen.People), pruneStack(listOf(Screen.People, Screen.Person(7)), emptySet()))
```

- [ ] **Step 2: Run it to confirm it fails**

Run: `./gradlew testDebugUnitTest`
Expected: FAIL, unresolved reference `pruneStack`.

- [ ] **Step 3: Implement `pruneStack`**

Run: `./gradlew testDebugUnitTest`
Expected: PASS.

- [ ] **Step 4: Implement `Liquid`**

Draw in a `Canvas`: a filled sine path from the waterline to the bottom, phase driven by `rememberInfiniteTransition` over 5s, with the wave's wavelength equal to half the width. `showBackWave` adds a second wave at 45% alpha, reversed, over 8s. The target level is animated with `animateFloatAsState` and `spring(dampingRatio = 0.45f, stiffness = 60f)` so it overshoots, and the colour with `animateColorAsState` over 600ms. No liquid is drawn at level 0.

- [ ] **Step 5: Implement `PeopleScreen`**

Follow the mockup's home screen. Header totals use `formatTotals`, and wrap if several currencies don't fit. There's a two-column `LazyVerticalGrid` of 210dp-tall jars with a 28dp corner radius, plus the dashed "Add person" tile last. The jar's text sits above the liquid: name, amount with its currency code, and "owes you" / "you owe" / "all square". Amounts use `amountSp(text, 20, 12, 9)` and names use one line with ellipsis. Add search and settings icon buttons in the header, wired to `push`. Their screens are empty placeholders until Tasks 8 and 9.

Jars fill from 0 on first appearance, staggered by 90ms per jar, using the same spring.

`PersonSheet` is a Material 3 `ModalBottomSheet` in `Tide.Panel`, with the name field focused on open and Save disabled when the name is blank.

`AppRoot` renders the top of the stack, and applies `pruneStack` whenever `state.people` changes once `state.loaded` is true. `MainActivity` shows `AppRoot` with the view model.

- [ ] **Step 6: Install and try it**

Run: `./gradlew installDebug`, then launch.
Expected on the phone: first launch shows only the "Add person" tile. Adding a person shows an empty jar. Close and reopen the app, and the person is still there.

- [ ] **Step 7: Commit**

```bash
git add -A && git commit -m "feat: add people screen with animated jars"
```

---

### Task 5: Person screen and entries

**Files:**
- Create: `app/src/main/java/app/tide/loans/ui/person/PersonScreen.kt`
- Create: `app/src/main/java/app/tide/loans/ui/sheets/EntrySheet.kt`, `ui/sheets/DateTimeDialogs.kt`, `ui/sheets/ConfirmDialog.kt`
- Modify: `app/src/main/java/app/tide/loans/ui/sheets/PersonSheet.kt`, `ui/AppRoot.kt`

**Interfaces:**
- Consumes: `Liquid`, `historyOrder`, `personLevel`, `resolveOccurredAt`, `parseAmount`, `normalizeNote`, `formatWhen`, `AppViewModel.saveEntry`, `deleteEntry`, `updatePerson`, `deletePerson`.
- Produces:

```kotlin
@Composable fun PersonScreen(summary: PersonSummary, entries: List<Entry>, onBack: () -> Unit,
    onSaveEntry: (id: Long?, Direction, amountMinor: Long, note: String?, occurredAt: Long) -> Unit,
    onDeleteEntry: (Long) -> Unit, onSavePerson: (name: String, currencyOverride: String?) -> Unit,
    onDeletePerson: () -> Unit, defaultCurrency: String)
@Composable fun EntrySheet(existing: Entry?, onSave: (Direction, Long, String?, Long) -> Unit,
                           onDelete: (() -> Unit)?, onDismiss: () -> Unit)
@Composable fun ConfirmDialog(title: String, body: String, confirmLabel: String,
                              onConfirm: () -> Unit, onDismiss: () -> Unit)
```

`PersonSheet` gains an `onDelete: (() -> Unit)?` parameter, shown only when editing.

- [ ] **Step 1: Implement `PersonScreen`**

Follow the mockup's person screen. `Liquid` fills the whole screen behind the content, with `showBackWave = true`, level from `personLevel`, blue when balance is zero or above and orange below. Above the history: back, name, edit, the balance in Unbounded at `amountSp(text, 52, 24, 7)` with its currency code, then "Maya owes you" / "You owe Maya" / "All square" using the person's name. The history is a `LazyColumn` of solid `Tide.Panel` cards from `historyOrder`, ending with the fixed "Add entry" pill. Tapping a card opens `EntrySheet` with that entry.

Keep the history list and the liquid layer in separate composables, since the owner may rework this layout later.

- [ ] **Step 2: Implement `EntrySheet`**

Direction toggle defaulting to "You gave", amount field with `KeyboardType.Decimal`, optional note, a "When" row, Save, and Delete when editing. Save is disabled while `parseAmount` returns null. The "When" row shows "Now" when adding, or the entry's time when editing. Tapping it opens `DateTimeDialogs`, Material 3 `DatePicker` then `TimePicker` in the phone's time zone.

The save moment comes from `resolveOccurredAt(existing?.occurredAt, picked, System.currentTimeMillis())`, called when Save is tapped, so an unchanged time is the moment of saving and not the moment the sheet opened. The sheet stays open if the amount is invalid.

Editing pre-fills the amount with `formatAmount` with commas removed.

- [ ] **Step 3: Wire edit and delete**

Edit opens `PersonSheet`. Until Task 8 adds the currency row, `onSavePerson` passes the person's existing `currencyOverride` unchanged. Deleting a person shows `ConfirmDialog` naming the person and warning that their history is erased, then calls `onDeletePerson`. Deleting an entry shows `ConfirmDialog` too. After a person is deleted, Task 4's `pruneStack` handles navigation.

- [ ] **Step 4: Install and try it on the phone**

Expected:

- Add "You gave 2,000" with a note, then "You got 500" with no note. The balance reads 1,500, and "owes you" shows.
- An entry saved without touching the time shows the current time.
- Change an entry to yesterday: it moves down the history and shows "Yesterday".
- Edit the 500 to 3,000: the balance goes to 1,000 "you owe" and the liquid turns orange.
- Delete that entry: the balance returns.
- Delete the person: the app returns to the People screen.

- [ ] **Step 5: Commit**

```bash
git add -A && git commit -m "feat: add person history and entry editing"
```

---

### Task 6: Save animation

**Files:**
- Create: `app/src/main/java/app/tide/loans/ui/liquid/Drop.kt`, `app/src/main/java/app/tide/loans/ui/ReducedMotion.kt`
- Modify: `app/src/main/java/app/tide/loans/ui/person/PersonScreen.kt`, `ui/liquid/Liquid.kt`, `ui/people/PeopleScreen.kt`

**Interfaces:**
- Consumes: `PersonScreen`, `Liquid`.
- Produces: `@Composable fun rememberReducedMotion(): Boolean`, true when `Settings.Global.ANIMATOR_DURATION_SCALE` is 0. `@Composable fun Drop(color: Color, targetY: Dp, onFinished: () -> Unit)`.
- `Liquid` gains `levelDelayMs: Int = 0`.

- [ ] **Step 1: Implement the sequence**

On saving an entry in `PersonScreen`, in this order, with timings from the mockup:

1. The sheet closes and a `Drop` falls from just below the balance to the waterline over 620ms, in blue for "You gave" and orange for "You got".
2. At 420ms the level starts to rise with its wobble (`levelDelayMs = 420`), and any colour change happens then too.
3. The balance counts from old to new over 900ms with a cubic ease-out. Interpolate the `Long` minor units through a 0–1 progress value, never through a `Float` balance.
4. The new history row slides up 16dp and fades in over 500ms.

Editing or deleting an entry animates the level and balance, without the drop. Opening a person doesn't count or drop. The liquid fills from 0 once.

- [ ] **Step 2: Honour reduced motion**

When `rememberReducedMotion()` is true, skip the drop, count and stagger, set values directly, and show still water.

- [ ] **Step 3: Install and try it**

Expected: saving shows the drop, then the rise and count. Saving enough to cross zero changes the colour during the rise.

Then run `adb -s 29141FDH2009AN shell settings put global animator_duration_scale 0`, relaunch, and confirm saving is instant with no drop. Restore it with `adb -s 29141FDH2009AN shell settings put global animator_duration_scale 1`.

- [ ] **Step 4: Commit**

```bash
git add -A && git commit -m "feat: animate saving an entry"
```

---

### Task 7: Jar expansion transition

**Files:**
- Modify: `app/src/main/java/app/tide/loans/ui/AppRoot.kt`, `ui/people/PeopleScreen.kt`, `ui/person/PersonScreen.kt`

**Interfaces:**
- Consumes: `AppRoot`, `PeopleScreen`, `PersonScreen`.
- `PeopleScreen` and `PersonScreen` gain `sharedScope: SharedTransitionScope` and `animatedScope: AnimatedVisibilityScope`.

- [ ] **Step 1: Implement the transition**

Wrap `AnimatedContent` in `SharedTransitionLayout`, under `@OptIn(ExperimentalSharedTransitionApi::class)`. The jar and the person screen's root each use `Modifier.sharedBounds(rememberSharedContentState("jar-$id"), animatedScope)`, so the jar grows into the full-screen liquid and shrinks back on Back. Animate the corner radius from 28dp to 0. Search and Settings use a plain fade and slide. With reduced motion, use a fade.

- [ ] **Step 2: Install and try it**

Expected: tapping a jar grows it to fill the screen with no flash of the ground colour. Back shrinks it into its place. The system back gesture behaves the same. Rapid repeated taps don't crash or leave a stuck screen.

- [ ] **Step 3: Commit**

```bash
git add -A && git commit -m "feat: expand jars into the person screen"
```

---

### Task 8: Settings and currencies

**Files:**
- Create: `app/src/main/java/app/tide/loans/ui/settings/SettingsScreen.kt`, `ui/settings/CurrencyPicker.kt`
- Modify: `app/src/main/java/app/tide/loans/domain/Money.kt`, `ui/sheets/PersonSheet.kt`, `ui/person/PersonScreen.kt`, `ui/AppRoot.kt`
- Test: `app/src/test/java/app/tide/loans/domain/MoneyTest.kt`

**Interfaces:**
- Consumes: `AppViewModel.setDefaultCurrency`, `updatePerson`.
- Produces:

```kotlin
data class CurrencyOption(val code: String, val name: String)
fun currencyOptions(query: String): List<CurrencyOption>   // sorted by code; filters on code or name
@Composable fun CurrencyPicker(selected: String?, allowDefault: Boolean, defaultCurrency: String,
                               onPick: (String?) -> Unit, onDismiss: () -> Unit)
```

- [ ] **Step 1: Write the failing test**

```kotlin
assertTrue(currencyOptions("").any { it.code == "INR" })
assertEquals("USD", currencyOptions("usd").first().code)
assertTrue(currencyOptions("rupee").any { it.code == "INR" })
assertTrue(currencyOptions("zzzz").isEmpty())
```

- [ ] **Step 2: Run it to confirm it fails**

Run: `./gradlew testDebugUnitTest`
Expected: FAIL, unresolved reference `currencyOptions`.

- [ ] **Step 3: Implement `currencyOptions`**

Use `java.util.Currency.getAvailableCurrencies()` with `Locale.ENGLISH` display names.

Run: `./gradlew testDebugUnitTest`
Expected: PASS.

- [ ] **Step 4: Implement the screens**

`SettingsScreen` has a back button and one row, "Default currency", opening `CurrencyPicker`. `CurrencyPicker` is a sheet with a search field and a list. With `allowDefault = true` its first row is "Use default (USD)", which passes `null`. `PersonSheet` gains a "Currency" row showing the override, or "Default (USD)".

Replace the Task 4 settings placeholder.

- [ ] **Step 5: Install and try it**

Expected:

- With two people, change the default currency: both change label, and amounts stay the same.
- Override one person: they keep that currency when the default changes again.
- Header totals show two figures, and jars are scaled separately.
- Set the override back to "Use default": that person rejoins the default.

- [ ] **Step 6: Commit**

```bash
git add -A && git commit -m "feat: add default and per-person currencies"
```

---

### Task 9: Search

**Files:**
- Create: `app/src/main/java/app/tide/loans/ui/search/SearchScreen.kt`
- Modify: `app/src/main/java/app/tide/loans/ui/AppRoot.kt`

**Interfaces:**
- Consumes: `search`, `SearchHit`, `formatAmount`, `formatWhen`, `Screen.Person`.
- Produces: `@Composable fun SearchScreen(people: List<Person>, entries: List<Entry>, defaultCurrency: String, onOpenPerson: (Long) -> Unit, onBack: () -> Unit)`

- [ ] **Step 1: Implement `SearchScreen`**

A focused search field at the top, then results on `Tide.Panel` cards using the history card style, with the person's name added: person, "You gave" or "You got", amount with currency code, note, and date. Tapping a result pushes `Screen.Person`. If nothing matches, show "Nothing found". Replace the Task 4 search placeholder.

- [ ] **Step 2: Install and try it**

Expected:

- Searching "1,500" finds entries of exactly 1,500 across people, and not 15,000.
- Searching part of a note finds it, in any letter case.
- Opening a result shows that person, and Back returns to the results.

- [ ] **Step 3: Commit**

```bash
git add -A && git commit -m "feat: search entries across people"
```

---

### Task 10: Icon and final check

**Files:**
- Create: `app/src/main/res/drawable/ic_launcher_foreground.xml`, `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`, `app/src/main/res/values/ic_launcher_background.xml`
- Modify: `app/src/main/AndroidManifest.xml`

- [ ] **Step 1: Add the icon**

An adaptive vector icon: a blue wave filling the lower half on `Tide.Ground`. No PNG assets. Android 13 and later's themed icon isn't needed.

- [ ] **Step 2: Run every check**

Run: `./gradlew testDebugUnitTest connectedDebugAndroidTest assembleDebug`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Walk through the spec on the phone**

Install a fresh copy (`adb -s 29141FDH2009AN uninstall app.tide.loans`, then `./gradlew installDebug`). Confirm each item under "Screens and behaviour" and "Motion" in the spec, including the long-name and long-amount cases from Review Focus: a 40-character name and an amount of 999,999,999,999.

Restart the phone with `adb -s 29141FDH2009AN reboot`, open the app, and confirm the records are still there. This reboots the owner's phone, so ask before doing it.

- [ ] **Step 4: Commit**

```bash
git add -A && git commit -m "feat: add app icon"
```

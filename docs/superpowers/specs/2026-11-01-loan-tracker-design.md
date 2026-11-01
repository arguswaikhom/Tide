# Loan Tracker (Tide): design

Date: 2026-11-01

## Intent

A personal Android app for remembering who owes what. The owner lends and borrows money with friends on an ongoing basis, forgets amounts, and sometimes sees an amount in the account history without remembering what it was for. The app is their memory: a running balance per person, with a note on each entry explaining what happened.

The app is not for release. It has only a few screens, so its appeal comes from unique UI and animation. A correct but plain app would miss the point.

It is a tracker only. It never moves money and never connects to a bank.

## Goals

- See at a glance who owes you and who you owe, and how much.
- Add people, and edit them later.
- Open a person to see their full history.
- Add an entry in seconds, with an optional note and a time that defaults to now but can be changed.
- Find an entry by amount or note across all people.
- Delight: the Tide look and motion are a core requirement, not polish to add if time allows.

## Non-goals

- Backup, restore, export or sync. The owner decided against backup for now, and accepts that losing the phone means losing the records.
- Accounts, cloud or internet access of any kind.
- Currency conversion or exchange rates.
- Reminders, notifications, interest, due dates, contact import, charts.
- App lock.
- Light theme, tablets, landscape, languages other than English.

## Anti-goals

- A plain Material list with a nice colour. If it looks like a template, it has failed even if it works.
- Adding numbers from different currencies into one total.
- Animation that slows down entering a record. Adding an entry must stay fast.

## Look and feel

Direction C, Tide, chosen by the owner from four clickable mockups.

- Interactive canvas: https://claude.ai/artifact/Ac17z7oD63dYc5TRR2qeqP (the Tide artboard)
- Source of the mockup, kept with the project: `docs/design/tide-mockup.dc.html`. It holds the exact colours, type, spacing and animation timings. Treat those as the starting values.

The mockup's people and notes are examples and must not ship. It predates the currency and settings decisions, so it doesn't show those. Build them in the same visual language.

Key elements:

- Dark navy ground with a palette of blue liquid (they owe you) and orange liquid (you owe them). Fonts are Unbounded for numbers and Figtree for text.
- Each person is a jar with animated waves. Name, amount and "owes you" / "you owe" / "all square" sit above the liquid.
- On the person screen the liquid fills the background, with the balance above it and the history on solid dark cards so text stays readable.

## Screens and behaviour

### People screen

- Header shows "Owed to you" and "You owe" totals, with one figure per currency (for example "2,150 INR · 300 USD"). Totals never add across currencies.
- A two-column grid of jars, plus an "Add person" tile. Tapping a jar opens that person.
- A settings entry point, drawn in the Tide style.
- On first launch there are no people, so only the "Add person" tile shows.

### Person screen

- Name, balance, and direction ("Maya owes you", "You owe Sam", "All square").
- Layout follows the original Tide mockup: the liquid fills the whole screen as a background and the history scrolls over it on solid cards. The owner saw two alternatives (a fixed jar on top, a draggable history panel) and chose this one, knowing that a long history will hide most of the liquid. They'll try it on the phone and may change it later, so keep the screen easy to rework.
- History, newest first. Each entry shows "You gave" or "You got", the amount, the note if any, and the date and time.
- "Add entry" button. Tapping an entry edits it.
- An edit control opens the person's details: name, currency override, and delete.

### Search

The owner asked for this after the mockups, so it isn't in them. Build it in the same visual language.

- Opened from the People screen.
- Searches all entries across all people, by amount or by note text.
- Each result shows the person, "You gave" or "You got", the amount, the note and the date.
- Tapping a result opens that person.
- An amount matches regardless of currency.

### Add or edit an entry

A bottom sheet, as in the mockup.

- Direction: "You gave" or "You got". Default is "You gave".
- Amount: required, greater than zero.
- Note: optional.
- When: when adding, the entry takes the moment it is saved unless the owner changes it. When editing, the entry keeps its existing time unless changed.
- Editing also offers delete.

The words "You gave" and "You got" replace "borrowed" and "lent" so that a repayment reads correctly. There is no separate repayment type: a repayment is an entry in the other direction.

### Settings

- Default currency.

### Currency rules

- Settings holds one default currency. Each person may override it.
- A person with no override follows the default, so changing the default changes them too.
- Changing any currency changes the label only. Amounts stay the same and nothing is converted.
- A person has exactly one currency, so all their entries share it.

## Calculations

- Balance for a person = sum of "You gave" minus sum of "You got".
- Positive: they owe you (blue). Negative: you owe them (orange). Zero: all square (empty jar).
- A jar's level on the People screen is measured against the largest absolute balance among people using the same currency. People with different currencies are not compared. Jar levels range from 10% to 52%, as in the mockup.
- The person screen's liquid level uses that person's balance alone, on a logarithmic scale from 1 to 1,000,000, mapped to the mockup's 24–62% range. This is the builder's call: a relative level would stay at the top for the person with the largest balance, or the only person in a currency, so the liquid would not move when an entry is saved.
- Text must stay clear of the liquid at the top of the range.

## Motion

- Jars fill from empty when the People screen appears, staggered.
- Waves drift continuously, on jars and on the person screen.
- Saving an entry: a drop in the entry's colour falls into the liquid, then the level rises with a wobble and the balance counts to its new value.
- If the balance crosses zero, the liquid changes colour.
- New history entries slide in, and sheets rise from the bottom.
- Respect the system setting for removing animations.

## How it gets made

The owner left these to the builder, so they are not the owner's decisions:

- Native Android in Kotlin with Jetpack Compose.
- Room for local storage on the phone.
- Amounts stored as whole minor units (such as cents) to avoid rounding errors, with up to two decimal places entered.

Environment: Android Studio 2026.2 and an SDK with platforms up to API 37 are installed, with JDK 17 on the path. The owner's phone is connected over adb and is the real test device.

## Calls made without asking the owner

- Tapping a jar expands it into the person screen's full-screen liquid. The mockup cuts between screens. This fits the owner's request for cool animations.
- Entries can be edited and deleted, because typos happen.
- Deleting a person or an entry asks for confirmation.
- Two people can share a name.
- First-launch default currency comes from the phone's region.
- People are ordered by most recent entry, with new people first.
- A person with a zero balance stays on the People screen with an empty jar.
- The person screen's liquid uses its own scale, described under Calculations, so it always moves when a balance changes.

## Left to the builder

- Project structure, minimum Android version (must run on the owner's phone), and library versions.
- The currency picker's design, and how amounts show the currency (symbol or code).
- How to render the liquid and waves, as long as it is smooth on the owner's phone.
- App name and icon. Working title: Tide.

## Done means

- All the behaviour above works on the owner's phone.
- Records survive closing the app and restarting the phone.
- Balance, per-currency totals and jar-level logic have automated tests.

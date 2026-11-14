# HealthMate code guide (for the viva)

Read this with the code open. It explains each part in plain words, in the order the data flows.

## 1. The big picture: MVVM

```
Screen (Compose)  ->  ViewModel  ->  Repository  ->  DAO (Room)  ->  SQLite file
      ^                   |
      |---- StateFlow ----|
```

- **Screen** (for example `RecordsScreen.kt`) only draws things and passes button clicks to the ViewModel. It has no database code.
- **ViewModel** (for example `RecordsViewModel.kt`) holds the screen's state and does the work when a button is pressed. It survives screen rotation.
- **Repository** (for example `HealthRepository.kt`) is the single place that talks to the database. If we changed the database later, only this layer would change.
- **DAO** (for example `HealthRecordDao.kt`) is an interface. Each function has a SQL statement and Room writes the code for it.
- **SQLite** is the actual database file, `healthmate.db`, stored privately inside the app.

**State management:** a ViewModel exposes data as a `StateFlow`. The screen calls `collectAsState()`, so whenever the data changes, the screen redraws by itself. Because the DAO returns `Flow`, adding a record in the form makes the list update automatically, with no "refresh" code.

Form text boxes use Compose `mutableStateOf` inside the ViewModel, because they change on every key press and don't come from the database.

## 2. How the app talks to SQLite (Room)

Room is Google's library that sits on top of SQLite.

- `HealthRecord.kt` is marked `@Entity(tableName = "health_records")`. Room creates this table:
  `CREATE TABLE health_records (id INTEGER PRIMARY KEY AUTOINCREMENT, date TEXT, steps INTEGER, calories INTEGER, water INTEGER)`
- `HealthRecordDao.kt` has the SQL:
  - Create: `@Insert`
  - Read: `SELECT * FROM health_records ORDER BY date DESC`
  - Update: `@Update`
  - Delete: `@Delete`
  - Search: `SELECT * FROM health_records WHERE date BETWEEN :from AND :to`
- `AppDatabase.kt` lists all tables and makes one shared database object (a singleton).
- Room checks the SQL when the app is built, so a typo in a query is a build error, not a crash.
- Database work runs in `suspend` functions inside `viewModelScope.launch`, so it never freezes the screen.

**Why dates are text (yyyy-MM-dd):** the paper asks for TEXT, and this format sorts correctly as text, so `BETWEEN` and `ORDER BY` work.

**Other tables:** `foods` (food list), `food_logs` (food diary), `sleep_logs`. `health_records` is exactly as the paper says.

## 3. Walkthrough of each feature

**Sample data** (`MainActivity.addSampleDataOnFirstLaunch`, `DummyData.kt`): on the first launch it adds 14 days of records, the 86 foods, a few food logs and 7 nights of sleep. A flag in SharedPreferences stops it from running twice.

**Add / edit record** (`RecordFormScreen`, `RecordFormViewModel`): one screen is used for both. The route `record_form/0` means add, `record_form/5` means edit record 5. On Save, the ViewModel checks if another record already has that date, then calls `Validator.validateRecord`. If there are errors, they show under each box. If not, it inserts or updates.

**Validation** (`util/Validator.kt`): plain Kotlin with no Android code, so it can be unit tested. It checks empty, not a whole number, negative, too high (limits at the top of the file), bad date, future date and duplicate date.

**Delete with Undo** (`RecordsViewModel.delete` and `undoDelete`): the deleted record is kept in a variable. If Undo is pressed on the snackbar, it is inserted again with the same id.

**Search** (`RecordsViewModel.records`): `_filter` holds the chosen dates (or null for all). `flatMapLatest` switches to the right database query whenever the filter changes.

**Dashboard** (`DashboardViewModel`): `period` (Day, Week or Month) and `date` decide a date range. Week is Monday to Sunday, Month is the 1st to the last day (`DateUtils`). The records in that range are added up into a `Summary`. Averages are per day that has a record.

**Bar chart** (`ui/components/BarChart.kt`): drawn by hand on a Compose `Canvas`. Each bar's height = value / biggest value × chart height. The dashed line is the goal.

**Step counter** (`feature/steps/StepTracker.kt`, `util/StepCounterLogic.kt`): the phone's `TYPE_STEP_COUNTER` sensor gives total steps since the phone was turned on. `StepCounterLogic` turns that into "steps today":
- first reading or new day: remember this reading as the starting point
- normal: steps = reading minus starting point
- reading went down: the phone restarted, so keep the old steps as an offset
The result is saved to today's record every 10 steps, but it never lowers a number typed by hand. The sensor only listens while the app is open (onResume/onPause). Because the sensor keeps its own total, steps walked while the app was closed are still counted the next time it opens. If there is no sensor or no permission, a message is shown instead.

**Calories from steps** (`util/CalorieCalc.kt`): stride = height × 0.415 (male) or 0.413 (female). km = steps × stride / 100,000. kcal = km × weight × 0.53.

**Food diary** (`feature/food/`): the picker shows Favourites first, then each category. Tapping the heart runs one SQL `UPDATE foods SET isFavourite = ...`. A custom food is saved with `isCustom = true`, so only those show edit and delete buttons. A food log copies the food name and calories, so old diary entries still make sense if a custom food is deleted.

**Sleep** (`feature/sleep/`, `util/SleepCalc.kt`): if the wake time is earlier than the bedtime (22:30 to 06:30), the person slept past midnight, so 24 hours is added.

**Breathing** (`feature/breathing/BreathingScreen.kt`): a `LaunchedEffect` loop counts 4 seconds per phase. `animateFloatAsState` grows and shrinks the circle smoothly.

**Reminders** (`feature/reminders/`): `ReminderTimes` lists the times. `ReminderScheduler` sets one alarm for the next time. When it fires, `ReminderReceiver` shows the notification and sets the next alarm. `BootReceiver` sets alarms again after a phone restart. The breathing notification carries `open_screen = breathing`, and `HealthMateApp` navigates there.

**Screen time pop-up** (`MainActivity.startScreenTimeTimer`): `repeatOnLifecycle(RESUMED)` runs a 1 hour `delay` only while the app is on screen. Leaving the app cancels it, so it really measures continuous use, and no background service is needed. To test it quickly, change `LIMIT_MILLIS` in `ScreenTime` to `60_000L`.

**Dark mode** (`SettingsPrefs.darkMode`): a StateFlow read in `MainActivity`. Changing the switch redraws the whole app with the dark colours.

## 4. Questions you might be asked

- *Why Room and not sqflite?* sqflite is a Flutter package. This app is native Kotlin, and Room is the official way to use SQLite on Android.
- *Why Compose and not XML?* It is Google's recommended UI toolkit now, and each screen is one Kotlin function, so there's less code to follow.
- *Why AndroidViewModel?* It gives the ViewModel the application context to open the database, without needing a dependency injection library.
- *Why a Repository if it just calls the DAO?* It keeps ViewModels separate from the data source, and some rules live there (for example "never lower a manual step count").
- *Why no exact alarms?* Android 12+ needs a special permission for exact alarms. Reminders don't need to be to-the-second, so `setWindow` (within 5 minutes) is enough.
- *What runs in the background?* Only the reminder alarms. The step sensor and screen time timer run only while the app is open.

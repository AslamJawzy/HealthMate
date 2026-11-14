# Testing and issues

## Unit tests (app/src/test)

34 JUnit tests, all passing:

| File | What it checks |
|---|---|
| ValidatorTest | empty fields, non numbers, negatives, too high, future date, bad date, duplicate date, custom food rules |
| DateUtilsTest | week is Monday to Sunday, month is the whole month, leap year, date text conversion |
| CalorieCalcTest | 8,000 steps at 170 cm and 70 kg male = 209 kcal, stride and distance |
| StepCounterLogicTest | first reading, normal counting, phone restart, new day |
| SleepAndReminderTest | sleep past midnight, water reminder every 90 minutes, next reminder today or tomorrow |

Run them in Android Studio: right-click `app/src/test/java` > Run 'Tests in ...'. Take a screenshot of the green results for the report.

## Manual test cases (fill in Actual Result and Status after testing)

| Test ID | Test Scenario | Input | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|
| TC01 | Sample data on first launch | Install and open app | Records tab shows 14 days of records |  |  |
| TC02 | Add a valid record | Date 20 days ago, 7850 steps, 420 kcal, 2000 ml | Record saved and appears in list |  |  |
| TC03 | Empty fields | Leave all fields empty, tap Save | Error under each field, nothing saved |  |  |
| TC04 | Text in a number field | Steps = "abc" | "Steps must be a whole number" |  |  |
| TC05 | Negative value | Water = -200 | "Water cannot be negative" |  |  |
| TC06 | Unrealistic value | Steps = 500000 | "Steps looks too high (max 100000)" |  |  |
| TC07 | Future date | Tomorrow's date typed in | "Date cannot be in the future" |  |  |
| TC08 | Duplicate date | Today's date (already has a record) | "A record for this date already exists" |  |  |
| TC09 | Edit a record | Change water to 2500 on a record | List shows 2500 ml |  |  |
| TC10 | Delete and Undo | Delete a record, tap Undo | Record comes back |  |  |
| TC11 | Search single date | Search date = 3 days ago | Only that day's record is shown |  |  |
| TC12 | Search date range | Range of the last 5 days | 5 records shown, newest first |  |  |
| TC13 | Dashboard week view | Tap Week | Totals and daily averages for Mon to Sun |  |  |
| TC14 | Quick add water | Tap +250 ml | Today's water goes up by 250 |  |  |
| TC15 | Change goals | Step goal 10000, Save | Dashboard bar and chart line use 10000 |  |  |
| TC16 | Dark mode | Switch on | Whole app turns dark |  |  |
| TC17 | Log a food | Kottu, Dinner, 1 portion | Diary shows 800 kcal under Dinner |  |  |
| TC18 | Custom food | "Mum's fish curry", 220 kcal, Curries | Appears under Curries with edit/delete |  |  |
| TC19 | Favourite | Tap heart on Milk tea | Milk tea appears under Favourites |  |  |
| TC20 | Sleep past midnight | Bed 23:00, wake 06:30 | Shows 7h 30m |  |  |
| TC21 | Breathing | Tap Start | Circle grows 4s, holds 4s, shrinks 4s, holds 4s |  |  |
| TC22 | Test notification | Reminders > Send test notification | Notification appears, tapping opens Breathing |  |  |
| TC23 | No step sensor | Open on emulator | Message "No step sensor on this device" and no crash |  |  |
| TC24 | Step counter | Walk 100 steps with a real phone | Dashboard "Live" count goes up about 100 |  |  |

## Design problems handled while building

These came up while writing the code. Add the real errors you hit in Android Studio as well, with screenshots, because the paper asks for problems you actually faced.

1. **sqflite is Flutter only.** The paper names sqflite, but the app is native Kotlin. Fixed by using Room, which is Google's library for SQLite.
2. **Step sensor does not reset at midnight and goes back to 0 after a restart.** Fixed with `StepCounterLogic`, which saves a starting point per day and an offset for restarts.
3. **Sensor could overwrite a number typed by hand.** `saveSensorSteps` only ever raises the value, never lowers it.
4. **Same food in two lists crashed the list.** A favourite food shows in Favourites and in its category, and two list items with the same key crash a LazyColumn. Fixed with keys like `fav_5` and `cat_5`.
5. **Double gap at the top of screens on Android 15.** Target SDK 35 makes apps edge to edge, and both the outer and inner Scaffold added the status bar space. Fixed by passing only bottom padding from the outer Scaffold and `WindowInsets(0, 0, 0, 0)` on inner screens.
6. **Date picker showing the wrong day.** The Material date picker uses UTC milliseconds, so converting with the phone's time zone could shift the date by one. Fixed by converting with UTC in `DateUtils`.
7. **"2026-02-30" was accepted as a date.** Lenient parsing turns it into 28 Feb. Fixed by using the strict ISO date parser.
8. **Exact alarm permission on Android 12+.** Avoided by using `setWindow` (reminder within 5 minutes) instead of exact alarms.
9. **Notifications blocked on Android 13+.** The app asks for the notification permission when a reminder is switched on.
10. **Known limitation:** without a background service, steps walked after midnight but before the app is first opened that day are not counted for the new day.

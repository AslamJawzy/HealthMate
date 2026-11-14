# HealthMate

A personal health and wellness tracker for Android, built with Kotlin and Jetpack Compose for the Mobile Application Development coursework (ESOFT / Kingston University).

## Features

**Core**
- Add, view, edit and delete daily health records (steps, calories burned, water in ml)
- Form validation: empty fields, non numbers, negative or unrealistic values, future dates, duplicate dates
- Delete with Undo
- Search records by a single date or a date range
- Dashboard with today's steps, calories and water and progress towards goals
- 14 days of sample records added on first launch
- Colour coding: steps green, calories red, water blue, food orange, sleep indigo, breathing cyan

**Extra**
- Day / Week / Month view on the dashboard with totals and daily averages
- Weekly bar chart (last 7 days) with a goal line
- Quick add water (+250 ml, +500 ml)
- Daily goals, user profile (height, weight, gender) and dark mode in Settings
- Automatic step counter using the phone's step sensor
- Calories burned estimated from steps and profile
- Sri Lankan food diary with 86 built-in foods, custom foods in any category, favourites, and calories eaten vs burned
- Sleep tracker with history and 7-day average
- Animated box breathing exercise
- Breathing, water and move reminders (notifications)
- Screen time break pop-up after 1 hour of continuous use

## Tech stack

| Part | Choice |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Architecture | MVVM with Repository layer |
| State | ViewModel + StateFlow (and Compose state for form fields) |
| Database | SQLite through Room |
| Settings | SharedPreferences |
| Navigation | Navigation Compose |
| Reminders | AlarmManager + BroadcastReceiver + NotificationCompat |
| Tests | JUnit 4 |

Min SDK 26 (Android 8.0), target SDK 35.

## Project structure

```
app/src/main/java/com/aslam/healthmate/
  MainActivity.kt        app entry, sample data, permissions, screen time timer
  HealthMateApp.kt       navigation and bottom bar
  data/db/               Room entities, DAOs, AppDatabase, sample data, food list
  data/repository/       repositories (the only way ViewModels reach the database)
  data/prefs/            SettingsPrefs (goals, profile, dark mode, reminder switches)
  feature/dashboard/     home screen
  feature/records/       records list, search, add/edit form
  feature/food/          food diary and food picker
  feature/sleep/         sleep tracker
  feature/breathing/     breathing exercise
  feature/settings/      settings
  feature/reminders/     reminders screen, alarms, notifications
  feature/steps/         step sensor
  feature/screentime/    break pop-up
  ui/theme/              colours and theme
  ui/components/         reusable UI (StatCard, BarChart, date pickers)
  util/                  plain Kotlin logic (validation, dates, calories, steps, sleep, reminder times)
app/src/test/            unit tests for everything in util/
```

## How to run

1. Open the project folder in Android Studio (File > Open).
2. Wait for Gradle sync to finish.
3. Pick an emulator or a phone and press Run.

To run the unit tests, right-click `app/src/test/java` and choose **Run 'Tests in ...'**.

Note: the emulator has no step sensor, so the dashboard shows a message instead of live steps. Use a real phone to test the step counter.

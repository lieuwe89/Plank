# Plank Tracker 💪

A minimalist Android app that helps you build a daily plank exercise habit through progressive goals, streak tracking, and daily reminders.

---

## Features

### 🏠 Home Dashboard
- **Animated progress ring** showing today's goal completion at a glance
- **Live stats** — current day streak, personal best duration, and total planks completed
- Quick-start button to jump straight into a plank session
- Tomorrow's target preview with daily increment indicator

### ⏱️ Timer
- Full-screen countdown-style timer with a pulsing progress ring
- **Start / Pause / Resume / Stop** controls
- Real-time "seconds to go" countdown toward today's target
- Visual colour change (green) when the goal is reached
- Save or discard dialog after stopping, with a summary of performance

### 📊 History
- Scrollable list of every logged plank, ordered newest-first
- Each entry shows the date, duration, target, and whether the goal was met
- Green ✓ / red ✗ status indicators with +/– offset from target

### ⚙️ Settings
- **Daily Reminder** — toggle on/off, pick a reminder time; notification taps open the timer directly
- **Progression** — configurable base target (seconds) and daily increment (seconds per streak day)
- **Overview** — summary of current target, increment, streak, and total count
- **Reset** — delete all history and start fresh (with confirmation dialog)

### 🔔 Notifications
- Notification channel created on app startup
- Daily repeating alarm via `AlarmManager`
- Reminder survives device reboots and app updates (via `BootReceiver`)

### 📈 Progressive Goals
The daily target automatically increases with your streak:

```
todayTarget = baseTarget + (streakBeforeToday × dailyIncrement)
```

Default: start at **30 s**, increase by **+5 s** for every consecutive day completed. Both values are fully configurable in Settings.

---

## Screenshots

_Coming soon_

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | **Kotlin** (JVM target 17) |
| UI | **Jetpack Compose** + Material 3 |
| Navigation | **Navigation Compose** (bottom nav bar + timer route) |
| Database | **Room** (SQLite, KSP annotation processor) |
| Preferences | **DataStore Preferences** |
| Async | **Kotlin Coroutines + Flow** |
| Architecture | MVVM — single `PlankViewModel` with `StateFlow<PlankUiState>` |
| Notifications | `AlarmManager` + `NotificationCompat` |
| Theme | Dark-only Material 3 colour scheme |
| Min SDK | **26** (Android 8.0) |
| Target SDK | **35** |

---

## Project Structure

```
app/src/main/java/com/planktracker/
├── MainActivity.kt              # Single Activity, hosts Compose UI
├── PlankApplication.kt          # Application class — creates notification channel
├── data/
│   ├── PlankRecord.kt           # Room @Entity — stores each plank session
│   ├── PlankDao.kt              # Room @Dao — queries & inserts
│   ├── PlankDatabase.kt         # Room database singleton
│   └── PreferencesManager.kt   # DataStore wrapper for settings
├── notification/
│   ├── NotificationHelper.kt   # Channel creation, alarm scheduling, showing notifications
│   ├── ReminderReceiver.kt     # BroadcastReceiver — fires the notification
│   └── BootReceiver.kt         # Re-schedules alarm after reboot / app update
├── ui/
│   ├── Navigation.kt           # NavHost + bottom navigation bar
│   ├── HomeScreen.kt           # Dashboard with progress ring & stats
│   ├── TimerScreen.kt          # Full-screen timer with controls
│   ├── HistoryScreen.kt        # Scrollable list of past sessions
│   ├── SettingsScreen.kt       # Reminder, progression & reset settings
│   └── theme/
│       ├── Color.kt            # Colour palette (green, blue, orange, red, dark surfaces)
│       ├── Theme.kt            # Dark Material 3 colour scheme
│       └── Type.kt             # Typography scale
└── viewmodel/
    └── PlankViewModel.kt       # Business logic, timer, state management
```

---

## Getting Started

### Prerequisites

- **Android Studio** Ladybug or newer (with Kotlin 2.1+)
- **JDK 17**
- Android SDK with Build Tools **35**

### Build & Run

```bash
# Clone the repository
git clone https://github.com/lieuwe89/Plank.git
cd Plank

# Open in Android Studio — it will sync Gradle automatically.
# Or build from the command line:
./gradlew assembleDebug

# Install on a connected device / emulator:
./gradlew installDebug
```

### Pre-built APK

A release APK is included in the repo root for quick side-loading:

```
PlankTrackerv2.apk
```

---

## Architecture Overview

```
┌───────────────────────────────────────────┐
│              UI Layer (Compose)           │
│  HomeScreen  TimerScreen  HistoryScreen   │
│              SettingsScreen               │
└──────────────────┬────────────────────────┘
                   │ collectAsState()
                   ▼
┌───────────────────────────────────────────┐
│        PlankViewModel (MVVM)             │
│  StateFlow<PlankUiState>                 │
│  Timer logic · Streak calc · Settings    │
└─────┬────────────────────────┬────────────┘
      │                        │
      ▼                        ▼
┌─────────────┐     ┌──────────────────┐
│  Room DB    │     │  DataStore Prefs │
│ PlankRecord │     │  reminder time   │
│ PlankDao    │     │  base target     │
│             │     │  daily increment │
└─────────────┘     └──────────────────┘
```

The ViewModel observes all data sources reactively via `combine()` on Flows, producing a single `PlankUiState` that the Compose screens consume. The timer runs as a coroutine job within `viewModelScope`.

---

## Permissions

| Permission | Purpose |
|---|---|
| `POST_NOTIFICATIONS` | Show daily reminder notifications (Android 13+) |
| `RECEIVE_BOOT_COMPLETED` | Re-schedule reminder alarm after reboot |
| `SCHEDULE_EXACT_ALARM` | Schedule daily reminder at the exact chosen time |
| `USE_EXACT_ALARM` | Alternative exact alarm permission (Android 14+) |
| `VIBRATE` | Vibrate on notification |
| `WAKE_LOCK` | Wake device for alarm delivery |

---

## Configuration Defaults

| Setting | Default | Range |
|---|---|---|
| Base target | 30 seconds | 5 – 300 s |
| Daily increment | 5 seconds | 1 – 30 s |
| Reminder time | 08:00 | Any time |
| Reminder enabled | Off | On / Off |

---

## License

This project is provided as-is for personal use. No license has been specified yet.

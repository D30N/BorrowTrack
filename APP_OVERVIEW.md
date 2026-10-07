# BorrowTrack - Application Overview & Technical Documentation

**BorrowTrack** is a lightweight, 100% offline Android IOU (I Owe You) tracking application. It helps users manage items and money lent to or borrowed from others, featuring due-date notifications, WhatsApp reminder sharing, and a Malayalam-first user interface.

---

## 📱 Key Features

- **Lent & Borrowed Tracking**: Quickly record items or money given to (`lent`) or taken from (`borrowed`) specific people.
- **100% Offline & Private**: All data is stored locally on the device using JSON serialization via `SharedPreferences`.
- **Scheduled Due Notifications**: Uses Android `AlarmManager` (`setExactAndAllowWhileIdle`) and `NotificationChannel` to post exact reminders at 9:00 AM on due dates.
- **One-Tap WhatsApp Reminders**: Generate pre-formatted Malayalam reminder messages and launch WhatsApp directly from the entry details.
- **Visual Status Badges**: Dynamic status badges with color coding:
  - `OVERDUE` (Red) - Due date has passed
  - `DUE SOON` (Amber) - Due within 2 days
  - `OK` (Green) - Active entry with time remaining
  - `DONE` (Green) - Item returned / transaction complete
- **Dashboard & Filtering**:
  - **Home**: Filter active entries between "Given" (*കൊടുത്തത്*) and "Received" (*വാങ്ങിയത്*).
  - **Reminders**: View upcoming dues and overdue entries sorted by urgency.
  - **History**: View returned entries with long-press deletion support.
- **Zero-Dependency Lightweight UI**: Built programmatically without heavy UI frameworks or XML layout inflation overhead.

---

## 🏗️ Technical Architecture & Code Structure

The project follows a modular, single-package architecture under `com.deon.borrowtrack`:

```
app/app/src/main/java/com/deon/borrowtrack/
├── Entry.kt             # Data Model & Helper Methods
├── Store.kt             # Data Persistence (SharedPreferences & JSON)
├── Remind.kt            # Notification & Alarm Scheduling Engine
├── Ui.kt                # Programmatic UI Utilities & Design System
├── MainActivity.kt      # Main Dashboard & Navigation
├── AddActivity.kt       # New Entry Creation Screen
├── DetailActivity.kt    # Entry Detail, Action & Sharing Screen
└── ReminderReceiver.kt  # BroadcastReceiver for Alarm Events
```

---

## 📄 File Details

### 1. [`Entry.kt`](file:///C:/Users/donpa/StudioProjects/BorrowTrack/app/app/src/main/java/com/deon/borrowtrack/Entry.kt)
Core data model representing a single transaction:
- **Properties**: `id`, `type` (`"lent"` | `"borrowed"`), `person`, `item`, `dateMs`, `dueMs`, `note`, `returned`, `returnedMs`.
- **Methods**: `daysOverdue()`, `daysLeft()` to calculate day offsets relative to current time.

### 2. [`Store.kt`](file:///C:/Users/donpa/StudioProjects/BorrowTrack/app/app/src/main/java/com/deon/borrowtrack/Store.kt)
Single-object data store handling persistence:
- **Storage Mechanism**: Encapsulates `SharedPreferences` (`"bt"`) storing a JSON array of entries.
- **Operations**: `all()`, `byId()`, `add()`, `markReturned()`, `delete()`.
- **Formatting**: `fmtDate()` provides clean date strings (e.g., `"14 Oct 2026"`).

### 3. [`Remind.kt`](file:///C:/Users/donpa/StudioProjects/BorrowTrack/app/app/src/main/java/com/deon/borrowtrack/Remind.kt) & `ReminderReceiver`
Notification management engine:
- **Channel**: Configures high-priority notification channel `"due_reminders"`.
- **Alarms**: Schedules exact RTC wakeup alarms at 9:00 AM on `dueMs`.
- **Receiver**: `ReminderReceiver` catches broadcast alarms and triggers system notifications with tap-to-open `DetailActivity` intent.

### 4. [`Ui.kt`](file:///C:/Users/donpa/StudioProjects/BorrowTrack/app/app/src/main/java/com/deon/borrowtrack/Ui.kt)
Centralized programmatic UI helper:
- **Design Tokens**: Color definitions (`bg`, `card`, `amber`, `green`, `red`, `grey`).
- **Components**: Dynamic creation of cards, status badges (`statusOf`), sublines (`subLine`), bottom navigation tab buttons (`tabBtn`), and density unit conversions (`dp`).

### 5. [`MainActivity.kt`](file:///C:/Users/donpa/StudioProjects/BorrowTrack/app/app/src/main/java/com/deon/borrowtrack/MainActivity.kt)
The primary user dashboard:
- **Summary Cards**: Displays live counters for total pending lent and borrowed items.
- **Tabs**: `Home`, `Reminders`, `History`.
- **Segment Control**: Switch between lent (*ഞാൻ കൊടുത്തത്*) and borrowed (*ഞാൻ വാങ്ങിയത്*) lists.

### 6. [`AddActivity.kt`](file:///C:/Users/donpa/StudioProjects/BorrowTrack/app/app/src/main/java/com/deon/borrowtrack/AddActivity.kt)
Form screen for creating entries:
- **Form Controls**: Person name, Item description, Transaction Date, Due Date pickers (`DatePickerDialog`), and optional notes.
- **Validation**: Enforces non-empty person and item inputs before saving and scheduling alarm.

### 7. [`DetailActivity.kt`](file:///C:/Users/donpa/StudioProjects/BorrowTrack/app/app/src/main/java/com/deon/borrowtrack/DetailActivity.kt)
Entry inspection and action screen:
- **Status Management**: Button to mark entries as returned (*തിരികെ കിട്ടി ✓*).
- **One-Tap WhatsApp Share**: Automatically generates a friendly Malayalam reminder message and opens WhatsApp share sheet.
- **Deletion**: Options menu to delete entries and cancel pending alarms.

---

## 🛠️ Build & Developer Execution

### Prerequisites
- **Android SDK**: API 34 (Compile SDK 34, Min SDK 26)
- **Java JDK**: JDK 17 or JDK 21

### Windows Build & Run Scripts
The project includes self-contained, automated build scripts for Windows developers:

- **Command Prompt**:
  ```cmd
  win-build.bat
  ```
- **PowerShell**:
  ```powershell
  .\win-build.ps1
  ```

**What the scripts do automatically**:
1. Locates installed JDK (Java 21 / Android Studio JBR) and Android SDK.
2. Auto-generates local configuration files (`local.properties`) and debug signing keystores (`debug.keystore`).
3. Downloads and uses Gradle 8.7.
4. Compiles the debug APK.
5. Auto-detects connected Android devices via ADB, installs the APK, and launches `BorrowTrack`.

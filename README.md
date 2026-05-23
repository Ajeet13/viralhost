# Solar Leads – Android App

An Android app for tracking solar leads. You can call customers directly, log
each call with an outcome, schedule callback reminders, search/filter leads,
import bulk leads from Excel/CSV, and export everything back to CSV.

## Features

- **Lead management** — add, edit, delete leads with the fields you need:
  `Name`, `Phone Number`, `IVRS`, `Address/City`, `Roof Type` (RCC / Tin /
  Other), `System Size (kW)`, plus status and notes.
- **Search & filter** — fuzzy search across name / phone / IVRS / address, plus
  filter chips for status and roof type.
- **One-tap calling** — direct dial (with `CALL_PHONE` permission) or fall back
  to the system dialer; every call is auto-logged in the lead's call history.
- **Call outcomes** — after each call pick from: Connected, Not Interested,
  No Answer, Wrong Number, Callback Later, Site Visit Booked, Quoted,
  Converted. The lead's status is updated accordingly.
- **Callback reminders** — schedule date/time per lead; a notification fires
  via WorkManager when it's due (tap it to jump straight to the lead).
- **Today's Callbacks screen** — bottom-tab view that lists overdue and
  due-today reminders across all leads, with quick call/done/delete actions.
- **WhatsApp / SMS templates** — manage reusable message templates with
  placeholders like `{name}`, `{address}`, `{size}`. Send single messages
  from a lead row, or **bulk send** to many leads at once via multi-select.
- **Analytics dashboard** — pipeline funnel by status, roof-type breakdown,
  conversion rate, and a 7-day calls-per-day bar chart.
- **Excel / CSV import** — pick a `.xlsx`, `.xls`, or `.csv` from your phone
  and bulk-create leads. Header row drives column mapping.
- **CSV export** — export all leads, then share via email / Drive / etc.
- **Solar-themed app icon** — adaptive icon with a sun + house + roof panel design.

## Lead Statuses

`New`, `Interested`, `Not Interested`, `Callback Scheduled`,
`Site Visit Booked`, `Quoted`, `Converted`, `Lost`.

## Tech stack

| Layer | Library |
|---|---|
| Language | Kotlin 1.9 |
| UI | Jetpack Compose (Material 3) |
| Navigation | Navigation Compose |
| State | ViewModel + StateFlow |
| Persistence | Room (SQLite) |
| Background work | WorkManager |
| Excel | Apache POI 5.x |
| CSV | OpenCSV 5.x |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 34 |

## Project layout

```
app/src/main/java/com/viralhost/solarleads/
├── SolarLeadsApp.kt           # Application class, notification channel
├── data/
│   ├── AppDatabase.kt         # Room DB
│   ├── Converters.kt
│   ├── dao/                   # LeadDao, CallLogDao, ReminderDao
│   ├── model/                 # Lead, CallLog, Reminder, LeadStatus, RoofType
│   └── repository/LeadRepository.kt
├── reminders/
│   ├── ReminderScheduler.kt   # WorkManager scheduling
│   └── ReminderWorker.kt      # Notification on trigger
├── ui/
│   ├── MainActivity.kt
│   ├── nav/SolarLeadsNavHost.kt
│   ├── theme/Theme.kt
│   ├── list/                  # Lead list + filters + export
│   ├── edit/                  # Add / edit form
│   ├── detail/                # Detail + call + reminders + outcome
│   └── import_/               # Excel/CSV import
└── util/
    ├── CallUtils.kt
    ├── CsvExporter.kt
    └── ExcelImporter.kt
```

## Building

This project uses the Gradle Kotlin DSL with KSP for Room.

### Option A — Android Studio (recommended)

1. Open the `viralhost` folder in **Android Studio** (Hedgehog or newer).
2. Studio will sync, download dependencies, and generate `gradle/wrapper/gradle-wrapper.jar`.
3. Press **Run** to build and install the app on a device/emulator.

### Option B — Command line

You'll need a local Gradle installation (8.7+) the first time so the wrapper
jar can be generated:

```bash
gradle wrapper                # one-time, only if gradle-wrapper.jar is missing
./gradlew assembleDebug        # build debug APK
```

The APK lands in `app/build/outputs/apk/debug/`.

## Permissions

Granted on install (no runtime prompt needed):
- `INTERNET` — none required; app is fully offline today.

Asked for at runtime:
- `CALL_PHONE` — when you tap the **Call** button. If denied, the system
  dialer opens with the number pre-filled.
- `POST_NOTIFICATIONS` (Android 13+) — needed for callback reminders.
- `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM` — for accurate callback firing.

## Excel / CSV import format

The first row must be a header row. Column order doesn't matter; matching is
case-insensitive. Recognised headers:

| Field | Accepted headers |
|---|---|
| Name (required) | `Name`, `Customer Name`, `Full Name` |
| Phone (required) | `Phone`, `Phone Number`, `Mobile`, `Contact` |
| IVRS | `IVRS`, `IVR` |
| Address | `Address`, `City`, `Address/City`, `Location` |
| Roof Type | `Roof`, `Roof Type`, `Rooftop` |
| System Size | `Size`, `System Size`, `kW`, `System Size (kW)` |

A ready-to-use sample file lives at [`sample_leads_template.csv`](sample_leads_template.csv).

You can also save it as `.xlsx` from Excel/Google Sheets and import that
directly.

## Roadmap ideas

- Cloud sync (Firebase / REST backend)
- User accounts + role-based access
- WhatsApp / SMS quick-templates
- Lead source tracking and analytics dashboard
- Bulk-edit and bulk-delete

## License

Internal project — license TBD.

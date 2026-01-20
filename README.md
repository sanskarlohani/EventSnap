# EventSnap

EventSnap is a Jetpack Compose Android app that lets users view public holidays (via API) and create their own events (stored in Firebase).

## Features

- View events from:
  - Public Holidays API (US holidays)
  - User-created events (Firebase Cloud Firestore)
- Add events with title, date, and optional notes
- View event details and delete user-created events

## Firebase integration

- **Firebase Analytics**: logs events including:
  - `app_open`
  - `event_added`
  - `event_viewed`
  - `screen_view`
- **Firebase Crashlytics**: enabled for crash reporting
- **Cloud Firestore**: user-created events are stored under each user
- **Firebase Auth**: required to scope events per user (`users/{uid}/events/...`)

## Tech stack

- Kotlin
- Jetpack Compose + Material 3
- MVVM + Repository
- Retrofit + OkHttp
- Firebase (Analytics, Crashlytics, Auth, Firestore, Remote Config)

## Project structure

```
app/src/main/java/com/sanskar/eventsnap/
├── data/
│   ├── model/
│   ├── remote/
│   └── repository/
├── service/
├── ui/
│   ├── components/
│   ├── navigation/
│   ├── screens/
│   ├── theme/
│   └── viewmodel/
├── util/
├── EventSnapApplication.kt
└── MainActivity.kt
```

## Setup

### Prerequisites

- Android Studio (recent stable)
- JDK 11+
- Android device/emulator with API 26+

### Firebase

See `FIREBASE_SETUP.md`.

Quick checklist:
1. Create a Firebase project.
2. Add Android app with package name: `com.sanskar.eventsnap`.
3. Download `google-services.json` and place it in `app/`.
4. Enable:
   - Firestore Database
   - Analytics
   - Crashlytics
   - Authentication (Email/Password recommended)

### Build & Run

1. Open the project in Android Studio
2. Sync Gradle
3. Run on a device/emulator

## Firebase Analytics events

| Event name | Trigger |
|-----------|---------|
| `app_open` | Logged when app opens (HomeViewModel init) |
| `event_added` | Logged when a user successfully adds an event |
| `event_viewed` | Logged when a user opens event details |
| `screen_view` | Logged on key screens |

> Note: In Firebase Console, Analytics events can take time to appear in **Analytics → Events**.
> For near real-time debugging, use **Analytics → DebugView**.

## Firestore database structure

User-created events are stored per-user:

```
users/
  └── {uid}/
      └── events/
          └── {eventId}
              ├── title: string
              ├── date: string (YYYY-MM-DD)
              ├── description: string
              ├── notes: string
              ├── source: string ("USER_CREATED")
              ├── createdByUid: string
              └── createdByName: string
```

## Remote Config

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `show_add_event_button` | Boolean | `true` | Toggle visibility of Add Event FAB |


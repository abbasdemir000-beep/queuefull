# QueueFuel — دور البنزين ⛽

QueueFuel is a native Android app that helps drivers in **Kirkuk, Erbil, and
Sulaymaniyah** avoid crowded fuel stations. Users report and confirm the live
queue ("السرا") status of nearby stations, and the crowd-sourced data is shown
on an interactive map and station list in real time. The UI is Arabic (RTL).

> The app is fully self-contained and runs offline against a seeded local
> database. An optional Firebase Realtime Database sync can be enabled from the
> admin panel.

## Features

- **Phone + OTP login** (simulated OTP for the MVP — see [Test credentials](#test-credentials)).
- **Interactive map** drawn with Compose `Canvas`, plotting stations colour-coded
  by queue status, plus a simulated GPS position.
- **Live station list** with search and filters (status, government/private
  ownership, fuel type).
- **Status reporting & confirmation** gated by a geofence (you must be within
  ~200 m of a station, simulated via the GPS controller) to deter false reports.
- **Reliability points & a 5-hour competition cycle** — users earn points for
  reporting/confirming; points reset every 5 hours and the most active
  contributors are surfaced for rewards.
- **Suggestions** — users can propose new stations or cities, with automatic
  duplicate detection within 100 m.
- **Admin panel** — approve/reject/merge station and city suggestions, ban
  users, manage the reward cycle, and configure Firebase sync.
- **Local notifications log** for nearby updates, expiry events, and cycle alerts.

## Tech stack

- **Kotlin** + **Jetpack Compose** (Material 3)
- **MVVM** with an `AndroidViewModel` and a pure domain layer
- **Room** for local persistence (KSP-generated)
- **OkHttp / Retrofit / Moshi** for the optional Firebase REST sync
- **Robolectric** + **Roborazzi** for unit and screenshot tests
- Min SDK 24, target/compile SDK 36

## Architecture

The code is organised into clean layers under `app/src/main/java/com/example/`:

| Layer | Location | Responsibility |
|-------|----------|----------------|
| `domain/model` | data classes (`Station`, `City`, `AppUser`, …) | Framework-agnostic entities |
| `domain/repository` | `QueueFuelRepository` | Repository interface |
| `domain/usecase` | `AuthPolicy`, `CyclePolicy`, `GeoProximity`, `PointsPolicy`, `StatusExpiryPolicy` | Pure, unit-tested business rules |
| `data/local` | Room database, DAO, entities, mappers | Persistence |
| `data/repository` | `QueueFuelRepositoryImpl` | Room-backed repository + first-run seeding |
| `ui` | `QueueFuelApp`, `QueueFuelViewModel`, `theme/` | Compose screens + state |

The `feature/` and `core/` packages are placeholders reserved for a future
multi-module split; they are currently empty.

## Build & run

**Prerequisites:** [Android Studio](https://developer.android.com/studio) (latest
stable) with JDK 17+ and the Android SDK for API 36.

1. Open the project in Android Studio and let it sync Gradle.
2. The debug build is configured to sign with a checked-in debug keystore.
   The keystore is stored base64-encoded as `debug.keystore.base64` (the raw
   `debug.keystore` is git-ignored). Generate it once before building:

   ```bash
   base64 -d debug.keystore.base64 > debug.keystore
   ```

   Alternatively, remove the `signingConfig = signingConfigs.getByName("debugConfig")`
   line from the `debug { }` block in `app/build.gradle.kts` to use the default
   debug signing.
3. Run the `app` configuration on an emulator or device (API 24+).

No API keys are required to run the app. (The `.env.example` /
`GEMINI_API_KEY` and `google-services.json` are inherited from the project
template and are **not** used by the current code.)

### Test credentials

The OTP flow is simulated for the MVP:

- **OTP:** the generated code is shown on screen; the backdoor code **`1234`**
  is always accepted.
- **Admin account:** log in with phone **`07774564334`** (any name) to get the
  `ADMIN` role and see the admin panel. Any other 10+ digit phone logs in as a
  regular `USER`.
- **Geofence:** the **"محاكاة GPS"** toggle on the map screen lets you snap your
  simulated location to a city centre so you can report/confirm statuses without
  being physically near a station. Disabling it bypasses the proximity check
  entirely (developer mode).

You can also switch roles instantly (USER / REPORTER / ADMIN) from the profile
screen for testing.

## Tests

Unit and screenshot tests live in `app/src/test/`:

```bash
gradle :app:testDebugUnitTest
```

- Use-case logic: `AuthPolicyTest`, `PointsPolicyTest`, `CyclePolicyTest`,
  `StatusExpiryPolicyTest`, `GeoProximityTest`
- Compose screenshot baseline: `GreetingScreenshotTest` (Roborazzi)

## Optional: Firebase cloud sync

From **Admin panel → سحاب الفايربيس**, paste a Firebase Realtime Database URL
(and optional auth token) and enable sync. The app then PUTs all users,
stations, cities, and queue updates to `…/queue_fuel_data.json` whenever the
local data changes (debounced). This is entirely optional — the app works fully
offline without it.

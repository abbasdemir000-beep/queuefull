# CLAUDE.md

Guidance for working in this repository.

## What this is

QueueFuel (دور البنزين) — a native Android app (Kotlin + Jetpack Compose,
Material 3) for crowd-sourced fuel-station queue tracking in Kirkuk, Erbil, and
Sulaymaniyah. The UI is Arabic / RTL. See `README.md` for the user-facing
overview.

## Build & test

```bash
gradle :app:testDebugUnitTest      # unit + Roborazzi screenshot tests
gradle :app:assembleDebug          # build the debug APK
```

Toolchain: JDK 17+, Android SDK API 36, min SDK 24. The debug build signs with a
keystore decoded from `debug.keystore.base64` (`base64 -d debug.keystore.base64 >
debug.keystore`), or drop the `debugConfig` signing line in
`app/build.gradle.kts`.

Note: building requires network access to download the Android Gradle Plugin and
dependencies. In a restricted/offline sandbox the build will fail at plugin
resolution — reason from source and rely on CI to verify compilation.

## Architecture

Clean-ish layering under `app/src/main/java/com/example/`:

- `domain/model` — entities (`Station`, `City`, `AppUser`, `QueueUpdate`, `AdBanner`).
- `domain/repository` — `QueueFuelRepository` interface.
- `domain/usecase` — **pure, framework-free, unit-tested** rules. Put new
  business logic here, not in the ViewModel: `AuthPolicy`, `CyclePolicy`,
  `GeoProximity`, `PointsPolicy`, `StatusExpiryPolicy`.
- `data/local` — Room DB, DAO, entities, and entity↔domain `Mappers.kt`.
- `data/repository` — `QueueFuelRepositoryImpl` (Room-backed; seeds the DB on
  first launch).
- `ui` — `QueueFuelApp.kt` (all Compose screens) and `QueueFuelViewModel.kt`
  (state holder); theme in `ui/theme/`, reusable design-system components in
  `ui/components/`. The visual spec is `docs/DESIGN_SYSTEM.md` — use its
  tokens (`Qf*` colors, Tajawal type scale); never introduce ad-hoc colors.

`feature/` and `core/` are empty placeholders for a planned multi-module split.

## Conventions

- The ViewModel exposes Compose `mutableStateOf` properties and `StateFlow`s
  derived from the repository. It holds side effects (prefs, DB writes,
  notifications); keep decision logic in `domain/usecase` so it stays testable.
- The VM must stay free of `android.widget.Toast` — emit messages on
  `toastEvent` (a `SharedFlow`) and let the UI show them.
- New use-case logic should ship with a matching test in
  `app/src/test/java/com/example/domain/usecase/`.
- Recent history is a "phase0" refactor extracting the domain layer; preserve
  behaviour when refactoring and keep commits focused.

## MVP shortcuts to be aware of

- Login is a direct profile registration (name + phone + city) — no OTP, no
  SMS, no Firebase Phone Auth. Users are saved with `phoneVerified = false`.
- Admin is a hardcoded phone: `AuthPolicy.ADMIN_PHONE` = `07774564334`.
- GPS is simulated via the map screen toggle; proximity is a 200 m geofence
  (`GeoProximity`).
- Reports require a photo and run through `ReportVerifier`
  (`domain/usecase/ReportVerification.kt`). The MVP uses `StubReportVerifier`
  (deterministic heuristics); a real AI vision verifier should implement the
  same interface. Only `VERIFIED` reports update the station, award points,
  and create a raffle `RewardEntry`.
- `.env`/`GEMINI_API_KEY` and `google-services.json` are template leftovers and
  are not used by the code.

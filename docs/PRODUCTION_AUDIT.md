# QueueFuel — Production Readiness Audit

Date: 2026-06-11 · Scope: full repository (architecture, data, UI, CI/CD,
security, testing, release engineering).

## Fixed in this audit branch

| # | Area | Finding | Fix |
|---|------|---------|-----|
| 1 | Security/correctness | Cloud-sync JSON was built by string interpolation — any `"`/`\`/newline in a user or station name produced invalid (and injectable) JSON | `CloudSyncPayload` pure builder with proper escaping + unit tests |
| 2 | Correctness | `loadOrCreateCycle` contained dead code (`if (start != now)` right after `start = now`) — points were never reset when a cycle expired while the app was closed | Expiry detected before the start time is overwritten |
| 3 | Correctness | Background loops (`expireOldUpdates`, cycle resets, cloud sync) read `StateFlow.value` of `WhileSubscribed` streams — empty whenever no screen is subscribed, so they silently no-oped | Read `repository.*.first()` directly |
| 4 | Correctness | Badges compared `lifetimePoints` (15 pts/report) against thresholds defined in *verified contributions* — badges unlocked ~15× too early | Count verified `reward_entries` per user (new DAO query); manual admin approvals also award badges now |
| 5 | i18n | Cycle countdown used default-locale `String.format` — renders Eastern-Arabic digits on Arabic devices | `Locale.ROOT` |
| 6 | Validation | `isValidPhone` accepted any ≥10 chars, including letters | digits-only + trim (unit-tested) |
| 7 | Consistency | Repository awarded a literal `5` for confirmations | `PointsPolicy.CONFIRMATION_POINTS` |
| 8 | Networking | New `OkHttpClient` per sync, no timeouts, leaked response body; `http://` URLs allowed | Shared client with timeouts, `response.use`, https enforced |
| 9 | Security | `allowBackup=true` let a restored/edited backup forge points or admin state | `allowBackup=false` |
| 10 | Release | No R8/resource shrinking; fixed `versionCode=1`; no AAB output | Minify+shrink enabled, env-driven versioning, `bundleRelease` in CI |
| 11 | CI | Release keystore passwords hardcoded in workflow YAML | GitHub-secrets-first with documented placeholder fallback; production keystore via `RELEASE_KEYSTORE_B64` |
| 12 | Build health | Unused deps (retrofit, moshi+codegen, logging-interceptor) inflated build time and APK | Commented out per repo convention |
| 13 | Code quality | Duplicated Haversine in UI, `_text_wrap` naming, shadowed `mutableStateListOf` extension | Delegated to `GeoProximity`, renamed, removed |
| 14 | CI | No lint signal | `lintDebug` step + HTML report artifact (non-blocking until a baseline exists) |

## Known MVP shortcuts (accepted, documented in CLAUDE.md)

- No real auth: profile registration without OTP; admin is a hardcoded phone.
- GPS is simulated; proximity is a 200 m geofence.
- `StubReportVerifier` (heuristics) stands in for AI photo verification.
- Cloud sync is a one-way RTDB REST PUT configured by the admin at runtime;
  there is no real backend, no multi-device merge, no conflict resolution.
- `fallbackToDestructiveMigration()` wipes local data if a migration is missing.

## Remaining gaps requiring external resources (cannot be fixed repo-locally)

1. **Real backend** (Firebase/Supabase/custom): two-way sync, server-side
   points, server-enforced admin role. The current local-first model is
   fundamentally tamper-prone.
2. **Phone verification** (Firebase Phone Auth or SMS provider).
3. **AI photo verification** (vision-model API key) replacing the stub via the
   existing `ReportVerifier` interface.
4. **Real map tiles** (Google Maps SDK key). Real GPS is already integrated:
   FusedLocationProvider with a runtime-permission flow; a fresh fix overrides
   the simulation and always enforces the report geofence (`LocationPolicy`).
5. **Push notifications** (FCM; current "notifications" are an in-app log).
6. **Production signing**: the committed `release.keystore` (password
   `android123`) is public and must never sign a Play upload. Generate a
   private keystore and set `RELEASE_KEYSTORE_B64`, `RELEASE_STORE_PASSWORD`,
   `RELEASE_KEY_PASSWORD` secrets.
7. **Play Console account**, privacy policy URL, data-safety form.
8. **Crash reporting** (Crashlytics/Sentry — needs account).

## Readiness scores (post-fixes)

- Architecture: **80%** — clean domain/data/ui layering, pure tested policies;
  missing DI framework and the planned `feature/`/`core/` modularization.
- Android release engineering: **75%** — R8, shrinking, AAB, versioning, CI
  artifacts in place; blocked on real signing + Play account.
- Security: **45%** — local hardening done; trust model remains client-side
  until a backend exists (admin role, points, bans are all local data).
- Performance: **85%** — small local DB, flows, R8; no known hot spots.
- Maintainability: **75%** — tested domain layer, design-system components;
  `QueueFuelApp.kt` (~2k lines) and the ViewModel should keep shrinking.
- Product completeness vs. "كل الطوابير بمكان واحد": **35%** — fuel queues are
  functional locally; other categories are placeholders; no real-time sharing
  between users without a backend.

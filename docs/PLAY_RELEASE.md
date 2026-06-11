# Google Play release readiness

Checklist for the first Play Store upload. Engineering items that are
already done are marked ✅; everything ⬜ needs Abbas (account, content, or a
decision).

## Privacy policy (required before listing)

⬜ Host a privacy policy at a public URL (GitHub Pages is free and enough).
It must cover, truthfully, what the app does today:

- Data collected: self-declared name, phone number (unverified), city;
  device location while reporting/confirming (geofence check, sent with
  reports); report photos; anonymous device identifier (Firebase uid);
  FCM push token. No advertising ID, no contacts, no background location.
- Purpose: crowd-sourced queue statuses, anti-abuse verification, points
  and raffle, push notifications.
- Storage: locally on device; on Firebase (Firestore/Storage, Google Cloud)
  once the backend is live.
- Deletion contact: an email address users can write to.
- Arabic version recommended (the audience is Arabic-speaking).

## Data safety form (Play Console)

Declare when the backend is live:

| Data | Collected | Shared | Purpose |
|------|-----------|--------|---------|
| Name, phone, city | Yes | No | App functionality (profile, raffle) |
| Precise location | Yes (foreground only, while reporting) | No | App functionality (geofence) |
| Photos | Yes (report photos) | No | App functionality (verification) |
| Device/other IDs | Yes (anonymous uid, FCM token) | No | App functionality |

- Encryption in transit: yes (Firebase TLS). ⬜ Offer deletion on request.
- If AdMob is enabled later, the form must be redone (ads SDK adds
  advertising-ID collection) — see MONETIZATION.md.

## Release checklist

- ✅ AAB build (`bundleRelease`), R8 + resource shrinking, monotonic
  `versionCode` from CI.
- ✅ `allowBackup=false`, lint in CI, unit + screenshot tests.
- ⬜ **Private upload keystore** — the committed `release.keystore`
  (password `android123`) is public and must never sign a Play build.
  Generate privately, set `RELEASE_KEYSTORE_B64`, `RELEASE_STORE_PASSWORD`,
  `RELEASE_KEY_PASSWORD` GitHub secrets (CI already prefers them), and
  enroll in Play App Signing.
- ⬜ Play Console developer account ($25 one-time), app created with package
  `com.aistudio.queuefuel.xtynvw`.
- ⬜ Store listing: Arabic title/description, screenshots (Roborazzi
  captures in CI artifacts are a starting point), 512 px icon, feature
  graphic.
- ⬜ Content rating questionnaire (no objectionable content → Everyone).
- ⬜ App access notes for review: the app is usable without credentials;
  mention that admin features require a server-assigned role.
- ⬜ Decide target countries (Iraq) and closed-testing track first
  (Play now requires a testing phase for new personal accounts —
  check current requirements when creating the account).
- ⬜ Crash reporting (Crashlytics is free; one dependency + the existing
  google-services.json) — recommended before public release.

## Suggested order

1. Real Firebase project + backend deploy (see BACKEND.md) — the app is
   honest about its data flows only after that.
2. Keystore + Play account + privacy policy.
3. Closed testing with real users in Kirkuk.
4. Production rollout.

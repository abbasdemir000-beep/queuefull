# QueueFuel backend architecture (Firestore + Cloud Functions + FCM)

Status: **implemented repo-locally, awaiting a real Firebase project**.
The deployable backend lives in [`backend/`](../backend/README.md); the app
ships a gated client (`FirebaseBackendGateway`) that stays dormant until a
real `google-services.json` replaces the committed placeholder.

## Why Firebase

| Criterion | Verdict |
|-----------|---------|
| Speed of launch | One console project + `firebase deploy` — no servers, no ops |
| Cost | Free tier covers MVP city-scale load (estimate below) |
| Scalability | Firestore/Functions scale automatically; topics fan out FCM |
| Maintenance | ~600 lines of TypeScript total; no infrastructure to patch |

A custom backend was rejected: it would add weeks and a monthly bill before
the first real user.

## Identity (strict rule: no OTP)

- **Firebase Anonymous Auth** — one uid per device/install. Free, zero
  friction, no SMS/OTP/Twilio/Phone-Auth anywhere.
- The profile (name, phone, city) is self-declared and stored with
  `phoneVerified = false`. The phone is a display/contact field only.
- Limitation (accepted): uninstalling loses the identity; one person can
  create several identities. The trust system, not identity, defends the
  points economy. Account linking (Google sign-in) is a later upgrade that
  drops in without schema changes.

## Firestore data model

```
users/{uid}            name, phone, phoneVerified=false, city, fcmToken,
                       createdAt, updatedAt                       ← client-writable
                       role (USER|REPORTER|ADMIN), points, lifetimePoints,
                       trustScore (0–100), banned, flagged        ← server-only
stations/{stationId}   id (int, mirrors Room id), cityId, cityName, name,
                       latitude, longitude, type, fuelTypes, queueStatus,
                       hasFuel, confirmedCount, lastUpdated, isApproved,
                       suggestedBy                                ← admin/server-only
cities/{cityId}        id (int), nameAr, nameEn, isApproved       ← admin/server-only
banners/{bannerId}     title, description, city, category, ctaPhone, imageUrl
reports/{autoId}       uid, userPhone, stationId, queueStatus, hasFuel,
                       fuelType, latitude, longitude, photoPath (Storage),
                       photoSha256, createdAt, verification       ← client appends PENDING
                       verificationNote, confidence, verifiedAt   ← server verdict
rewardEntries/{autoId} uid, stationId, reportId, monthYear, verified, createdAt  ← server-only
photoHashes/{sha256}   uid, reportId, createdAt                   ← server-only dedupe ledger
```

Station documents carry an integer `id` field equal to the Room primary key
so live snapshots can be mirrored into the local DB (`insertStation` is a
REPLACE upsert). Transitional but pragmatic; a string-id migration can come
with the multi-module split.

## Sync design (v1, deliberately simple)

- **Down:** the client listens to `stations` snapshots and upserts them into
  Room. Every screen keeps reading the same local flows — offline behaviour
  is unchanged, and with no backend configured nothing happens at all.
- **Up:** the only client write that matters is appending a `PENDING`
  report (plus its photo to Storage). The client never writes station
  status, points, roles, or raffle entries.
- The legacy admin-configured RTDB REST sync remains untouched as the
  offline-demo path.

## Server-side roles & points

- `onReportCreated` is the single authority: anti-spam checks → verification
  → one transaction that sets the verdict, updates the station, awards
  `PointsPolicy.REPORT_POINTS`, adjusts trust, and creates the raffle entry.
- `confirmStatus` (callable) is the geofenced +5 confirmation.
- `setUserRole` (callable) is ADMIN-only. **There is no hardcoded admin**:
  the first admin is bootstrapped once by setting `users/{uid}.role =
  "ADMIN"` in the Firebase console; rules prevent any client from touching
  `role`. The app applies the server role via `AuthPolicy.effectiveRole`
  (hardcoded phone = offline-demo fallback only).
- `reviewReport` (callable) lets an admin resolve reports held `PENDING` for
  low-trust users.

## Anti-spam / trust (server-authoritative, client-mirrored)

| Rule | Value | Enforced |
|------|-------|----------|
| Per-station cooldown | 10 min | client (UX) + function |
| Global cooldown | 2 min | client (UX) + function |
| Daily report budget | 30/24 h | function (flags the account) |
| Duplicate photo | SHA-256 ledger `photoHashes` | client (session) + function (durable) |
| Trust score | 0–100, start 50; +2 verified, −8 rejected, −15 duplicate, +1 confirm | function |
| Suspicious (< 25) | reports held PENDING for admin review | function |
| Ban review (< 10) | account `flagged` for admin decision | function |

Constants live twice by design: `domain/usecase/{AntiSpamPolicy,TrustPolicy}.kt`
(client UX) and `backend/functions/src/policies.ts` (**authoritative**).

## FCM contracts

- Token: stored at `users/{uid}.fcmToken` (registered at sign-in, rotated by
  `QueueFuelMessagingService.onNewToken`).
- Topics: the app subscribes to `city_{cityId}` for the selected city.
- Message: notification title/body in Arabic + data payload
  `{ type: "station_update", stationId, queueStatus }` — the data shape is
  the contract for deep links later.
- Android 13+ needs the `POST_NOTIFICATIONS` runtime grant; the permission
  is declared, the request dialog ships with the first real-backend release.

## AI photo verification

The interface exists on both sides and the swap is a one-liner:

- App: `ReportVerifier` (`domain/usecase/ReportVerification.kt`) — local UX
  verdict only.
- Server (**authoritative**): `ReportVerifier` in
  `backend/functions/src/verifier.ts`; `activeVerifier` is currently the
  heuristic implementation (photo present + geofence + valid status).

Contract for the real implementation: input = Storage photo path + station
coordinates/context + claimed status; output = `{verdict, reason,
confidence}`. The vision API key goes into Cloud Functions secrets
(`firebase functions:secrets:set VISION_API_KEY`) — never in this repo or in
the app. Vision pricing/quotas should be checked at integration time;
verification stays server-side so the key never reaches devices.

## Maps foundation (no paid keys)

Current map = custom Compose Canvas (works offline, zero cost). Decision for
real maps, in order of preference:

1. **osmdroid (OpenStreetMap)** — free, no API key, no billing account; good
   enough for "colored station pins in three cities". Recommended next step.
2. Google Maps SDK — better polish, requires a billing-enabled key
   (free $200/month credit) via the already-configured secrets plugin
   (`MAPS_API_KEY` in `.env`).

Either way the change is confined to `ui/screens/MapScreen.kt`; station data
and `GeoProximity` are already map-agnostic. No abstraction layer was added
now — one implementation needs no indirection.

## Cost estimate (MVP, ~1–5 k users in 3 cities)

- Anonymous Auth, FCM, security rules: **free, unlimited**.
- Firestore free tier: 50 k reads / 20 k writes per day. Dominant load is
  station snapshot reads; ~9 stations × a few snapshots/session keeps even
  thousands of daily sessions inside the tier.
- Functions free tier: 2 M invocations/month — reports are the only trigger.
- Storage free tier: 5 GB — at ~200 KB/photo that's ~25 k photos; add a
  30-day lifecycle-delete rule on `reports/` at deploy time.
- **Expected bill: $0/month** during MVP (Blaze plan required for Functions
  but usage stays inside the free allowances).

## What Abbas must do (in order)

1. Create the Firebase project, enable Anonymous Auth + Firestore + Storage
   + FCM, upgrade to Blaze — see `backend/README.md`.
2. `firebase deploy` rules + functions (runbook in `backend/README.md`).
3. Download the real `google-services.json` (package
   `com.aistudio.queuefuel.xtynvw`) and replace `app/google-services.json`.
4. Run the app once, grab his uid, set `users/{uid}.role = "ADMIN"` in the
   console.
5. Seed `stations` + `cities` (copy the Room seed; an import script or the
   admin panel can follow).

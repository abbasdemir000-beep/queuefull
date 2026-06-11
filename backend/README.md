# QueueFuel backend (Firebase)

Firestore + Cloud Functions + Storage + FCM. This directory is fully
deployable — it only needs a real Firebase project. The full data model,
trust model, and architecture rationale live in [`../docs/BACKEND.md`](../docs/BACKEND.md).

## Layout

| Path | Purpose |
|------|---------|
| `firebase.json` | Service wiring + local emulator ports |
| `.firebaserc` | Project alias — replace `queuefuel-placeholder` with the real project id |
| `firestore.rules` | Security rules: clients append reports + edit own profile; everything valuable is server-written |
| `firestore.indexes.json` | Composite indexes for the anti-spam queries |
| `storage.rules` | Report photos: owner-only, image-only, < 5 MB |
| `functions/src/policies.ts` | Authoritative mirrors of the app's domain policies |
| `functions/src/verifier.ts` | `ReportVerifier` interface + heuristic default (AI slot) |
| `functions/src/index.ts` | `onReportCreated`, `confirmStatus`, `setUserRole`, `reviewReport` |

## Deploy (one-time setup ~15 minutes)

```bash
npm install -g firebase-tools
firebase login

# 1. Create a Firebase project (e.g. "queuefuel-prod") at console.firebase.google.com
#    Enable: Authentication → Anonymous, Firestore (production mode),
#    Storage, Cloud Messaging. Functions require the Blaze plan
#    (pay-as-you-go; this workload stays inside the free allowance).

# 2. Point this directory at the project
cd backend
firebase use --add        # pick the project, alias "default"

# 3. Deploy everything
cd functions && npm install && cd ..
firebase deploy --only firestore:rules,firestore:indexes,storage,functions

# 4. Bootstrap the first admin (one time, Firebase console):
#    Firestore → users → {Abbas' uid} → set field role = "ADMIN"
#    (Get the uid from the app's account screen or the Auth user list.)

# 5. Drop the real google-services.json into ../app/ (package name
#    com.aistudio.queuefuel.xtynvw) and rebuild the app.
```

## Local emulator

```bash
cd backend/functions && npm run serve
```

## Cost

Anonymous Auth, FCM, and rules are free. Firestore/Functions/Storage free
tiers comfortably cover an MVP city-scale load (see docs/BACKEND.md § Cost).
No secrets are stored in this repo; a future AI verifier key goes into
Cloud Functions secrets (`firebase functions:secrets:set VISION_API_KEY`).

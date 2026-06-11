# iOS readiness — planning only

No iOS work ships in this milestone; this records the decision path so
Android choices keep the door open.

## Recommendation: Kotlin Multiplatform (KMP), later

- The repo's discipline already fits KMP: `domain/model`, `domain/usecase`,
  and `domain/repository` are pure Kotlin with no Android imports — exactly
  the code that would move into a shared module during the planned
  `feature/`/`core/` split.
- Firebase has official iOS SDKs; the Firestore data model, security rules,
  Cloud Functions, and FCM topics in `backend/` are platform-neutral and
  need zero changes for iOS.
- UI would be native SwiftUI (or Compose Multiplatform when the team is
  comfortable); the Arabic/RTL design tokens in docs/DESIGN_SYSTEM.md map
  cleanly.

## Why not now

- iOS demands a Mac + Apple Developer account ($99/year) and roughly
  doubles release surface while the Android MVP still has zero real users.
- Decision trigger: revisit after Android reaches sustained real usage
  (e.g. >1 k MAU) or a concrete iOS demand signal.

## Keep-the-door-open rules (cost ≈ 0 today)

1. New business logic goes in `domain/usecase` (already the convention).
2. No Android types in domain interfaces (`BackendGateway` already complies).
3. Server stays authoritative for anything valuable — an iOS client then
   only needs read mirrors + report submission, same as Android.

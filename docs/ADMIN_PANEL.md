# Admin web panel — roadmap & API contracts

Goal: move admin work (approvals, bans, roles, banners) off the phone and
onto a web panel, without building a custom API server.

## Architecture decision

**Firebase Hosting + a small web app talking directly to Firestore** with
the existing security rules. No REST API layer is needed:

- Reads/writes allowed to ADMIN users are already encoded in
  `backend/firestore.rules` (`isAdmin()` checks `users/{uid}.role`).
- Privileged mutations with side effects go through the already-deployed
  callables.

The panel signs in with **Google sign-in** (admin's Google account) or, for
v0, the same anonymous uid flow — the only requirement is that the signed-in
uid has `role == "ADMIN"` in Firestore. Recommendation: enable Google
sign-in for the panel only; the Android app keeps anonymous auth.

Cost: Hosting free tier. Stack suggestion: Vite + React (or plain
HTML + Firebase JS SDK for v0). Estimated effort: 2–4 days for v1.

## API contracts (already deployed with backend/)

| Operation | Mechanism | Contract |
|-----------|-----------|----------|
| List pending reports | Firestore query | `reports where verification == "PENDING" orderBy createdAt` (admin read allowed) |
| Approve/reject report | Callable `reviewReport` | `{ reportId: string, approve: boolean }` → applies points/station/raffle server-side |
| Assign roles | Callable `setUserRole` | `{ targetUid: string, role: "USER"\|"REPORTER"\|"ADMIN" }` |
| Ban / unban | Firestore write | `users/{uid}.banned` — **add an admin-writable rule or a `setUserStatus` callable when the panel is built** (rules currently allow profile-field edits by the owner only) |
| Manage stations/cities/banners | Firestore write | direct doc writes — admin writes already allowed by rules |
| View flagged users | Firestore query | `users where flagged == true` |
| Raffle draw | Firestore query | `rewardEntries where monthYear == "yyyy-MM"`; pick a random verified entry |

## Roadmap

1. **v0 (now possible):** Firebase console *is* the admin panel — approvals
   via `reports` collection + `reviewReport` from the GCP console, roles by
   editing `users/{uid}.role`.
2. **v1:** single-page panel on Hosting: pending-report queue with photo
   preview (Storage admin read rule or signed URLs), approve/reject buttons,
   user search with ban/role controls, station/city CRUD.
3. **v2:** banner/ads management, raffle-draw button, trust-score
   dashboards, audit log (separate `auditLog` collection written by the
   callables).

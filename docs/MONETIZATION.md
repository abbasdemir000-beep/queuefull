# Monetization foundation

Principle: nothing monetized ships until there are real users; everything is
behind flags so enabling it later is a flag flip + listing update, not a
refactor.

## Current state (all in place, all off)

- `FeatureFlags.ADS_ENABLED = false`, `FeatureFlags.PREMIUM_ENABLED = false`.
- `MonetizationPolicy` (unit-tested) is the single gate every monetized
  surface must ask: `isPremium(accountType, premiumUntil, now)` and
  `shouldShowAds(...)`.
- `AppUser` already carries `accountType` (FREE/PREMIUM) and `premiumUntil`.
- Station/local-business ads: the `AdBanner` model, Room storage, seeded
  Arabic banners, and city targeting already exist and render in the app —
  this is the first real revenue channel (sell directly to Kirkuk/Erbil
  businesses, no SDK needed).

## Revenue channels, in launch order

1. **Local banner ads (now):** direct deals with fuel-adjacent businesses
   (oil shops, garages, restaurants). Sold manually, managed via the admin
   panel (`banners` collection once the backend is live). Zero cost, zero
   SDK, culturally targeted.
2. **AdMob (after real traction):** add the dependency + `APPLICATION_ID`
   only when flipping `ADS_ENABLED`; placements should be limited to the
   home list (native/banner) — never inside the report wizard. Requires:
   AdMob account, app-ads.txt, updated Data-safety form (advertising ID).
3. **Premium (later):** ad-free + future perks (status history, alerts for
   chosen stations). Server-side: `users/{uid}.accountType/premiumUntil`
   set by a Play Billing webhook/function. Do not build before channels 1–2
   prove demand.

## Server-side notes

- `premiumUntil`/`accountType` must become server-only fields in
  `firestore.rules` when premium launches (clients must not self-upgrade).
- Station-sponsored pins ("featured station") are an easy upsell: a
  `sponsoredUntil` field on `stations` + a badge in the list/map UI.

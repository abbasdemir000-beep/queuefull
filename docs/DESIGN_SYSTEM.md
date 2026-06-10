# QueueFuel Design System

> **"One application for all queues."** — كل الطوابير... بمكان واحد
>
> This document is the master design specification for QueueFuel. Every screen,
> component, and asset must follow it. Do not invent another design language,
> palette, or logo direction.

QueueFuel is evolving from a fuel-queue tracker into a universal queue platform
(government offices, hospitals, banks, universities, passport offices, service
centers, …). The visual identity therefore represents **queues, waiting lines,
locations, and real-time crowd information — not fuel**.

---

## A. Logo specification

**Master mark: Location Pin + People Queue.** The fuel nozzle never appears.

- Pin body: Deep Teal `#0D9488`, classic map-pin silhouette.
- Queue symbol: three white person figures (head + shoulders) lined up inside
  the pin bulb, the middle figure slightly larger.
- Accent: a thin Turquoise `#2DD4BF` arc inside the pin's left edge.
- Glow: a soft radial Turquoise glow (≈14–22 % alpha) behind the mark on dark
  surfaces.

Assets (single source of geometry — keep them in sync):

| Asset | File | Use |
|---|---|---|
| Brand mark (vector) | `app/src/main/res/drawable/ic_qf_logo.xml` | Splash, login, headers (via `QueueFuelLogo`) |
| Adaptive icon foreground | `res/drawable/ic_launcher_foreground.xml` | Android 8+ launcher |
| Adaptive icon background | `res/drawable/ic_launcher_background.xml` | Dark navy + glow |
| Legacy launcher PNGs | `res/mipmap-*/ic_launcher{_round}.png` | Android 7.x |
| Play Store icon (512 px) | `app/src/main/ic_launcher-playstore.png` | Store listing |

## B. Color palette

Defined once in `app/src/main/java/com/example/ui/theme/Color.kt` (Compose) and
mirrored in `res/values/colors.xml` (platform). **No colors outside this table.**

| Token | Hex | Role |
|---|---|---|
| `QfDeepTeal` | `#0D9488` | Primary — buttons, selection, brand pin |
| `QfTurquoise` | `#2DD4BF` | Secondary — highlights, glow, slogan |
| `QfTealContainer` / `QfOnTealContainer` | `#11403B` / `#5EEAD4` | Filled containers |
| `QfNavy` | `#0B1622` | Background (dark navy) |
| `QfSurface` | `#152535` | Cards, sheets (dark blue) |
| `QfSurfaceVariant` | `#1C2F42` | Inputs, chips, secondary panels |
| `QfBorder` | `#24405A` | Hairline borders, dividers |
| `QfTextPrimary` | `#FFFFFF` | Primary text (white) |
| `QfTextSecondary` | `#B6C2CE` | Secondary text (light gray) |
| `QfTextTertiary` | `#7C8B99` | Captions, muted |
| `QfSuccess` | `#22C55E` | Success / **Empty** queue |
| `QfSuccessLight` | `#4ADE80` | Short queue |
| `QfWarning` / `QfGold` | `#F59E0B` | Warning / **Medium** queue / points & trophies |
| `QfError` | `#EF4444` | Error / **Crowded** queue |
| `QfClosed` | `#6B7280` | **Closed** (gray) |

Legacy identifiers (`Cosmic*`, `Fuel*`) are aliases of these tokens for older
composables — never give them independent values.

### Queue status system (used everywhere: cards, map pins, badges, reports, admin)

| Status | Color |
|---|---|
| Empty (فارغة) | Green `QfSuccess` |
| Medium (متوسطة) | Orange `QfWarning` |
| Crowded (مزدحمة) | Red `QfError` |
| Closed (مغلقة) | Gray `QfClosed` |

## C. Typography

Typeface: **Tajawal** (Arabic + Latin), bundled in `res/font/` and exposed as
`Tajawal` in `ui/theme/Type.kt`. Hierarchy (mapped to Material 3 styles):

| Level | M3 style | Size / weight |
|---|---|---|
| Large Title | `headlineLarge` | 30 sp ExtraBold |
| Title | `titleLarge` / `titleMedium` | 20 / 16 sp Bold |
| Subtitle | `titleSmall` | 14 sp Medium |
| Body | `bodyLarge` / `bodyMedium` | 16 / 14 sp Regular |
| Caption | `labelMedium` / `labelSmall` | 12 / 11 sp Medium |

## D. Component library (`ui/components/`)

| Component | Purpose |
|---|---|
| `QueueFuelLogo` | Brand mark with optional turquoise glow |
| `QueueFuelSplashScreen` | Navy splash: glow, logo, name, Arabic slogan |
| `QfCard` | Standard dark-blue card, 16 dp radius, hairline border |
| `QfSectionHeader` | Section title row (white bold + muted trailing) |
| `QfStatusBadge` | Status pill: colored dot + label on 15 %-alpha tint |
| `QfCategoryTile` / `QfCategoriesGrid` | Home categories (4 × 2); fuel live, others "قريباً" |

Shape language: cards 14–16 dp radius, sheets 24 dp top radius, chips fully
rounded. Borders are 1 dp `QfBorder`. Elevation stays ≤ 2 dp — depth comes from
layer color, not shadows. Animations are subtle and fast (crossfades, short
tweens); no flashy effects.

## E. Screen rules

- **Splash** — logo centered on navy, soft glow, `QueueFuel` + slogan.
- **Home** — search field, city chips, categories grid, live map card, then
  "أقرب الطوابير" (nearest queues, sorted by distance) with status cards.
- **Map** — dark canvas (`QfSurfaceVariant` roads, `QfBorder` grid), status-
  colored pins, legend chips.
- **Detail sheet** — large bottom card: status, reliability, geo-verification
  state, confirm/report actions.
- **Report flow** — GPS proximity check → status choice (empty/medium/crowded)
  → mandatory photo → submit.
- **Verification / Admin** — same tokens as the user app; verdicts use the
  status colors (Verified = green, Pending = orange, Rejected = red).
- **Rewards / Profile** — points and trophies in `QfGold`, stats on `QfSurface`
  cards.

## F. Future categories

`domain/model/QueueCategory.kt` enumerates the roadmap (fuel, government,
hospitals, banks, universities, passport, service centers, more). New
categories must reuse the same tokens, status system, and components — only the
category icon and label change.

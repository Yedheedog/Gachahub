# GachaHub

A mobile-first Android app for tracking gacha banners. **hololive Dreams is treated as one shared worldwide banner schedule; Gakuen Idolmaster, Uma Musume, and The Seven Deadly Sins: Grand Cross are JP-server only.** All banner start/end times and countdowns are displayed in Japan Standard Time (JST, UTC+9 / `Asia/Tokyo`).

## Supported games
- hololive Dreams (shared worldwide schedule; check for documented regional exceptions)
- Gakuen Idolmaster (JP)
- Uma Musume (JP)
- The Seven Deadly Sins: Grand Cross (JP)

## Banner feed
The app downloads `data/banners.json`. It supports All / Live / Upcoming / Ended filters plus per-game filters, displays start/end times and live countdowns in Japan Standard Time (JST, UTC+9 / `Asia/Tokyo`), and links each banner to its cited source. Countdown text updates locally every second; the feed is refreshed on launch, manually on demand, and every six hours while the app remains open.

Each banner requires:
- `id`, `gameId` (must exactly match a supported game ID), `title`
- `startAt`, `endAt` (ISO-8601 instants; do not guess missing times)
- `sourceName`, `sourceUrl` (HTTPS evidence URL)
- optional `featuredUnits`: an array of up to five featured unit names. For hololive Dreams, keep a multi-unit pickup (usually 3–5 talents) as **one banner record** and list each featured talent here; do not split one shared banner into separate records.
- optional `imageUrl`: HTTPS URL for the banner-specific artwork. The app displays it behind a dark readability overlay; cards fall back to the plain dark style when absent.
- optional `endTimeEstimated`: boolean (defaults to `false`). Set to `true` whenever an exact end clock is taken from an unofficial/secondary schedule because the official notice omits it, or when using a maintenance-time estimate. The app labels these as estimated. For hololive Dreams, Holodori may supply the end time when the official notice omits it; cite Holodori and mark the time estimated. For Grand Cross JP notices that say a banner ends before maintenance but omit the clock time, 14:00 JST is the usual provisional maintenance-start estimate; replace it when an exact official time becomes available.

## Automatic source monitoring
The **Monitor gacha sources** GitHub Actions workflow runs every six hours and can also be started manually from Actions. It checks public announcement pages, extracts keyword-based excerpts, and uploads a 30-day report artifact. The report is a discovery aid, not verified banner data. For every new hololive Dreams banner, Holodori is the primary discovery source for the shared worldwide schedule. Prefer official dates, but when an official notice omits an exact end clock, use Holodori's listed end time as a secondary-source estimate, set `endTimeEstimated: true`, and name Holodori in `sourceName`; check for any documented regional exclusions or timing differences. Do not split a shared banner into regional copies unless an official source establishes a real regional difference. GameWith is backup cross-check only and must not be used as the initial discovery source. For Uma Musume, use the [official JP news portal](https://umamusume.jp/news/) as the primary source; its game notices provide banner details and schedules, so a third-party schedule is not needed when the official notice contains the information. Gakuen Idolmaster, Uma Musume, and Grand Cross remain JP-only. A human must verify exact banner-specific artwork and start/end instants before publishing. The monitor deliberately does not write to `data/banners.json`. Gakumas' calendar mirror is marked discovery-only; verify against the original @gkmas_official post. X itself is not scraped because authenticated API access is not configured.

## Adding a verified banner
1. For every new hololive Dreams banner, use [Holodori](https://holodori.best/announcements) as the primary and first-stop discovery source. Treat it as one shared worldwide banner unless an official source documents a regional exclusion or different schedule. Holodori is unofficial, so verify banner identity and use official start/end times whenever available. If the official notice omits an exact end clock, its Holodori-listed end time may still be entered as a secondary-source estimate: set `endTimeEstimated: true` and cite Holodori in `sourceName`. Check whether official announcements identify regional exceptions. Store the official JST schedule once rather than creating separate JP/Global copies of the same banner. Use GameWith only as a backup cross-check if needed; do not use it as the initial discovery source.
2. For Gakuen Idolmaster, Uma Musume, and Grand Cross, confirm every notice is for the Japanese server (not Global/English). For Uma Musume, use the [official news portal](https://umamusume.jp/news/) directly and take banner names, featured characters/cards, and announced dates from the relevant official game notice. For Grand Cross, the Japanese official notice at `7taizai.netmarble.jp` remains the primary source. The Netmarble forum (`forum.netmarble.com/7ds_en`) is a secondary source only: use Japanese-language posts that explicitly identify the JP service, because the URL path alone is not a reliable region indicator. Global-server announcements are excluded for these three games. For Grand Cross, GameWith may be used as a secondary cross-check only and must refer to the JP schedule; it is not needed for Uma Musume. Convert announced JST times to ISO-8601 instants (JST is UTC+09:00). For example, `2026-11-14T11:00:00+09:00` is `2026-11-14T02:00:00Z`.
3. Add a unique record to `data/banners.json` using the schema above.
4. Confirm the `gameId` exactly matches `app/src/main/java/com/yedheedog/gachahub/data/Game.kt`.

## Continuous validation
The Android build workflow runs `scripts/validate_feed.py` before compiling. The validator checks the JST timezone declaration, ISO-8601 instants with explicit UTC offsets, required fields, supported game IDs, unique banner IDs, optional featured-unit lists (up to five names), optional HTTPS banner artwork URLs, HTTPS source links, and start/end ordering.

## Build
Open in Android Studio with JDK 17 and Android SDK 35, sync Gradle, and run the `app` configuration.

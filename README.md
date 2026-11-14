# GachaHub

A mobile-first Android app for tracking Japanese-server gacha banners.

## Supported games
- hololive Dreams (JP)
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

## Automatic source monitoring
The **Monitor Japanese gacha sources** GitHub Actions workflow runs every six hours and can also be started manually from Actions. It checks public announcement pages, extracts keyword-based excerpts, and uploads a 30-day report artifact. The report is a discovery aid, not verified banner data. It deliberately does not write to `data/banners.json`: a human must open the original official announcement, verify the exact banner name and start/end instants, then publish the record. Gakumas' calendar mirror is marked discovery-only; verify against the original @gkmas_official post. X itself is not scraped because authenticated API access is not configured.

## Adding a verified banner
1. Open the original official notice and confirm the Japanese-server banner name and exact start/end times.
2. Convert the announced JST times to ISO-8601 instants (JST is UTC+09:00). For example, `2026-11-14T11:00:00+09:00` is `2026-11-14T02:00:00Z`.
3. Add a unique record to `data/banners.json` using the schema above.
4. Confirm the `gameId` exactly matches `app/src/main/java/com/yedheedog/gachahub/data/Game.kt`.

## Continuous validation
The Android build workflow runs `scripts/validate_feed.py` before compiling. The validator checks the JST timezone declaration, ISO-8601 instants with explicit UTC offsets, required fields, supported game IDs, unique banner IDs, optional featured-unit lists (up to five names), HTTPS source links, and start/end ordering.

## Build
Open in Android Studio with JDK 17 and Android SDK 35, sync Gradle, and run the `app` configuration.

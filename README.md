# GachaHub

A mobile-first Android app for tracking Japanese-server gacha banners. **All four supported games are JP-server only; Global/English-server banners, dates, maintenance notices, and schedules must never be used.**

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
- optional `imageUrl`: HTTPS URL for the banner-specific artwork. The app displays it behind a dark readability overlay; cards fall back to the plain dark style when absent.
- optional `endTimeEstimated`: boolean (defaults to `false`). Set to `true` only for a clearly marked provisional end time. For Grand Cross JP notices that say a banner ends before maintenance but omit the clock time, 14:00 JST is the usual provisional maintenance-start estimate; display it as estimated, never confirmed, and replace it when the official maintenance notice gives the actual time.

## Automatic source monitoring
The **Monitor Japanese gacha sources** GitHub Actions workflow runs every six hours and can also be started manually from Actions. It checks public announcement pages, extracts keyword-based excerpts, and uploads a 30-day report artifact. The report is a discovery aid, not verified banner data. For every new hololive Dreams banner, Holodori is the primary discovery source; use its JP-server view first, then verify every candidate and exact dates against official JP notices, which remain authoritative. GameWith is backup cross-check only and must not be used as the initial discovery source. The monitor labels these priorities explicitly. A human must verify exact banner-specific artwork and start/end instants before publishing. It deliberately does not write to `data/banners.json`. Gakumas' calendar mirror is marked discovery-only; verify against the original @gkmas_official post. X itself is not scraped because authenticated API access is not configured.

## Adding a verified banner
1. For every new hololive Dreams banner, use [Holodori](https://holodori.best/announcements) as the primary and first-stop discovery source, with the JP server selected. Holodori is unofficial, so verify the banner name and exact start/end times against the original official JP notice before adding anything. Use GameWith only as a backup cross-check if needed; do not use it as the initial discovery source.
2. Confirm the notice is for the Japanese server (not Global/English). For Grand Cross, the Japanese official notice at `7taizai.netmarble.jp` remains the primary source. The Netmarble forum (`forum.netmarble.com/7ds_en`) is a secondary source only: use Japanese-language posts that explicitly identify the JP service, because the URL path alone is not a reliable region indicator. Global-server announcements are excluded. GameWith is only a secondary cross-check and must also refer to the JP schedule. Convert the announced JST times to ISO-8601 instants (JST is UTC+09:00). For example, `2026-11-14T11:00:00+09:00` is `2026-11-14T02:00:00Z`.
3. Add a unique record to `data/banners.json` using the schema above.
4. Confirm the `gameId` exactly matches `app/src/main/java/com/yedheedog/gachahub/data/Game.kt`.

## Continuous validation
The Android build workflow runs `scripts/validate_feed.py` before compiling. The validator checks the JST timezone declaration, ISO-8601 instants with explicit UTC offsets, required fields, supported game IDs, unique banner IDs, optional featured-unit lists (up to five names), optional HTTPS banner artwork URLs, HTTPS source links, and start/end ordering.

## Build
Open in Android Studio with JDK 17 and Android SDK 35, sync Gradle, and run the `app` configuration.

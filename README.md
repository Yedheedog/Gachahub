# GachaHub

A mobile-first Android app for tracking Japanese-server gacha banners.

## Supported games
- hololive Dreams (JP)
- Gakuen Idolmaster (JP)
- Uma Musume (JP)
- The Seven Deadly Sins: Grand Cross (JP)

## Banner tracking
The app downloads `data/banners.json` from this repository, supports All / Live / Upcoming / Ended filters, shows start/end dates and countdowns, and refreshes the feed every 30 seconds while open. Each banner includes source attribution. The feed is intentionally empty until dates can be verified; no guessed banner schedules are included.

## Time handling
All displayed times and countdown calculations use Japan Standard Time (JST, UTC+9 / `Asia/Tokyo`) regardless of the device timezone. Feed timestamps must be ISO-8601 instants, preferably UTC (for example `2026-11-14T03:00:00Z`).

## Feed schema
Each item in `banners` requires:
- `id`, `gameId` (one of the supported game IDs), `title`
- `startAt`, `endAt` (ISO-8601 instants)
- `sourceName`, `sourceUrl` (HTTPS source)

## Build
Open in Android Studio with JDK 17 and Android SDK 35, sync Gradle, and run the `app` configuration.

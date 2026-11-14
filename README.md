# GachaHub

A mobile-first Android app for tracking Japanese-server gacha banners across four games.

## Supported games

- hololive Dreams (JP)
- Gakuen Idolmaster (JP)
- Uma Musume (JP)
- The Seven Deadly Sins: Grand Cross (JP)

## Time handling

All banner start/end times and countdowns must use Japan Standard Time (JST, UTC+9 / `Asia/Tokyo`), regardless of the device timezone.

## Current status

This repository contains the initial native Android scaffold built with Kotlin and Jetpack Compose. The first screen shows the supported games and an explicit JST clock.

No unverified banner dates are bundled. Automatic banner ingestion, calendar, favorites, pull tracking, persistence, and notifications are planned follow-up features.

## Build

Open the project in Android Studio with JDK 17 and Android SDK 35 installed, then sync Gradle and run the `app` configuration.

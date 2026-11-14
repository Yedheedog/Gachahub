#!/usr/bin/env python3
"""Validate GachaHub's published banner feed without third-party dependencies."""
from __future__ import annotations

import json
import re
import sys
from datetime import datetime

FEED = "data/banners.json"
SUPPORTED_GAMES = {
    "hololive_dreams",
    "学園アイドルマスター",
    "umamusume",
    "七つの大罪",
}
REQUIRED = {"id", "gameId", "title", "startAt", "endAt", "sourceName", "sourceUrl"}
ISO_INSTANT = re.compile(r"^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(?:\\.\\d+)?(?:Z|[+-]\\d{2}:\\d{2})$")


def parse_instant(value: object, label: str) -> datetime:
    if not isinstance(value, str) or not ISO_INSTANT.fullmatch(value):
        raise ValueError(f"{label} must be an ISO-8601 instant with Z or an explicit offset")
    return datetime.fromisoformat(value.replace("Z", "+00:00"))


def main() -> int:
    with open(FEED, encoding="utf-8") as handle:
        document = json.load(handle)
    if document.get("timezone") != "Asia/Tokyo":
        raise ValueError("timezone must be Asia/Tokyo")
    parse_instant(document.get("updatedAt"), "updatedAt")
    banners = document.get("banners")
    if not isinstance(banners, list):
        raise ValueError("banners must be an array")

    seen: set[str] = set()
    for index, banner in enumerate(banners):
        label = f"banners[{index}]"
        if not isinstance(banner, dict):
            raise ValueError(f"{label} must be an object")
        missing = REQUIRED - banner.keys()
        if missing:
            raise ValueError(f"{label} is missing: {', '.join(sorted(missing))}")
        for field in ("id", "gameId", "title", "sourceName", "sourceUrl"):
            if not isinstance(banner[field], str) or not banner[field].strip():
                raise ValueError(f"{label}.{field} must be a non-empty string")
        if banner["id"] in seen:
            raise ValueError(f"duplicate banner id: {banner['id']}")
        seen.add(banner["id"])
        if banner["gameId"] not in SUPPORTED_GAMES:
            raise ValueError(f"{label}.gameId is not supported: {banner['gameId']}")
        if not banner["sourceUrl"].startswith("https://"):
            raise ValueError(f"{label}.sourceUrl must use HTTPS")
        start = parse_instant(banner["startAt"], f"{label}.startAt")
        end = parse_instant(banner["endAt"], f"{label}.endAt")
        if end <= start:
            raise ValueError(f"{label}.endAt must be later than startAt")

    print(f"Feed valid: {len(banners)} banner(s), JST display timezone, unique IDs.")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except (OSError, json.JSONDecodeError, ValueError) as error:
        print(f"Feed validation failed: {error}", file=sys.stderr)
        raise SystemExit(1)

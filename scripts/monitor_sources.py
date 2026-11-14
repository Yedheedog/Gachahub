#!/usr/bin/env python3
"""Review-first source monitor for GachaHub.

Fetches public announcement pages and extracts small, keyword-based excerpts.
It NEVER publishes banner dates to banners.json: a human must verify each
candidate against the source and enter exact start/end instants.
"""
from __future__ import annotations

import hashlib
import json
import re
import sys
import time
from datetime import datetime, timezone
from html.parser import HTMLParser
from urllib.error import HTTPError, URLError
from urllib.request import Request, urlopen

SOURCES = [
    {"gameId": "hololive_dreams", "serverRegion": "JP", "name": "hololive Dreams official news (JP)", "url": "https://www.hololive-dreams.com/news/"},
    {"gameId": "学園アイドルマスター", "serverRegion": "JP", "name": "Gakuen Idolmaster official site (JP)", "url": "https://gakuen.idolmaster-official.jp/"},
    {"gameId": "umamusume", "serverRegion": "JP", "name": "Uma Musume official news (JP)", "url": "https://umamusume.jp/news/"},
    {"gameId": "七つの大罪", "serverRegion": "JP", "name": "Grand Cross official notices (JP server)", "url": "https://7taizai.netmarble.jp/notice/"},
    # Netmarble forum: include only Japanese-language posts explicitly for the JP service;
    # the forum path itself is not a reliable region indicator.
    {"gameId": "七つの大罪", "serverRegion": "JP", "name": "Grand Cross Netmarble forum (JP-language notices; verify JP server)", "url": "https://forum.netmarble.com/7ds_en", "secondary": True},
    # GameWith is a secondary cross-check for banner dates and artwork, never primary evidence.
    {"gameId": "hololive_dreams", "serverRegion": "JP", "name": "GameWith hololive Dreams banner article (JP)", "url": "https://gamewith.jp/gamedb/15524/articles/65414", "secondary": True},
    {"gameId": "umamusume", "serverRegion": "JP", "name": "GameWith Uma Musume event schedule (JP)", "url": "https://gamewith.jp/uma-musume/article/show/257333", "secondary": True},
    {"gameId": "七つの大罪", "serverRegion": "JP", "name": "GameWith Grand Cross gacha schedule (JP)", "url": "https://gamewith.jp/7taizai/article/show/155826", "secondary": True},
    # Discovery-only calendar for official @gkmas_official posts. Not treated as
    # primary evidence; the original official post must be checked before publish.
    {"gameId": "学園アイドルマスター", "serverRegion": "JP", "name": "Gakumas official-account calendar mirror (JP, discovery only)", "url": "https://oshical.com/gkmas_official", "discoveryOnly": True},
]

KEYWORDS = re.compile(r"ガチャ|ガシャ|ピックアップ|開催期間|メンテナンス|gacha|pickup|banner", re.I)
NOISE = re.compile(r"\s+")


class MetaExtractor(HTMLParser):
    """Collect page-level preview artwork without assuming it is banner-specific."""
    def __init__(self):
        super().__init__()
        self.images = []

    def handle_starttag(self, tag, attrs):
        if tag.lower() != "meta":
            return
        values = {key.lower(): value for key, value in attrs if key and value}
        key = (values.get("property") or values.get("name") or "").lower()
        if key in {"og:image", "twitter:image"}:
            url = values.get("content", "").strip()
            if url.startswith("https://") and url not in self.images:
                self.images.append(url)


class TextExtractor(HTMLParser):
    def __init__(self):
        super().__init__()
        self.parts = []
        self.skip = 0

    def handle_starttag(self, tag, attrs):
        if tag in {"script", "style", "noscript", "svg"}:
            self.skip += 1

    def handle_endtag(self, tag):
        if tag in {"script", "style", "noscript", "svg"} and self.skip:
            self.skip -= 1

    def handle_data(self, data):
        if not self.skip:
            value = NOISE.sub(" ", data).strip()
            if value:
                self.parts.append(value)


def fetch(source):
    req = Request(source["url"], headers={
        "User-Agent": "GachaHubSourceMonitor/1.0 (+https://github.com/Yedheedog/Gachahub)",
        "Accept": "text/html,application/xhtml+xml",
    })
    try:
        with urlopen(req, timeout=20) as response:
            raw = response.read(2_500_000)
            charset = response.headers.get_content_charset() or "utf-8"
            html = raw.decode(charset, errors="replace")
            parser = TextExtractor()
            parser.feed(html)
            meta = MetaExtractor()
            meta.feed(html)
            text = NOISE.sub(" ", " ".join(parser.parts)).strip()
            matches = []
            for match in KEYWORDS.finditer(text):
                start, end = max(0, match.start() - 180), min(len(text), match.end() + 260)
                excerpt = text[start:end].strip()
                if excerpt and excerpt not in matches:
                    matches.append(excerpt)
                if len(matches) >= 12:
                    break
            return {
                **source,
                "status": "ok",
                "httpStatus": response.status,
                "checkedAt": datetime.now(timezone.utc).isoformat(),
                "contentSha256": hashlib.sha256(text.encode("utf-8")).hexdigest(),
                "pageTitle": text[:180],
                "pageImageUrl": meta.images[0] if meta.images else None,
                "candidateExcerpts": matches,
                "note": "GameWith is a secondary cross-check. Excerpts and page-level preview artwork are hints only; verify exact dates and banner-specific artwork before publishing.",
            }
    except (HTTPError, URLError, TimeoutError, OSError, ValueError) as exc:
        return {
            **source,
            "status": "error",
            "checkedAt": datetime.now(timezone.utc).isoformat(),
            "error": str(exc)[:300],
            "pageImageUrl": None,
            "candidateExcerpts": [],
        }


def main():
    results = []
    for source in SOURCES:
        results.append(fetch(source))
        time.sleep(1)  # be polite to public sites
    report = {
        "generatedAt": datetime.now(timezone.utc).isoformat(),
        "timezone": "Asia/Tokyo",
        "serverRegion": "JP",
        "policy": "JP servers only. Exclude Global/English-server notices and schedules. Review-first: official Japanese-server announcements are preferred; the Netmarble forum is secondary and only Japanese-language posts explicitly for the JP service may be used; GameWith is a secondary cross-check for dates and page artwork. The forum URL path is not a reliable region indicator. This report never writes or modifies data/banners.json.",
        "sources": results,
    }
    with open("source-monitor-report.json", "w", encoding="utf-8") as f:
        json.dump(report, f, ensure_ascii=False, indent=2)
        f.write("\n")
    failures = [s for s in results if s["status"] != "ok"]
    print(f"Checked {len(results)} sources; {len(failures)} failed.")
    for item in failures:
        print(f"- {item['name']}: {item.get('error', 'unknown error')}")
    # Source outages are visible in the artifact but don't fail the entire run.
    return 0


if __name__ == "__main__":
    sys.exit(main())

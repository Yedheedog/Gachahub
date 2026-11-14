package com.yedheedog.gachahub.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant

class BannerRepository {
    suspend fun fetchBanners(): List<Banner> = withContext(Dispatchers.IO) {
        val connection = (URL(FEED_URL).openConnection() as HttpURLConnection).apply {
            connectTimeout = 10_000
            readTimeout = 10_000
            requestMethod = "GET"
            setRequestProperty("Accept", "application/json")
        }
        try {
            if (connection.responseCode !in 200..299) error("Feed returned HTTP " + connection.responseCode)
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(body)
            val array = root.optJSONArray("banners") ?: return@withContext emptyList()
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    val gameId = item.optString("gameId")
                    val game = supportedGames.firstOrNull { it.id == gameId } ?: continue
                    val start = runCatching { Instant.parse(item.getString("startAt")) }.getOrNull() ?: continue
                    val end = runCatching { Instant.parse(item.getString("endAt")) }.getOrNull() ?: continue
                    if (!end.isAfter(start)) continue
                    val sourceUrl = item.optString("sourceUrl")
                    val sourceName = item.optString("sourceName")
                    val title = item.optString("title")
                    val imageUrl = item.optString("imageUrl").takeIf { it.startsWith("https://") }
                    val endTimeEstimated = item.optBoolean("endTimeEstimated", false)
                    val featuredArray = item.optJSONArray("featuredUnits")
                    val featuredUnits = if (featuredArray == null) emptyList() else buildList {
                        for (unitIndex in 0 until featuredArray.length()) {
                            val unit = featuredArray.optString(unitIndex).trim()
                            if (unit.isNotEmpty()) add(unit)
                        }
                    }
                    if (!sourceUrl.startsWith("https://") || sourceName.isBlank() || title.isBlank()) continue
                    add(Banner(
                        id = item.optString("id", gameId + "-" + index),
                        gameId = gameId,
                        gameName = game.name,
                        title = title,
                        featuredUnits = featuredUnits,
                        imageUrl = imageUrl,
                        startAt = start,
                        endAt = end,
                        endTimeEstimated = endTimeEstimated,
                        sourceName = sourceName,
                        sourceUrl = sourceUrl
                    ))
                }
            }
        } finally {
            connection.disconnect()
        }
    }

    companion object {
        const val FEED_URL = "https://raw.githubusercontent.com/Yedheedog/Gachahub/main/data/banners.json"
    }
}

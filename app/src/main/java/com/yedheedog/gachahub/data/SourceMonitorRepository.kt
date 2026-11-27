package com.yedheedog.gachahub.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class SourceMonitorEntry(
    val gameId: String,
    val name: String,
    val url: String,
    val status: String,
    val checkedAt: String,
    val note: String,
    val error: String?,
    val candidateExcerpts: List<String>
)

data class SourceMonitorReport(
    val generatedAt: String,
    val sources: List<SourceMonitorEntry>
)

class SourceMonitorRepository {
    suspend fun fetchReport(): SourceMonitorReport = withContext(Dispatchers.IO) {
        val connection = (URL(REPORT_URL).openConnection() as HttpURLConnection).apply {
            connectTimeout = 10_000
            readTimeout = 15_000
            requestMethod = "GET"
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Cache-Control", "no-cache")
        }
        try {
            if (connection.responseCode !in 200..299) {
                error("Source report returned HTTP " + connection.responseCode)
            }
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(body)
            val array = root.optJSONArray("sources")
                ?: error("Source report is missing its sources array")
            val entries = buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    val name = item.optString("name").trim()
                    val url = item.optString("url").trim()
                    if (name.isEmpty() || !url.startsWith("https://")) continue
                    val excerpts = item.optJSONArray("candidateExcerpts")
                    val candidates = if (excerpts == null) emptyList() else buildList {
                        for (excerptIndex in 0 until excerpts.length()) {
                            val excerpt = excerpts.optString(excerptIndex).trim()
                            if (excerpt.isNotEmpty()) add(excerpt)
                        }
                    }
                    add(
                        SourceMonitorEntry(
                            gameId = item.optString("gameId"),
                            name = name,
                            url = url,
                            status = item.optString("status", "unknown"),
                            checkedAt = item.optString("checkedAt"),
                            note = item.optString("note"),
                            error = item.optString("error").takeIf { it.isNotBlank() && it != "null" },
                            candidateExcerpts = candidates
                        )
                    )
                }
            }
            SourceMonitorReport(
                generatedAt = root.optString("generatedAt"),
                sources = entries
            )
        } finally {
            connection.disconnect()
        }
    }

    companion object {
        const val REPORT_URL =
            "https://raw.githubusercontent.com/Yedheedog/Gachahub/main/data/source-monitor-report.json"
    }
}

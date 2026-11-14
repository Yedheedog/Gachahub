package com.yedheedog.gachahub.time

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object JapanTime {
    val zone: ZoneId = ZoneId.of("Asia/Tokyo")

    private val dateTimeFormatter = DateTimeFormatter
        .ofPattern("MMM d, yyyy · HH:mm", Locale.ENGLISH)
        .withZone(zone)

    fun now(): Instant = Instant.now()

    fun format(instant: Instant): String = dateTimeFormatter.format(instant)

    fun countdown(target: Instant, now: Instant = Instant.now()): String {
        val seconds = (target.epochSecond - now.epochSecond).coerceAtLeast(0)
        val days = seconds / 86_400
        val hours = (seconds % 86_400) / 3_600
        val minutes = (seconds % 3_600) / 60
        return if (days > 0) {
            "${days}d ${hours}h ${minutes}m"
        } else {
            "${hours}h ${minutes}m"
        }
    }
}

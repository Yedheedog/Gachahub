package com.yedheedog.gachahub.data

import java.time.Instant

data class Banner(
    val id: String,
    val gameId: String,
    val gameName: String,
    val title: String,
    val startAt: Instant,
    val endAt: Instant,
    val sourceName: String,
    val sourceUrl: String
)

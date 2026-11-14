package com.yedheedog.gachahub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yedheedog.gachahub.data.Banner
import com.yedheedog.gachahub.data.BannerRepository
import com.yedheedog.gachahub.data.supportedGames
import com.yedheedog.gachahub.time.JapanTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val AppBackground = Color(0xFF101116)
private val Panel = Color(0xFF1B1D26)
private val Accent = Color(0xFFB79CFF)
private val Muted = Color(0xFF9A9BA8)
private val JstZone = ZoneId.of("Asia/Tokyo")
private val JstDate = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm").withZone(JstZone)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(background = AppBackground, surface = Panel, primary = Accent)) {
                GachaHubHome()
            }
        }
    }
}

@Composable
private fun GachaHubHome() {
    var banners by remember { mutableStateOf<List<Banner>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var filter by remember { mutableStateOf("All") }
    var selectedGameId by remember { mutableStateOf<String?>(null) }
    var now by remember { mutableStateOf(Instant.now()) }
    val scope = rememberCoroutineScope()
    val repository = remember { BannerRepository() }

    suspend fun refresh() {
        loading = true
        try {
            banners = repository.fetchBanners()
            error = null
        } catch (e: Exception) {
            error = "Could not update banner feed. Check your connection and try again."
        } finally {
            loading = false
        }
    }

    LaunchedEffect(Unit) {
        refresh()
        while (true) {
            delay(1_000)
            now = Instant.now()
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(6 * 60 * 60 * 1_000L)
            refresh()
        }
    }

    val visible = banners.filter {
        (selectedGameId == null || it.gameId == selectedGameId) && when (filter) {
            "Live" -> it.startAt <= now && now < it.endAt
            "Upcoming" -> it.startAt > now
            "Ended" -> it.endAt <= now
            else -> true
        }
    }.sortedBy { it.startAt }

    Surface(modifier = Modifier.fillMaxSize(), color = AppBackground) {
        LazyColumn(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 26.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text("GachaHub", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                Text("Japanese server banner tracker", color = Muted, fontSize = 14.sp)
            }
            item {
                Surface(color = Panel, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(18.dp)) {
                        Text("JAPAN STANDARD TIME", color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(JapanTime.format(now), color = Color.White, fontSize = 20.sp)
                        Text("All dates and countdowns use Asia/Tokyo (UTC+9).", color = Muted, fontSize = 12.sp)
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("BANNERS", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { scope.launch { refresh() } }, enabled = !loading) {
                        Text(if (loading) "Updating…" else "Refresh")
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("All", "Live", "Upcoming", "Ended").forEach { choice ->
                        FilterChip(selected = filter == choice, onClick = { filter = choice }, label = { Text(choice) })
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedGameId == null,
                        onClick = { selectedGameId = null },
                        label = { Text("All games") }
                    )
                    supportedGames.forEach { game ->
                        FilterChip(
                            selected = selectedGameId == game.id,
                            onClick = { selectedGameId = game.id },
                            label = { Text(game.name) }
                        )
                    }
                }
            }
            if (error != null) {
                item { Text(error!!, color = Color(0xFFFFB4AB), fontSize = 12.sp) }
            }
            if (visible.isEmpty()) {
                item {
                    Surface(color = Panel, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(18.dp)) {
                            Text(if (banners.isEmpty()) "No verified banners yet" else "No banners in this view", color = Color.White, fontWeight = FontWeight.SemiBold)
                            Text(
                                if (banners.isEmpty()) "The feed is ready, but no banner dates are published until they can be checked against reliable sources."
                                else "Try another filter or refresh the feed.",
                                color = Muted, fontSize = 12.sp
                            )
                        }
                    }
                }
            }
            items(visible, key = { it.id }) { banner ->
                BannerCard(banner = banner, now = now)
            }
            item {
                Text("YOUR GAMES", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            items(supportedGames) { game ->
                Surface(
                    color = Panel,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(118.dp)
                ) {
                    Box(Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = game.imageUrl,
                            contentDescription = game.name + " artwork",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.58f))
                        )
                        Row(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(game.name, color = Color.White, fontWeight = FontWeight.Bold)
                                Text(game.regionLabel, color = Color.White.copy(alpha = 0.82f), fontSize = 12.sp)
                            }
                            Text(
                                banners.count { it.gameId == game.id }.toString() + " banners",
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BannerCard(banner: Banner, now: Instant) {
    val uriHandler = LocalUriHandler.current
    val live = banner.startAt <= now && now < banner.endAt
    val status = when {
        live -> "LIVE · " + countdown(banner.endAt, now) + " left"
        banner.startAt > now -> "Starts in " + countdown(banner.startAt, now)
        else -> "Ended"
    }
    Surface(color = Panel, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(banner.gameName, color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(banner.title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            if (banner.featuredUnits.isNotEmpty()) {
                Text("FEATURED LINEUP", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(banner.featuredUnits.joinToString("  •  "), color = Color.White, fontSize = 13.sp)
            }
            Text(status, color = if (live) Color(0xFF8DE0B0) else Muted, fontSize = 12.sp)
            Text("Start  " + JstDate.format(banner.startAt), color = Muted, fontSize = 12.sp)
            Text("End    " + JstDate.format(banner.endAt), color = Muted, fontSize = 12.sp)
            Text(
                "Open source: " + banner.sourceName,
                color = Accent,
                fontSize = 11.sp,
                modifier = Modifier.clickable {
                    runCatching { uriHandler.openUri(banner.sourceUrl) }
                }
            )
        }
    }
}

private fun countdown(target: Instant, now: Instant): String {
    val seconds = (target.epochSecond - now.epochSecond).coerceAtLeast(0)
    val days = seconds / 86400
    val hours = (seconds % 86400) / 3600
    val minutes = (seconds % 3600) / 60
    return if (days > 0) days.toString() + "d " + hours + "h"
    else if (hours > 0) hours.toString() + "h " + minutes + "m"
    else minutes.toString() + "m"
}

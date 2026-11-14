package com.yedheedog.gachahub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yedheedog.gachahub.data.supportedGames
import com.yedheedog.gachahub.time.JapanTime

private val AppBackground = Color(0xFF101116)
private val Panel = Color(0xFF1B1D26)
private val Accent = Color(0xFFB79CFF)
private val Muted = Color(0xFF9A9BA8)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(
                background = AppBackground,
                surface = Panel,
                primary = Accent
            )) {
                GachaHubHome()
            }
        }
    }
}

@Composable
private fun GachaHubHome() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AppBackground
    ) {
        LazyColumn(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "GachaHub",
                    color = Color.White,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Japanese server banner tracker",
                    color = Muted,
                    fontSize = 14.sp
                )
            }
            item {
                Surface(
                    color = Panel,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Text("JAPAN STANDARD TIME", color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Text(JapanTime.format(JapanTime.now()), color = Color.White, fontSize = 20.sp)
                        Text("All banner dates and countdowns use JST (UTC+9).", color = Muted, fontSize = 12.sp)
                    }
                }
            }
            item {
                Text("YOUR GAMES", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            items(supportedGames) { game ->
                Surface(
                    color = Panel,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(game.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            Text(game.regionLabel, color = Muted, fontSize = 12.sp)
                        }
                        Text("Banners coming soon", color = Accent, fontSize = 12.sp)
                    }
                }
            }
            item {
                Text(
                    "Live banner data is not connected yet. Dates will only appear after they can be verified from reliable sources.",
                    color = Muted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                )
            }
        }
    }
}

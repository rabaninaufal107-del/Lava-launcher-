package com.movtery.zalithlauncher.ui.lava

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val BgDark = Color(0xFF1E1A19)
private val SideDark = Color(0xFF2A2423)
private val PlayGreen = Color(0xFF3C8527)
private val Lava = Color(0xFFE5521B)

@Composable
fun LavaHomeScreen(onPlay: () -> Unit = {}) {
    var selected by remember { mutableStateOf("Home") }
    var query by remember { mutableStateOf("") }

    Row(Modifier.fillMaxSize().background(BgDark)) {
        // Menu samping
        Column(
            Modifier.width(150.dp).fillMaxHeight().background(SideDark).padding(vertical = 12.dp)
        ) {
            Text(
                "Lava Launcher",
                color = Lava,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            listOf("Home", "News", "Account", "Settings").forEach { name ->
                SideItem(name, name == selected) { selected = name }
            }
        }

        // Konten utama
        Column(Modifier.weight(1f).fillMaxHeight().padding(16.dp)) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Button(
                    onClick = onPlay,
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PlayGreen)
                ) {
                    Text("PLAY JAVA", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            Spacer(Modifier.height(12.dp))

            // Banner berita
            Box(
                Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(8.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFF3A1A10), Color(0xFFB8400F))))
                    .padding(20.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Column {
                    Text("Berita Minecraft", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text("Berita terbaru akan tampil di sini.", color = Color(0xFFEEDDD5), fontSize = 14.sp)
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = { },
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PlayGreen)
                    ) { Text("Baca selengkapnya", color = Color.White) }
                }
            }
            Spacer(Modifier.height(12.dp))

            // Pencarian mod
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Cari mod...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SourceCard("CurseForge", Modifier.weight(1f))
                SourceCard("Modrinth", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SideItem(text: String, active: Boolean, onClick: () -> Unit) {
    Text(
        text.uppercase(),
        color = if (active) Color.White else Color(0xFFBBB0AC),
        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
        fontSize = 13.sp,
        modifier = Modifier.fillMaxWidth()
            .background(if (active) Color(0xFF3A3331) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    )
}

@Composable
private fun SourceCard(name: String, modifier: Modifier = Modifier) {
    Box(
        modifier.height(56.dp).clip(RoundedCornerShape(6.dp)).background(SideDark),
        contentAlignment = Alignment.Center
    ) {
        Text(name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}

package com.movtery.zalithlauncher.ui.lava

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val BgDark = Color(0xFF1E1A19)
private val SideDark = Color(0xFF2A2423)
private val PlayGreen = Color(0xFF3C8527)
private val Lava = Color(0xFFE5521B)

@Composable
fun LavaHomeScreen(
    onPlay: () -> Unit = {},
    onOpenAccounts: () -> Unit = {},
    onOpenVersions: () -> Unit = {},
    onOpenSettings: () -> Unit = {}
) {
    var selected by remember { mutableStateOf("Home") }
    var query by remember { mutableStateOf("") }
    var news by remember { mutableStateOf<List<NewsItem>>(emptyList()) }
    var sel by remember { mutableStateOf(0) }
    val openLink = rememberOpenLink()
    LavaSplashHost()

    LaunchedEffect(Unit) {
        runCatching { loadNews() }.onSuccess { news = it.take(8) }
    }

    Row(Modifier.fillMaxSize().background(BgDark)) {
        // Menu samping
        Column(
            Modifier.width(170.dp).fillMaxHeight().background(SideDark).padding(vertical = 12.dp)
        ) {
            Text(
                "Lava Launcher",
                color = Lava,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Spacer(Modifier.height(4.dp))
            SideItem("Home", selected == "Home") { selected = "Home" }
            SideItem("Installations", false) { onOpenVersions() }
            SideItem("Skins", selected == "Skins") { selected = "Skins" }
            SideItem("Patch Notes", false) { openLink("https://www.minecraft.net") }
            SideItem("News", selected == "News") { selected = "News" }
            SideItem("Account", false) { onOpenAccounts() }
            SideItem("Settings", false) { onOpenSettings() }
        }

        // Konten utama
        if (selected == "News") {
            LavaNewsScreen(Modifier.weight(1f).fillMaxHeight(), openLink)
        } else if (selected == "Skins") {
            LavaSkinsScreen(Modifier.weight(1f).fillMaxHeight(), onOpenAccounts)
        } else Column(Modifier.weight(1f).fillMaxHeight().padding(12.dp)) {
            PlayBar(onPlay)
            Spacer(Modifier.height(8.dp))

            // Banner besar + daftar thumbnail di kanan
            Row(
                Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Banner(news.getOrNull(sel), openLink, Modifier.weight(1f).fillMaxHeight())
                Column(
                    Modifier.width(110.dp).fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    news.take(4).forEachIndexed { i, n ->
                        Thumb(n, i == sel, Modifier.weight(1f).fillMaxWidth()) { sel = i }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))

            // Pencarian mod
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Cari mod...") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(12.dp))
                Text("FILTERS", color = Color(0xFFBBB0AC), fontSize = 12.sp)
            }
            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SourceCard("CurseForge", Modifier.weight(1f))
                SourceCard("Modrinth", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun PlayBar(onPlay: () -> Unit) {
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Row(Modifier.clip(RoundedCornerShape(4.dp))) {
            Box(
                Modifier.background(PlayGreen).clickable(onClick = onPlay)
                    .padding(horizontal = 36.dp, vertical = 10.dp)
            ) {
                Text("PLAY JAVA", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Box(
                Modifier.background(Color(0xFF2F6B24)).padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text("▼", color = Color.White, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun Banner(item: NewsItem?, openLink: (String) -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.clip(RoundedCornerShape(6.dp)).background(Color(0xFF3A1A10))) {
        if (item != null) {
            NetImage(item.images, Modifier.fillMaxSize())
        }
        Box(
            Modifier.fillMaxSize().background(
                Brush.horizontalGradient(listOf(Color(0xE61E1A19), Color(0x001E1A19)))
            )
        )
        Column(
            Modifier.align(Alignment.BottomStart).fillMaxWidth(0.55f).padding(14.dp)
        ) {
            Text(
                item?.title ?: "Memuat berita...",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (item != null && item.text.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    item.text,
                    color = Color(0xFFEEDDD5),
                    fontSize = 12.sp,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (item != null && item.link.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { openLink(item.link) },
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PlayGreen)
                ) { Text("Baca selengkapnya", color = Color.White, fontSize = 12.sp) }
            }
        }
    }
}

@Composable
private fun Thumb(item: NewsItem, active: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(4.dp)
    Box(
        modifier
            .clip(shape)
            .border(if (active) 2.dp else 1.dp, if (active) PlayGreen else Color(0xFF3F3735), shape)
            .clickable(onClick = onClick)
    ) {
        NetImage(item.images, Modifier.fillMaxSize())
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
            .padding(horizontal = 16.dp, vertical = 10.dp)
    )
}

@Composable
private fun SourceCard(name: String, modifier: Modifier = Modifier) {
    Box(
        modifier.height(48.dp).clip(RoundedCornerShape(6.dp)).background(SideDark),
        contentAlignment = Alignment.Center
    ) {
        Text(name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}

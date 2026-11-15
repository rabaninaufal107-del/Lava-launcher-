package com.movtery.zalithlauncher.ui.lava

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

private val CardDark = Color(0xFF2A2423)
private val BorderDark = Color(0xFF3F3735)
private val GreenBtn = Color(0xFF3C8527)
private val GreenText = Color(0xFF6BE04A)

private const val BASE = "https://launchercontent.mojang.com"

internal data class NewsItem(
    val title: String,
    val category: String,
    val link: String,
    val images: List<String>,
    val text: String = ""
)

private fun imageCandidates(raw: String): List<String> = when {
    raw.isBlank() -> emptyList()
    raw.startsWith("http") -> listOf(raw)
    raw.startsWith("/v2/") -> listOf(BASE + raw)
    raw.startsWith("/") -> listOf(BASE + raw, BASE + "/v2" + raw)
    else -> listOf("$BASE/$raw", "$BASE/v2/$raw")
}

internal suspend fun loadNews(): List<NewsItem> = withContext(Dispatchers.IO) {
    val conn = URL("$BASE/v2/news.json").openConnection() as HttpURLConnection
    conn.connectTimeout = 15000
    conn.readTimeout = 15000
    try {
        val text = conn.inputStream.bufferedReader().use { it.readText() }
        val arr = JSONObject(text).getJSONArray("entries")
        (0 until arr.length()).map { i ->
            val e = arr.getJSONObject(i)
            val raw = e.optJSONObject("newsPageImage")?.optString("url").orEmpty()
            NewsItem(
                title = e.optString("title"),
                category = e.optString("category"),
                link = e.optString("readMoreLink"),
                images = imageCandidates(raw),
                text = e.optString("text")
            )
        }
    } finally {
        conn.disconnect()
    }
}

private val imageCache = ConcurrentHashMap<String, ImageBitmap>()

@Composable
internal fun NetImage(urls: List<String>, modifier: Modifier = Modifier) {
    val bmp by produceState<ImageBitmap?>(null, urls) {
        value = withContext(Dispatchers.IO) {
            var found: ImageBitmap? = null
            for (u in urls) {
                val cached = imageCache[u]
                if (cached != null) { found = cached; break }
                val loaded = runCatching {
                    val opts = BitmapFactory.Options().apply { inSampleSize = 2 }
                    URL(u).openStream().use { BitmapFactory.decodeStream(it, null, opts) }?.asImageBitmap()
                }.getOrNull()
                if (loaded != null) { imageCache[u] = loaded; found = loaded; break }
            }
            found
        }
    }
    Box(modifier.background(Color(0xFF1A1514))) {
        val b = bmp
        if (b != null) {
            Image(
                bitmap = b,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun rememberOpenLink(): (String) -> Unit {
    val ctx: Context = LocalContext.current
    return remember(ctx) {
        val f: (String) -> Unit = { url ->
            runCatching {
                ctx.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
            Unit
        }
        f
    }
}

@Composable
fun LavaNewsScreen(modifier: Modifier = Modifier, onOpenLink: (String) -> Unit) {
    var items by remember { mutableStateOf<List<NewsItem>?>(null) }
    var failed by remember { mutableStateOf(false) }
    var reload by remember { mutableStateOf(0) }

    LaunchedEffect(reload) {
        failed = false
        items = null
        runCatching { loadNews() }
            .onSuccess { items = it }
            .onFailure { failed = true }
    }

    Column(modifier.padding(16.dp)) {
        Text(
            "BERITA MINECRAFT",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(GreenBtn)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        )
        Spacer(Modifier.height(12.dp))

        val list = items
        when {
            failed -> Column(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Gagal memuat berita. Periksa koneksi internet.", color = Color(0xFFBBB0AC))
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { reload++ },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenBtn)
                ) { Text("Coba lagi", color = Color.White) }
            }
            list == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = GreenText)
            }
            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(list) { n -> NewsCard(n, onOpenLink) }
            }
        }
    }
}

@Composable
private fun NewsCard(n: NewsItem, onOpenLink: (String) -> Unit) {
    val shape = RoundedCornerShape(8.dp)
    Column(
        Modifier
            .clip(shape)
            .background(CardDark)
            .border(1.dp, BorderDark, shape)
    ) {
        NetImage(n.images, Modifier.fillMaxWidth().aspectRatio(16f / 9f))
        Column(Modifier.padding(12.dp)) {
            if (n.category.isNotBlank()) {
                Text(
                    n.category.uppercase(),
                    color = GreenText,
                    fontSize = 11.sp,
                    modifier = Modifier
                        .border(1.dp, Color(0xFF2F6B24), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
                Spacer(Modifier.height(8.dp))
            }
            Text(
                n.title,
                color = Color.White,
                fontSize = 15.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = { if (n.link.isNotBlank()) onOpenLink(n.link) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenBtn)
            ) {
                Text("BACA DI MINECRAFT.NET", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

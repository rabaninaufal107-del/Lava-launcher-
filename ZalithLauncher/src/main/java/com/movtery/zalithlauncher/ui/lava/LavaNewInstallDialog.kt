package com.movtery.zalithlauncher.ui.lava

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

private val DlgBg = Color(0xFF1E1A19)
private val FieldBg = Color(0xFF2A2423)
private val FieldBorder = Color(0xFF3F3735)
private val BtnGreen = Color(0xFF3C8527)
private val GreenText = Color(0xFF6BE04A)
private val Muted = Color(0xFFBBB0AC)

private val loaders = listOf("Vanilla", "Fabric", "Forge", "NeoForge", "Quilt", "OptiFine")

private data class CubeColors(val top: Color, val left: Color, val right: Color)

private val cubeIcons = listOf(
    CubeColors(Color(0xFF5DA832), Color(0xFF7A5232), Color(0xFF5C3B20)),
    CubeColors(Color(0xFFFF9A1F), Color(0xFFC9300F), Color(0xFF8A210A)),
    CubeColors(Color(0xFFBDBDBD), Color(0xFF8E8E8E), Color(0xFF6A6A6A)),
    CubeColors(Color(0xFF7FF0F0), Color(0xFF34B8C4), Color(0xFF218A96)),
    CubeColors(Color(0xFFFFE066), Color(0xFFE0A91A), Color(0xFFA67A10))
)

/** Daftar versi rilis Minecraft dari manifest resmi Mojang. */
private suspend fun loadMcVersions(): List<String> = withContext(Dispatchers.IO) {
    val conn = URL("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json")
        .openConnection() as HttpURLConnection
    conn.connectTimeout = 15000
    conn.readTimeout = 15000
    try {
        val text = conn.inputStream.bufferedReader().use { it.readText() }
        val arr = JSONObject(text).getJSONArray("versions")
        (0 until arr.length())
            .map { arr.getJSONObject(it) }
            .filter { it.optString("type") == "release" }
            .map { it.optString("id") }
    } finally {
        conn.disconnect()
    }
}

@Composable
private fun MiniCube(c: CubeColors, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val s = size.minDimension
        fun poly(vararg v: Pair<Float, Float>): Path = Path().apply {
            v.forEachIndexed { i, q ->
                if (i == 0) moveTo(q.first * s, q.second * s) else lineTo(q.first * s, q.second * s)
            }
            close()
        }
        drawPath(poly(0.5f to 0.08f, 0.92f to 0.30f, 0.5f to 0.52f, 0.08f to 0.30f), c.top)
        drawPath(poly(0.08f to 0.30f, 0.5f to 0.52f, 0.5f to 0.94f, 0.08f to 0.72f), c.left)
        drawPath(poly(0.5f to 0.52f, 0.92f to 0.30f, 0.92f to 0.72f, 0.5f to 0.94f), c.right)
    }
}

@Composable
fun LavaNewInstallDialog(
    onDismiss: () -> Unit,
    onCreate: ((String, String, String) -> Unit)? = null
) {
    var icon by remember { mutableStateOf(0) }
    var name by remember { mutableStateOf("") }
    var versions by remember { mutableStateOf<List<String>>(emptyList()) }
    var version by remember { mutableStateOf("") }
    var loader by remember { mutableStateOf("Vanilla") }
    var expanded by remember { mutableStateOf(false) }
    var notice by remember { mutableStateOf(false) }
    var loadFailed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        runCatching { loadMcVersions() }
            .onSuccess {
                versions = it
                if (it.isNotEmpty()) version = it.first()
            }
            .onFailure { loadFailed = true }
    }

    val autoName = if (version.isBlank()) loader else "$version $loader"
    val shape = RoundedCornerShape(6.dp)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            Modifier
                .fillMaxWidth(0.75f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(10.dp))
                .background(DlgBg)
                .padding(20.dp)
        ) {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                Text("CREATE NEW INSTALLATION", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(14.dp))

                Text("INSTALLATION ICON", color = Muted, fontSize = 12.sp)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    cubeIcons.forEachIndexed { i, c ->
                        val on = i == icon
                        Box(
                            Modifier.size(56.dp).clip(shape)
                                .background(if (on) Color(0xFF24401C) else FieldBg)
                                .border(if (on) 2.dp else 1.dp, if (on) GreenText else FieldBorder, shape)
                                .clickable { icon = i }
                                .padding(8.dp)
                        ) { MiniCube(c, Modifier.fillMaxSize()) }
                    }
                }
                Spacer(Modifier.height(14.dp))

                Text("INSTALLATION NAME", color = Muted, fontSize = 12.sp)
                Spacer(Modifier.height(6.dp))
                MaterialTheme(
                    colorScheme = darkColorScheme(),
                    typography = MaterialTheme.typography,
                    shapes = MaterialTheme.shapes
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = { Text(autoName) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(Modifier.height(14.dp))

                Text("MINECRAFT VERSION", color = Muted, fontSize = 12.sp)
                Spacer(Modifier.height(6.dp))
                Box {
                    Row(
                        Modifier.fillMaxWidth().clip(shape).background(FieldBg)
                            .border(1.dp, FieldBorder, shape)
                            .clickable { if (versions.isNotEmpty()) expanded = true }
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            when {
                                loadFailed -> "Gagal memuat versi (periksa internet)"
                                version.isBlank() -> "Memuat versi..."
                                else -> "Minecraft $version"
                            },
                            color = if (loadFailed) Muted else GreenText,
                            modifier = Modifier.weight(1f)
                        )
                        Text("▼", color = Color.White, fontSize = 12.sp)
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(FieldBg).heightIn(max = 240.dp)
                    ) {
                        versions.forEach { v ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        v,
                                        color = if (v == version) GreenText else Color.White,
                                        fontWeight = if (v == version) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = { version = v; expanded = false }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))

                Text("MOD LOADER", color = Muted, fontSize = 12.sp)
                Spacer(Modifier.height(6.dp))
                loaders.chunked(3).forEach { rowItems ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowItems.forEach { l ->
                            val on = l == loader
                            Box(
                                Modifier.weight(1f).clip(shape)
                                    .background(if (on) BtnGreen else FieldBg)
                                    .border(1.dp, if (on) GreenText else FieldBorder, shape)
                                    .clickable { loader = l }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) { Text(l, color = Color.White, fontSize = 13.sp) }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            if (notice) {
                Text(
                    "Pemasangan otomatis belum tersambung ke Zalith. Unduh game lewat ikon unduh di bar atas, lalu game muncul di daftar Installations.",
                    color = Color(0xFFFFB330),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F3A38))
                ) { Text("CANCEL", color = Color.White, fontWeight = FontWeight.Bold) }
                Spacer(Modifier.width(10.dp))
                Button(
                    onClick = {
                        val finalName = name.ifBlank { autoName }
                        if (onCreate != null) {
                            onCreate(finalName, version, loader)
                            onDismiss()
                        } else {
                            notice = true
                        }
                    },
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BtnGreen)
                ) { Text("CREATE", color = Color.White, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

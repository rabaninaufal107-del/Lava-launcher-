package com.movtery.zalithlauncher.ui.lava

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.ui.screens.content.elements.CommonVersionInfoLayout

private val RowDark = Color(0xFF2A2423)
private val RowActive = Color(0xFF3A3331)
private val GreenBar = Color(0xFF6BE04A)
private val BtnGreen = Color(0xFF3C8527)

@Composable
fun LavaInstallationsScreen(
    modifier: Modifier = Modifier,
    onOpenVersions: () -> Unit,
    onCreateInstallation: ((String, String, String) -> Unit)? = null,
    onOpenDownload: (() -> Unit)? = null
) {
    var showNew by remember { mutableStateOf(false) }
    val versions by VersionsManager.versions.collectAsStateWithLifecycle()
    val current by VersionsManager.currentVersion.collectAsStateWithLifecycle()
    val shape = RoundedCornerShape(6.dp)

    Column(modifier.padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "INSTALLATIONS",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    if (onOpenDownload != null) {
                        onOpenDownload()
                    } else {
                        showNew = true
                    }
                },
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BtnGreen)
            ) { Text("+ NEW INSTALLATION", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
        }
        Spacer(Modifier.height(10.dp))

        if (versions.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    "Belum ada game yang terpasang.\nUnduh game lewat ikon unduh, nanti muncul di sini.",
                    color = Color(0xFFBBB0AC),
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(versions) { v ->
                    val active = v == current
                    Row(
                        Modifier.fillMaxWidth()
                            .clip(shape)
                            .background(if (active) RowActive else RowDark)
                            .clickable { if (!active) VersionsManager.saveVersion(v) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MaterialTheme(
                            colorScheme = darkColorScheme(),
                            typography = MaterialTheme.typography,
                            shapes = MaterialTheme.shapes
                        ) {
                            CompositionLocalProvider(LocalContentColor provides Color.White) {
                                CommonVersionInfoLayout(
                                    modifier = Modifier.weight(1f),
                                    version = v,
                                    iconSize = 40.dp
                                )
                            }
                        }
                        if (active) {
                            Box(Modifier.width(4.dp).height(36.dp).background(GreenBar))
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        Button(
            onClick = onOpenVersions,
            shape = RoundedCornerShape(4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BtnGreen),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("KELOLA VERSI", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }

    if (showNew) {
        LavaNewInstallDialog(
            onDismiss = { showNew = false },
            onCreate = onCreateInstallation
        )
    }
}

package com.movtery.zalithlauncher.ui.lava

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.account.getAccountTypeName
import com.movtery.zalithlauncher.ui.screens.content.elements.PlayerFace

private val AccCard = Color(0xFF2A2423)
private val AccBorder = Color(0xFF3F3735)
private val AccGreen = Color(0xFF3C8527)
private val AccGrey = Color(0xFF3F3A38)
private val AccMuted = Color(0xFFBBB0AC)

@Composable
fun LavaAccountScreen(modifier: Modifier = Modifier, onOpenAccounts: () -> Unit) {
    val account by AccountsManager.currentAccountFlow.collectAsStateWithLifecycle()
    val refreshWardrobe by AccountsManager.refreshWardrobe.collectAsStateWithLifecycle()
    val shape = RoundedCornerShape(8.dp)
    val acc = account

    Column(
        modifier.padding(16.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            Modifier.fillMaxWidth().clip(shape).background(AccCard).border(1.dp, AccBorder, shape).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (acc != null) {
                PlayerFace(
                    account = acc,
                    avatarSize = 64.dp,
                    refreshKey = refreshWardrobe
                )
                Spacer(Modifier.height(8.dp))
                Text(acc.username, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(getAccountTypeName(acc), color = Color(0xFF6BE04A), fontSize = 12.sp)
            } else {
                Box(
                    Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF1E1A19))
                        .border(2.dp, Color(0xFF2F8BE0), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) { Text("?", color = AccMuted, fontSize = 28.sp) }
                Spacer(Modifier.height(8.dp))
                Text("NO ACTIVE ACCOUNT", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Tambahkan akun di bawah untuk bermain Minecraft", color = Color(0xFFFF6B6B), fontSize = 12.sp)
            }
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AccButton("+ MICROSOFT", AccGreen, Modifier.weight(1f), onOpenAccounts)
                AccButton("+ ELY.BY", AccGrey, Modifier.weight(1f), onOpenAccounts)
                AccButton("+ OFFLINE", AccGrey, Modifier.weight(1f), onOpenAccounts)
            }
        }

        Spacer(Modifier.height(12.dp))
        Text(
            "Login Microsoft, Ely.by, dan akun offline dilakukan di layar akun Zalith.",
            color = AccMuted,
            fontSize = 12.sp
        )
        Spacer(Modifier.height(8.dp))
        AccButton("KELOLA AKUN TERSIMPAN", AccGrey, Modifier.fillMaxWidth(), onOpenAccounts)
    }
}

@Composable
private fun AccButton(text: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

package com.movtery.zalithlauncher.ui.lava

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import com.movtery.petlib.PetAnimation
import com.movtery.petlib.PetTapReaction
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.account.wardrobe.toArmModel
import com.movtery.zalithlauncher.ui.components.PetModelView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val SkinCard = Color(0xFF2A2423)
private val SkinBorder = Color(0xFF3F3735)
private val SkinGreen = Color(0xFF3C8527)
private val SkinGreenText = Color(0xFF6BE04A)

@Composable
fun LavaSkinsScreen(modifier: Modifier = Modifier, onOpenWardrobe: () -> Unit) {
    val account by AccountsManager.currentAccountFlow.collectAsStateWithLifecycle()
    val refreshWardrobe by AccountsManager.refreshWardrobe.collectAsStateWithLifecycle()

    val armModel = account?.skinModelType?.toArmModel()
    val skinStamp = account?.getSkinFile()?.lastModified()
    val capeStamp = account?.getCapeFile()?.lastModified()
    val skinBytes by produceState<ByteArray?>(null, account, refreshWardrobe, skinStamp) {
        value = withContext(Dispatchers.IO) {
            account?.getSkinFile()?.takeIf { it.exists() }
                ?.inputStream()?.use { it.readBytes() }
        }
    }
    val capeBytes by produceState<ByteArray?>(null, account, refreshWardrobe, capeStamp) {
        value = withContext(Dispatchers.IO) {
            account?.getCapeFile()?.takeIf { it.exists() }
                ?.inputStream()?.use { it.readBytes() }
        }
    }

    val shape = RoundedCornerShape(8.dp)
    Row(
        modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            Modifier.weight(1f).fillMaxHeight()
                .clip(shape).background(SkinCard).border(1.dp, SkinBorder, shape)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("SKINS & WARDROBE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            PetModelView(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                skin = skinBytes,
                cape = capeBytes,
                arm = armModel,
                yaw = Math.toRadians(35.0).toFloat(),
                animation = PetAnimation.Idle,
                tapReaction = PetTapReaction.Interact,
                dragYaw = true
            )
            Text("Seret untuk memutar model", color = SkinGreenText, fontSize = 12.sp)
        }

        Column(
            Modifier.weight(1f).fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Ganti skin dan cape",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Skin, model lengan (classic atau slim), dan cape diatur di lemari pakaian akun.",
                color = Color(0xFFBBB0AC),
                fontSize = 13.sp
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onOpenWardrobe,
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SkinGreen),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("BUKA LEMARI SKIN & CAPE", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

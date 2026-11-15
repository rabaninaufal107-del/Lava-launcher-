package com.movtery.zalithlauncher.ui.lava

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import kotlin.math.sin
import kotlinx.coroutines.delay

/** Penanda supaya layar pembuka hanya tampil sekali per proses aplikasi. */
object LavaSplashState {
    var shown = false
}

@Composable
fun LavaSplashHost() {
    var visible by remember { mutableStateOf(!LavaSplashState.shown) }
    LaunchedEffect(Unit) { LavaSplashState.shown = true }

    if (visible) {
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            val view = LocalView.current
            SideEffect {
                (view.parent as? DialogWindowProvider)?.window?.setDimAmount(0f)
            }
            LavaSplashContent(onFinished = { visible = false })
        }
    }
}

@Composable
private fun LavaSplashContent(onFinished: () -> Unit) {
    val progress = remember { Animatable(0f) }
    val fade = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(2400, easing = LinearEasing))
        delay(300)
        fade.animateTo(0f, tween(500))
        onFinished()
    }

    Box(
        Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = fade.value }
            .background(Color(0xFF1E1A19)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Canvas(Modifier.size(200.dp)) { drawLavaBlock(progress.value) }
            Spacer(Modifier.height(12.dp))
            Text(
                "Lava Launcher",
                color = Color(0xFFE5521B),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun DrawScope.drawLavaBlock(p: Float) {
    val s = size.minDimension

    fun poly(vararg v: Pair<Float, Float>): Path = Path().apply {
        v.forEachIndexed { i, pair ->
            if (i == 0) moveTo(pair.first * s, pair.second * s)
            else lineTo(pair.first * s, pair.second * s)
        }
        close()
    }

    val top = poly(0.5f to 0.10f, 0.92f to 0.32f, 0.5f to 0.54f, 0.08f to 0.32f)
    val left = poly(0.08f to 0.32f, 0.5f to 0.54f, 0.5f to 0.94f, 0.08f to 0.72f)
    val right = poly(0.5f to 0.54f, 0.92f to 0.32f, 0.92f to 0.72f, 0.5f to 0.94f)

    // cahaya di belakang blok, makin terang saat lava menutupi
    drawCircle(
        brush = Brush.radialGradient(
            listOf(Color(0x66FF7A1A), Color.Transparent),
            center = Offset(0.5f * s, 0.55f * s),
            radius = 0.7f * s
        ),
        radius = 0.7f * s,
        center = Offset(0.5f * s, 0.55f * s),
        alpha = p
    )

    // blok rumput awal: sisi tanah + pita rumput + atas hijau
    drawPath(left, Color(0xFF7A5232))
    drawPath(right, Color(0xFF5C3B20))
    drawPath(
        poly(0.08f to 0.32f, 0.5f to 0.54f, 0.5f to 0.66f, 0.08f to 0.44f),
        Color(0xFF4A8A28)
    )
    drawPath(
        poly(0.5f to 0.54f, 0.92f to 0.32f, 0.92f to 0.44f, 0.5f to 0.66f),
        Color(0xFF356B1E)
    )
    drawPath(top, Color(0xFF5DA832))

    val lava = Brush.verticalGradient(
        listOf(Color(0xFFFFE066), Color(0xFFFF9A1F), Color(0xFFE5321B)),
        startY = 0.10f * s,
        endY = 0.94f * s
    )

    // tahap 1: lava menutupi sisi atas
    val f1 = (p / 0.35f).coerceIn(0f, 1f)
    clipPath(top) {
        drawRect(lava, Offset(0f, 0.10f * s), Size(s, f1 * 0.44f * s))
    }

    // tahap 2: lava mengalir turun di kedua sisi
    val f2 = ((p - 0.25f) / 0.75f).coerceIn(0f, 1f)
    val yT = 0.30f + f2 * 0.72f
    val flow = Path().apply {
        moveTo(0f, 0f)
        lineTo(s, 0f)
        var x = s
        while (x >= 0f) {
            val wave = sin(x / s * 16f + p * 10f) * 0.025f
            lineTo(x, (yT + wave) * s)
            x -= s / 40f
        }
        close()
    }
    clipPath(left) { drawPath(flow, lava) }
    clipPath(right) { drawPath(flow, lava, alpha = 0.8f) }

    // garis tepi blok
    val edge = Stroke(width = 0.012f * s)
    drawPath(top, Color(0xFF1A0F0A), style = edge)
    drawPath(left, Color(0xFF1A0F0A), style = edge)
    drawPath(right, Color(0xFF1A0F0A), style = edge)

    // aliran lava dari atas yang menyiram blok
    if (p < 0.75f) {
        val sa = (1f - p / 0.75f).coerceIn(0f, 1f)
        drawRect(
            Color(0xFFFF9A1F),
            Offset(0.47f * s, -1.2f * s),
            Size(0.06f * s, 1.5f * s),
            alpha = sa
        )
    }
}

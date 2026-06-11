package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.QfNavy
import com.example.ui.theme.QfSurface
import com.example.ui.theme.QfSurfaceVariant
import com.example.ui.theme.QfTextPrimary
import com.example.ui.theme.QfTextTertiary
import com.example.ui.theme.QfTurquoise
import kotlinx.coroutines.delay

/**
 * Brand splash: logo centered on dark navy with a soft glow, app name, the
 * Arabic slogan, and a city/queue skyline silhouette along the bottom edge
 * (matches the design-board launch frame). Fades in, then hands off.
 */
@Composable
fun QueueFuelSplashScreen(onFinished: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 450),
        label = "SplashFade"
    )

    LaunchedEffect(Unit) {
        visible = true
        delay(1400)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(QfNavy)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .alpha(alpha),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            QueueFuelLogo(size = 110.dp)

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "QueueFuel",
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = QfTextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "كل الطوابير... بمكان واحد",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = QfTurquoise,
                textAlign = TextAlign.Center
            )
        }

        CitySkylineSilhouette(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(120.dp)
                .alpha(alpha)
        )

        Text(
            text = "كركوك • أربيل • السليمانية",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = QfTextTertiary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
                .alpha(alpha)
        )
    }
}

/**
 * Flat two-layer city skyline with a queue of waiting "cars" (dots) along the
 * base line — the launch-frame motif from the design board.
 */
@Composable
fun CitySkylineSilhouette(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val baseY = h - 40f

        // Back layer — taller, dimmer towers.
        val back = listOf(
            // x-fraction, width-fraction, height-fraction
            Triple(0.00f, 0.10f, 0.78f),
            Triple(0.13f, 0.07f, 0.55f),
            Triple(0.24f, 0.11f, 0.90f),
            Triple(0.40f, 0.08f, 0.62f),
            Triple(0.52f, 0.10f, 0.84f),
            Triple(0.67f, 0.07f, 0.58f),
            Triple(0.78f, 0.11f, 0.92f),
            Triple(0.92f, 0.08f, 0.66f)
        )
        back.forEach { (x, bw, bh) ->
            drawRect(
                color = QfSurface,
                topLeft = Offset(x * w, baseY - bh * (baseY)),
                size = Size(bw * w, bh * baseY)
            )
        }

        // Front layer — shorter, lighter buildings with a minaret-like spire.
        val front = listOf(
            Triple(0.05f, 0.09f, 0.40f),
            Triple(0.19f, 0.06f, 0.30f),
            Triple(0.33f, 0.10f, 0.46f),
            Triple(0.48f, 0.05f, 0.34f),
            Triple(0.59f, 0.09f, 0.44f),
            Triple(0.73f, 0.06f, 0.28f),
            Triple(0.85f, 0.10f, 0.48f)
        )
        front.forEach { (x, bw, bh) ->
            drawRect(
                color = QfSurfaceVariant,
                topLeft = Offset(x * w, baseY - bh * baseY),
                size = Size(bw * w, bh * baseY)
            )
        }
        // Spire / dome accent near the center.
        drawArc(
            color = QfSurfaceVariant,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(0.44f * w, baseY - 0.52f * baseY),
            size = Size(0.06f * w, 0.10f * baseY)
        )

        // Base road line.
        drawLine(
            color = QfTurquoise.copy(alpha = 0.5f),
            start = Offset(0f, baseY + 14f),
            end = Offset(w, baseY + 14f),
            strokeWidth = 2f
        )

        // Queue of waiting dots ("cars in line") fading toward the edge.
        var dotX = w * 0.18f
        var i = 0
        while (dotX < w * 0.82f) {
            drawCircle(
                color = QfTurquoise.copy(alpha = 0.65f - (i % 3) * 0.15f),
                radius = 5f,
                center = Offset(dotX, baseY + 14f)
            )
            dotX += 36f
            i++
        }
    }
}

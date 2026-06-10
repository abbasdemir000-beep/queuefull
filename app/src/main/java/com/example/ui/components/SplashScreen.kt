package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.QfNavy
import com.example.ui.theme.QfTextPrimary
import com.example.ui.theme.QfTurquoise
import kotlinx.coroutines.delay

/**
 * Brand splash: logo centered on dark navy with a soft glow, app name and the
 * Arabic slogan. Fades in quickly, then hands off to the app.
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
            .background(QfNavy),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.alpha(alpha),
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
    }
}

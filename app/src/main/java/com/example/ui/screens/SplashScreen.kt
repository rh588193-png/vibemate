package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.VibePrismMark
import com.example.ui.theme.VibeElectricBlue
import com.example.ui.theme.VibeGradients
import com.example.ui.theme.VibeMutedSlate
import com.example.ui.theme.VibeObsidian
import com.example.ui.theme.VibePurple
import com.example.ui.theme.VibeWhite
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val logoScale = remember { Animatable(0.76f) }
    val contentAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        contentAlpha.animateTo(1f, animationSpec = tween(550, easing = FastOutSlowInEasing))
        logoScale.animateTo(1f, animationSpec = tween(650, easing = FastOutSlowInEasing))
        delay(800)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VibeObsidian)
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_vibe_splash),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.48f)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(VibeGradients.DarkScrim)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(24.dp)
                .alpha(contentAlpha.value)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .scale(logoScale.value)
                    .size(116.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .border(2.dp, VibeGradients.PrismBrand, RoundedCornerShape(30.dp))
                    .background(VibeObsidian.copy(alpha = 0.85f))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_vibemate_logo),
                    contentDescription = "VIBEMATE logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(Modifier.height(24.dp))

            VibePrismMark(size = 32.dp)

            Spacer(Modifier.height(10.dp))

            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displayLarge.copy(
                    color = VibeWhite,
                    letterSpacing = 4.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.tagline),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = VibeElectricBlue,
                    letterSpacing = 1.2.sp
                )
            )
        }

        Surface(
            color = VibeObsidian.copy(alpha = 0.65f),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .safeDrawingPadding()
                .padding(bottom = 28.dp)
                .border(1.dp, VibePurple.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
        ) {
            Text(
                text = stringResource(R.string.founder_credit),
                style = MaterialTheme.typography.labelMedium.copy(color = VibeMutedSlate),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

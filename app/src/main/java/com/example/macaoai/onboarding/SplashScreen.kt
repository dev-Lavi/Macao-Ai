package com.example.macaoai.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.macaoai.R
import kotlinx.coroutines.delay

// Background cream color used across all screens
val BgCream    = Color(0xFFF5F0E8)
val BrownCircle = Color(0xFF926247)

@Composable
fun SplashScreen(onSplashComplete: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(2000L)
        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCream),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Owl jumping image sits on top of the logo
            Image(
                painter            = painterResource(id = R.drawable.owl_jump),
                contentDescription = "Macao AI owl",
                modifier           = Modifier
                    .size(160.dp),
                contentScale       = ContentScale.Fit,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Logo image (the "MACAO Ai" text logo)
            Image(
                painter            = painterResource(id = R.drawable.logo),
                contentDescription = "Macao AI logo",
                modifier           = Modifier
                    .width(220.dp)
                    .height(72.dp),
                contentScale       = ContentScale.Fit,
            )
        }
    }
}

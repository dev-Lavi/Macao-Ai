package ai.macao.app.onboarding

import ai.macao.app.R
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
import kotlinx.coroutines.delay

import ai.macao.app.auth.ClashGroteskFontFamily
import ai.macao.app.theme.MindfulBrown
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.sp

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
            Image(
                painter            = painterResource(id = R.drawable.owl_logo),
                contentDescription = "Macao AI mascot logo",
                modifier           = Modifier.size(180.dp),
                contentScale       = ContentScale.Fit,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = MindfulBrown)) {
                        append("MACAO A")
                    }
                    withStyle(style = SpanStyle(color = Color(0xFF2E86DE))) {
                        append("i")
                    }
                },
                fontFamily = ClashGroteskFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 42.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

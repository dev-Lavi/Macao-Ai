package com.example.macaoai.auth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.macaoai.R

// Mindful Brown/40 and Mindful Brown/10 for the wavy background
private val MindfulBrown40 = Color(0xFFB08060).copy(alpha = 0.40f)
private val MindfulBrown10 = Color(0xFFB08060).copy(alpha = 0.10f)

@Composable
fun OtpSetupScreen(
    initialEmail: String = "",
    onBack: () -> Unit = {},
    onSendOtp: (email: String) -> Unit = {},
) {
    var email by remember { mutableStateOf(initialEmail) }
    val panelOverlap = 60.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AuthBodyCream)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState()),
    ) {
        AuthScreenTopBar(
            onBack = onBack,
            title = "OTP Setup",
        )

        // Owl section with wavy background behind it
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            // Three alternating waves from top-right toward bottom-left
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Wave 1 — top-right to bottom-left (MindfulBrown40)
                val wave1 = Path().apply {
                    moveTo(w, 0f)
                    cubicTo(
                        w * 0.70f, h * 0.15f,
                        w * 0.40f, h * 0.35f,
                        0f, h * 0.55f,
                    )
                    lineTo(0f, h)
                    lineTo(w, h)
                    close()
                }
                drawPath(wave1, color = MindfulBrown40)

                // Wave 2 — middle wave (MindfulBrown10)
                val wave2 = Path().apply {
                    moveTo(w, h * 0.15f)
                    cubicTo(
                        w * 0.65f, h * 0.30f,
                        w * 0.35f, h * 0.50f,
                        0f, h * 0.70f,
                    )
                    lineTo(0f, h)
                    lineTo(w, h)
                    close()
                }
                drawPath(wave2, color = MindfulBrown10)

                // Wave 3 — lower wave (MindfulBrown40)
                val wave3 = Path().apply {
                    moveTo(w, h * 0.30f)
                    cubicTo(
                        w * 0.60f, h * 0.45f,
                        w * 0.30f, h * 0.65f,
                        0f, h * 0.85f,
                    )
                    lineTo(0f, h)
                    lineTo(w, h)
                    close()
                }
                drawPath(wave3, color = MindfulBrown40)
            }

            // Mad Owl — only upper portion visible, lower part covered by white form
            Image(
                painter = painterResource(id = R.drawable.madowl_bg),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .offset(y = 20.dp),
                contentScale = ContentScale.Crop,
            )
        }

        // White form panel — more curved top corners, shifted down with extra top padding
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = -panelOverlap)
                .clip(RoundedCornerShape(topStart = 48.dp, topEnd = 48.dp)) // more curved
                .background(Color.White)
                .padding(horizontal = 28.dp)
                .padding(top = 56.dp, bottom = 48.dp), // more top padding = form lower in panel
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "OTP Verification",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = ClashGroteskFontFamily,
                color = AuthPrimaryBrown,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "We will send a one time message.",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = AuthInterFontFamily,
                color = AuthSubText,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(36.dp)) // increased from 28 to push field down more

            AuthEmailField(
                value = email,
                onValueChange = { email = it },
                label = "",
                elevated = true,
            )

            Spacer(modifier = Modifier.height(32.dp))

            MacaoPrimaryButton(
                text = "Send OTP",
                onClick = { onSendOtp(email) },
            )
        }
    }
}
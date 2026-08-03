package ai.macao.app.auth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.macao.app.R

// ─── Google Font ─────────────────────────────────────────────────────────────

private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

private val InterFontName = GoogleFont("Inter")

internal val AuthInterFontFamily = FontFamily(
    Font(googleFont = InterFontName, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = InterFontName, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = InterFontName, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = InterFontName, fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = InterFontName, fontProvider = provider, weight = FontWeight.ExtraBold),
)

private val ClashGroteskFontName = GoogleFont("Clash Grotesk")

internal val ClashGroteskFontFamily = FontFamily(
    Font(googleFont = ClashGroteskFontName, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = ClashGroteskFontName, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = ClashGroteskFontName, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = ClashGroteskFontName, fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = ClashGroteskFontName, fontProvider = provider, weight = FontWeight.ExtraBold),
)

// ─── Colors ──────────────────────────────────────────────────────────────────

val AuthHeaderBrown = Color(0xFFC1A594)
val AuthBodyCream = Color(0xFFFAF7F2)
val AuthPrimaryBrown = Color(0xFF8B5E3C)
val AuthInputBorder = Color(0xFFC8C4B8)
val AuthSocialCircle = Color(0xFFF0EBE3)
val AuthLabelBrown = Color(0xFF8B5E3C)
val AuthPlaceholder = Color(0xFFB0A090)
val AuthLinkBlue = Color(0xFF4A90D9)
val AuthSubText = Color(0xFF9A8A7A)

private val PillShape = RoundedCornerShape(50)

private fun authHeaderConcaveShape() = GenericShape { size, _ ->
    val curveDepth = size.height * 0.28f
    moveTo(0f, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width, size.height - curveDepth)
    quadraticTo(
        size.width / 2f,
        size.height + curveDepth * 0.15f,
        0f,
        size.height - curveDepth,
    )
    close()
}

// ─── Curved header (Sign Up / Sign In) ───────────────────────────────────────

@Composable
fun CurvedAuthHeader(owlRes: Int) {
    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp) // Increase height to give owl more room
    ) {
        // Curved brown background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width, size.height * 0.75f)
                quadraticBezierTo(
                    size.width / 2f, size.height * 1.1f,
                    0f, size.height * 0.75f
                )
                close()
            }
            drawPath(path, color = Color(0xFFBDA193))
        }

        // Owl image — offset upward so it peeks above the curve
        Image(
            painter = painterResource(id = owlRes),
            contentDescription = "Owl",
            modifier = Modifier
                .size(140.dp) // Increase size so owl isn't cramped
                .offset(y = (-16).dp) // Lift owl up slightly above curve bottom
        )
    }
}

// ─── Circle back button ──────────────────────────────────────────────────────

@Composable
fun HalfArcBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AuthCircleBackButton(onClick = onClick, modifier = modifier)
}

@Composable
fun AuthCircleBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(36.dp)) {
            val strokeWidth = 1.5.dp.toPx()
            val inset = strokeWidth / 2f
            drawCircle(
                color = AuthPrimaryBrown,
                radius = (size.minDimension - strokeWidth) / 2f,
                center = Offset(size.width / 2f, size.height / 2f),
                style = Stroke(width = strokeWidth),
            )
            val cx = size.width * 0.52f
            val cy = size.height / 2f
            val chevronLen = 5.dp.toPx()
            val path = Path().apply {
                moveTo(cx + chevronLen * 0.3f, cy - chevronLen)
                lineTo(cx - chevronLen * 0.5f, cy)
                lineTo(cx + chevronLen * 0.3f, cy + chevronLen)
            }
            drawPath(
                path = path,
                color = AuthPrimaryBrown,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
            )
        }
    }
}

@Composable
fun AuthScreenTopBar(
    onBack: () -> Unit,
    title: String? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AuthCircleBackButton(onClick = onBack)
        if (title != null) {
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = ClashGroteskFontFamily,
                color = AuthPrimaryBrown,
            )
        }
    }
}

// ─── Primary button with arrow ───────────────────────────────────────────────

@Composable
fun MacaoPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AuthPrimaryBrown,
            contentColor = Color.White,
        ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = AuthInterFontFamily,
                color = Color.White,
            )
            Spacer(modifier = Modifier.width(10.dp))
            Icon(
                painter = painterResource(id = R.drawable.arrow_right),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = Color.Unspecified,
            )
        }
    }
}

// ─── Email field ─────────────────────────────────────────────────────────────

@Composable
fun AuthEmailField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Email Address",
    showTrailingChevron: Boolean = false,
    elevated: Boolean = false,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = AuthInterFontFamily,
                color = AuthLabelBrown,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
            )
        }
        val fieldModifier = if (elevated) {
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(4.dp, PillShape, clip = false)
                .clip(PillShape)
                .background(Color.White)
                .border(1.dp, AuthInputBorder, PillShape)
                .padding(horizontal = 18.dp)
        } else {
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(PillShape)
                .background(Color.White)
                .border(1.dp, AuthInputBorder, PillShape)
                .padding(horizontal = 18.dp)
        }
        Row(
            modifier = fieldModifier,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.mail),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = Color.Unspecified,
            )
            Spacer(modifier = Modifier.width(12.dp))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 15.sp,
                    fontFamily = AuthInterFontFamily,
                    color = AuthPrimaryBrown,
                ),
                cursorBrush = SolidColor(Color(0xFF4CAF50)),
                decorationBox = { inner ->
                    if (value.isEmpty()) {
                        Text(
                            text = "Enter your email...",
                            fontSize = 15.sp,
                            fontFamily = AuthInterFontFamily,
                            color = AuthPlaceholder,
                        )
                    }
                    inner()
                },
            )
            if (showTrailingChevron) {
                Text(
                    text = "▾",
                    fontSize = 14.sp,
                    color = AuthPrimaryBrown,
                )
            }
        }
    }
}

// ─── Password field ──────────────────────────────────────────────────────────

@Composable
fun AuthPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    passwordVisible: Boolean,
    onToggleVisibility: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Password",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = AuthInterFontFamily,
            color = AuthLabelBrown,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(4.dp, PillShape, clip = false)
                .clip(PillShape)
                .background(Color.White)
                .border(1.dp, AuthInputBorder, PillShape)
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.lock),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = Color.Unspecified,
            )
            Spacer(modifier = Modifier.width(12.dp))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                textStyle = TextStyle(
                    fontSize = 15.sp,
                    fontFamily = AuthInterFontFamily,
                    color = AuthPrimaryBrown,
                ),
                cursorBrush = SolidColor(Color(0xFF4CAF50)),
                decorationBox = { inner ->
                    if (value.isEmpty()) {
                        Text(
                            text = "Enter your password...",
                            fontSize = 15.sp,
                            fontFamily = AuthInterFontFamily,
                            color = AuthPlaceholder,
                        )
                    }
                    inner()
                },
            )
            Icon(
                painter = painterResource(
                    id = if (passwordVisible) R.drawable.eye else R.drawable.eye_close,
                ),
                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                modifier = Modifier
                    .size(22.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = onToggleVisibility,
                    ),
                tint = Color.Unspecified,
            )
        }
    }
}

// ─── Social login row ────────────────────────────────────────────────────────

@Composable
fun SocialLoginRow(
    modifier: Modifier = Modifier,
    onGoogleClick: () -> Unit = {},
    onInstaClick: () -> Unit = {},
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SocialCircleButton(
            iconRes = R.drawable.google,
            onClick = onGoogleClick,
            contentDescription = "Continue with Google",
        )
        Spacer(modifier = Modifier.width(20.dp))
        SocialCircleButton(
            iconRes = R.drawable.insta,
            onClick = onInstaClick,
            contentDescription = "Continue with Instagram",
        )
    }
}

@Composable
private fun SocialCircleButton(
    iconRes: Int,
    onClick: () -> Unit,
    contentDescription: String,
) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(AuthSocialCircle)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp),
            tint = Color.Unspecified,
        )
    }
}

/** Upward-curving top edge for the OTP bottom panel. */
fun otpPanelShape() = GenericShape { size, _ ->
    val curveDepth = minOf(size.height * 0.08f, 48f)
    moveTo(0f, curveDepth)
    quadraticTo(size.width / 2f, -curveDepth * 0.4f, size.width, curveDepth)
    lineTo(size.width, size.height)
    lineTo(0f, size.height)
    close()
}

package ai.macao.app.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import ai.macao.app.R

// ── Colors ────────────────────────────────────────────────────────────────────
private val OtpBodyCream        = Color(0xFFF2EDE8)
private val OtpPrimaryBrown     = Color(0xFF6B3F1E)
private val OtpActiveBorder     = Color(0xFFA8C5A0)
private val OtpErrorBackground  = Color(0xFFFFDAD6)
private val OtpErrorBorder      = Color(0xFFFF5449)
private val OtpErrorText        = Color(0xFFB3261E)
private val OtpResendBlue       = Color(0xFF1A73E8)
private val OtpCellBackground   = Color(0xFFFFFFFF)
private val OtpCellFilledBg     = Color(0xFF7B4F2E)
private val OtpDigitInactive    = Color(0xFFB0A090)

@Composable
fun EnterOtpScreen(
    onBack: () -> Unit = {},
    onContinue: (otp: String) -> Unit = {},
    onResend: () -> Unit = {},
    errorMessage: String? = null,
) {
    val otpLength = 4
    val digits = remember { Array(otpLength) { mutableStateOf("") } }
    val focusRequesters = remember { Array(otpLength) { FocusRequester() } }
    var focusedIndex by remember { mutableStateOf(0) }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        focusRequesters[0].requestFocus()
    }

    val otp = { digits.joinToString("") { state -> state.value } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OtpBodyCream)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState()),
    ) {
        AuthScreenTopBar(
            onBack = onBack,
            title = "OTP Setup",
        )

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Enter 4 digit OTP Code",
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = ClashGroteskFontFamily,
            color = OtpPrimaryBrown,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
        )

        Spacer(modifier = Modifier.height(48.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        ) {
            repeat(otpLength) { index ->
                val digit = digits[index].value
                val isFilled = digit.isNotEmpty()
                val isActive = focusedIndex == index

                OtpCell(
                    digit = digit,
                    isFilled = isFilled,
                    isActive = isActive,
                    focusRequester = focusRequesters[index],
                    onFocusChange = { focused -> if (focused) focusedIndex = index },
                    onValueChange = { newValue ->
                        val filtered = newValue.filter { it.isDigit() }.take(1)
                        digits[index].value = filtered
                        if (filtered.isNotEmpty() && index < otpLength - 1) {
                            focusRequesters[index + 1].requestFocus()
                        }
                        if (filtered.isEmpty() && index > 0) {
                            focusRequesters[index - 1].requestFocus()
                        }
                    },
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (errorMessage != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(OtpErrorBackground)
                    .border(1.5.dp, OtpErrorBorder, RoundedCornerShape(50.dp))
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.alert),
                    contentDescription = "Error",
                    modifier = Modifier
                        .size(20.dp)
                        .padding(end = 8.dp),
                )
                Text(
                    text = errorMessage,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = AuthInterFontFamily,
                    color = OtpErrorText,
                )
            }
        } else {
            Spacer(modifier = Modifier.height(48.dp))
        }

        Spacer(modifier = Modifier.height(40.dp))

        MacaoPrimaryButton(
            text = "Continue",
            onClick = {
                focusManager.clearFocus()
                onContinue(otp())
            },
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Didn't receive the OTP? ",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = AuthInterFontFamily,
                color = AuthSubText,
            )
            Text(
                text = "Re-send.",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = AuthInterFontFamily,
                color = OtpResendBlue,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onResend,
                ),
            )
        }
    }
}

@Composable
private fun OtpCell(
    digit: String,
    isFilled: Boolean,
    isActive: Boolean,
    focusRequester: FocusRequester,
    onFocusChange: (Boolean) -> Unit,
    onValueChange: (String) -> Unit,
) {
    val cellSize = 76.dp
    val cornerRadius = 28.dp

    val backgroundColor = when {
        isFilled || isActive -> OtpCellFilledBg
        else -> OtpCellBackground
    }

    val borderModifier = if (isActive) {
        Modifier.border(2.5.dp, OtpActiveBorder, RoundedCornerShape(cornerRadius))
    } else {
        Modifier
    }

    Box(
        modifier = Modifier
            .size(cellSize)
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .then(borderModifier),
        contentAlignment = Alignment.Center,
    ) {
        BasicTextField(
            value = digit,
            onValueChange = onValueChange,
            modifier = Modifier
                .size(1.dp)
                .focusRequester(focusRequester)
                .onFocusChanged { onFocusChange(it.isFocused) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            textStyle = TextStyle(color = Color.Transparent),
            singleLine = true,
        )

        Text(
            text = if (digit.isEmpty()) "0" else digit,
            fontSize = 36.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = AuthInterFontFamily,
            color = when {
                isFilled || isActive -> Color.White
                else -> OtpDigitInactive
            },
            textAlign = TextAlign.Center,
        )
    }
}

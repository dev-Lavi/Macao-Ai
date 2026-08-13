package ai.macao.app.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.macao.app.R

@Composable
fun SignInScreen(
    onSignInSuccess: () -> Unit = {},
    onForgotPassword: () -> Unit = {},
    onNavigateToSignUp: () -> Unit = {},
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AuthBodyCream)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        CurvedAuthHeader(owlRes = R.drawable.owl_jump)

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Sign In To Macao.ai",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = ClashGroteskFontFamily,
            color = AuthPrimaryBrown,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        Spacer(modifier = Modifier.height(28.dp))

        AuthEmailField(
            value = email,
            onValueChange = {
                email = it
                errorMessage = null
            },
            showTrailingChevron = true,
            modifier = Modifier.padding(horizontal = 28.dp),
        )

        Spacer(modifier = Modifier.height(18.dp))

        AuthPasswordField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = null
            },
            passwordVisible = passwordVisible,
            onToggleVisibility = { passwordVisible = !passwordVisible },
            modifier = Modifier.padding(horizontal = 28.dp),
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = errorMessage!!,
                color = Color(0xFFB3261E),
                fontSize = 13.sp,
                fontFamily = AuthInterFontFamily,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 28.dp),
            )
        }

        if (statusMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = statusMessage!!,
                color = Color(0xFF2E7D32),
                fontSize = 13.sp,
                fontFamily = AuthInterFontFamily,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 28.dp),
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        MacaoPrimaryButton(
            text = if (isLoading) "Signing In..." else "Sign In",
            onClick = {
                if (isLoading) return@MacaoPrimaryButton
                isLoading = true
                errorMessage = null
                statusMessage = null
                FirebaseAuthHelper.signInWithEmail(
                    email = email,
                    password = password,
                    onSuccess = {
                        isLoading = false
                        onSignInSuccess()
                    },
                    onError = { err ->
                        isLoading = false
                        errorMessage = err
                    },
                )
            },
            modifier = Modifier.padding(horizontal = 28.dp),
        )

        Spacer(modifier = Modifier.height(28.dp))

        SocialLoginRow(
            onGoogleClick = {
                errorMessage = null
                statusMessage = null
                FirebaseAuthHelper.launchGoogleSignIn(
                    context = context,
                    scope = coroutineScope,
                    onSuccess = onSignInSuccess,
                    onError = { err -> errorMessage = err },
                )
            },
        )

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Forgot Password",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = AuthInterFontFamily,
            color = AuthLinkBlue,
            textDecoration = TextDecoration.Underline,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {
                        errorMessage = null
                        statusMessage = null
                        FirebaseAuthHelper.sendPasswordResetEmail(
                            email = email,
                            onSuccess = {
                                statusMessage = "Password reset email sent to $email."
                            },
                            onError = { err -> errorMessage = err },
                        )
                        onForgotPassword()
                    },
                ),
        )

        Spacer(modifier = Modifier.height(24.dp))

        val signUpFooter = buildAnnotatedString {
            withStyle(
                SpanStyle(
                    color = AuthPrimaryBrown,
                    fontFamily = AuthInterFontFamily,
                    fontSize = 14.sp,
                ),
            ) {
                append("Do not have an account? ")
            }
            withStyle(
                SpanStyle(
                    color = AuthLinkBlue,
                    fontFamily = AuthInterFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = TextDecoration.Underline,
                ),
            ) {
                append("signup")
            }
        }
        Text(
            text = signUpFooter,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onNavigateToSignUp,
                ),
        )
    }
}

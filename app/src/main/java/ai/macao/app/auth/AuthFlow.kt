package ai.macao.app.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

enum class AuthEntry {
    SignUp,
    SignIn,
}

private enum class AuthRoute {
    SignUp,
    SignIn,
    Otp,
}

@Composable
fun AuthFlow(
    start: AuthEntry,
    onAuthSuccess: () -> Unit = {},
    onExit: () -> Unit = {},
) {
    var route by remember {
        mutableStateOf(
            when (start) {
                AuthEntry.SignUp -> AuthRoute.SignUp
                AuthEntry.SignIn -> AuthRoute.SignIn
            },
        )
    }

    when (route) {
        AuthRoute.SignUp -> SignUpScreen(
            onBack = onExit,
            onSignUpSuccess = onAuthSuccess,
            onNavigateToSignIn = { route = AuthRoute.SignIn },
        )

        AuthRoute.SignIn -> SignInScreen(
            onSignInSuccess = onAuthSuccess,
            onForgotPassword = { /* Handled inside SignInScreen */ },
            onNavigateToSignUp = { route = AuthRoute.SignUp },
        )

        AuthRoute.Otp -> OtpSetupScreen(
            initialEmail = "",
            onBack = { route = AuthRoute.SignUp },
            onSendOtp = { /* Handled inside OtpSetupScreen */ },
        )
    }
}

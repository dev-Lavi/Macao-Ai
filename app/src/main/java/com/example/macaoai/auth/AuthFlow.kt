package com.example.macaoai.auth

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
    var emailForOtp by remember { mutableStateOf("") }

    when (route) {
        AuthRoute.SignUp -> SignUpScreen(
            onBack = onExit,
            onContinue = { email ->
                emailForOtp = email
                route = AuthRoute.Otp
            },
            onNavigateToSignIn = { route = AuthRoute.SignIn },
        )

        AuthRoute.SignIn -> SignInScreen(
            onSignIn = { /* UI only */ },
            onForgotPassword = { /* UI only */ },
            onNavigateToSignUp = { route = AuthRoute.SignUp },
        )

        AuthRoute.Otp -> OtpSetupScreen(
            initialEmail = emailForOtp,
            onBack = { route = AuthRoute.SignUp },
            onSendOtp = { /* UI only */ },
        )
    }
}

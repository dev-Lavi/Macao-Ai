package ai.macao.app

import ai.macao.app.onboarding.OnboardingScreen
import ai.macao.app.onboarding.SplashScreen
import ai.macao.app.theme.MacaoAITheme
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

enum class AppScreen {
    Splash,
    Onboarding,
    MainApp,
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MacaoAITheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent,
                ) {
                    val firebaseAuth = remember { FirebaseAuth.getInstance() }
                    var currentUser by remember { mutableStateOf(firebaseAuth.currentUser) }
                    var currentScreen by remember { mutableStateOf(AppScreen.Splash) }

                    DisposableEffect(firebaseAuth) {
                        val listener = FirebaseAuth.AuthStateListener { auth ->
                            currentUser = auth.currentUser
                        }
                        firebaseAuth.addAuthStateListener(listener)
                        onDispose {
                            firebaseAuth.removeAuthStateListener(listener)
                        }
                    }

                    when (currentScreen) {
                        AppScreen.Splash -> {
                            SplashScreen(
                                onSplashComplete = {
                                    currentScreen = if (currentUser != null) {
                                        AppScreen.MainApp
                                    } else {
                                        AppScreen.Onboarding
                                    }
                                },
                            )
                        }

                        AppScreen.Onboarding -> {
                            OnboardingScreen(
                                onAuthSuccess = {
                                    currentScreen = AppScreen.MainApp
                                },
                            )
                        }

                        AppScreen.MainApp -> {
                            MainDashboardScreen(
                                userEmail = currentUser?.email ?: currentUser?.displayName ?: "Logged In User",
                                onSignOut = {
                                    firebaseAuth.signOut()
                                    currentScreen = AppScreen.Onboarding
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainDashboardScreen(
    userEmail: String,
    onSignOut: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F0E8))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Welcome to Macao.ai!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6B3F1E),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Logged in as:\n$userEmail",
            fontSize = 16.sp,
            color = Color(0xFF4A4A4A),
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onSignOut,
        ) {
            Text("Sign Out")
        }
    }
}
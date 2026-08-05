package ai.macao.app

import ai.macao.app.onboarding.OnboardingScreen
import ai.macao.app.onboarding.SplashScreen
import ai.macao.app.onboarding.data.repository.UserRepository
import ai.macao.app.onboarding.ui.ProfileSetupFlow
import ai.macao.app.theme.MacaoAITheme
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import kotlinx.coroutines.launch

// ─── App screen enum ─────────────────────────────────────────────────────────

enum class AppScreen {
    Splash,
    Onboarding,     // intro slides + auth (sign in / sign up)
    ProfileSetup,   // 5-step profile setup (new users only)
    MainApp,
}

// ─── MainActivity ─────────────────────────────────────────────────────────────

class MainActivity : ComponentActivity() {

    private val userRepository = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MacaoAITheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color    = Color.Transparent,
                ) {
                    val firebaseAuth = remember { FirebaseAuth.getInstance() }
                    var currentUser by remember { mutableStateOf(firebaseAuth.currentUser) }
                    var currentScreen by remember { mutableStateOf(AppScreen.Splash) }
                    var isCheckingOnboarding by remember { mutableStateOf(false) }
                    val coroutineScope = rememberCoroutineScope()

                    // ── Auth state listener ───────────────────────────────────
                    DisposableEffect(firebaseAuth) {
                        val listener = FirebaseAuth.AuthStateListener { auth ->
                            currentUser = auth.currentUser
                        }
                        firebaseAuth.addAuthStateListener(listener)
                        onDispose { firebaseAuth.removeAuthStateListener(listener) }
                    }

                    // ── Screen router ─────────────────────────────────────────
                    when {
                        isCheckingOnboarding -> OnboardingCheckScreen()

                        currentScreen == AppScreen.Splash -> {
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

                        currentScreen == AppScreen.Onboarding -> {
                            OnboardingScreen(
                                onAuthSuccess = {
                                    // Auth succeeded — check if this user has already
                                    // completed onboarding to decide where to route them.
                                    isCheckingOnboarding = true
                                    coroutineScope.launch {
                                        val profile = userRepository.getUserProfile()
                                        val completed = profile.getOrNull()?.hasCompletedOnboarding == true
                                        isCheckingOnboarding = false
                                        currentScreen = if (completed) {
                                            AppScreen.MainApp
                                        } else {
                                            // Ensure base user document exists before setup
                                            userRepository.createUserIfNotExists()
                                            AppScreen.ProfileSetup
                                        }
                                    }
                                },
                            )
                        }

                        currentScreen == AppScreen.ProfileSetup -> {
                            ProfileSetupFlow(
                                onSetupComplete = {
                                    currentScreen = AppScreen.MainApp
                                },
                            )
                        }

                        currentScreen == AppScreen.MainApp -> {
                            MainDashboardScreen(
                                userEmail = currentUser?.email
                                    ?: currentUser?.displayName
                                    ?: "Logged In User",
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

// ─── Interim loading screen shown while checking onboarding status ────────────

@Composable
private fun OnboardingCheckScreen() {
    Box(
        modifier         = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F0E8)),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            color       = Color(0xFFE8572A),
            modifier    = Modifier.size(48.dp),
            strokeWidth = 4.dp,
        )
    }
}

// ─── Main dashboard (unchanged) ──────────────────────────────────────────────

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
            text       = "Welcome to Macao.ai!",
            fontSize   = 24.sp,
            fontWeight = FontWeight.Bold,
            color      = Color(0xFF6B3F1E),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text     = "Logged in as:\n$userEmail",
            fontSize = 16.sp,
            color    = Color(0xFF4A4A4A),
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onSignOut) {
            Text("Sign Out")
        }
    }
}
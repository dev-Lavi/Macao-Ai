package ai.macao.app.home

import ai.macao.app.R
import ai.macao.app.onboarding.data.model.UserProfile
import ai.macao.app.onboarding.data.repository.UserRepository
import ai.macao.app.theme.AppBackground
import ai.macao.app.theme.MindfulBrown
import ai.macao.app.theme.MindfulBrown10
import ai.macao.app.theme.MindfulBrown40
import ai.macao.app.theme.MindfulBrown60
import ai.macao.app.theme.MindfulBrown80
import ai.macao.app.theme.OrangeAccent
import android.widget.Toast
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import ai.macao.app.data.network.MacaoApiClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

// ─── Font ────────────────────────────────────────────────────────────────────

private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage   = "com.google.android.gms",
    certificates      = R.array.com_google_android_gms_fonts_certs,
)

private val SettingsInterFont = FontFamily(
    Font(googleFont = GoogleFont("Inter"), fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Inter"), fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("Inter"), fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = GoogleFont("Inter"), fontProvider = provider, weight = FontWeight.Bold),
)

// ─── AccountSettingsScreen ────────────────────────────────────────────────────

@Composable
fun AccountSettingsScreen(
    userRepository: UserRepository,
    onBack: () -> Unit,
    onSignOut: () -> Unit,
    selectedTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    // User states
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }

    // Launcher for device photo picker to upload picture to Render backend
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            isSaving = true
            coroutineScope.launch {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bytes = inputStream?.readBytes()
                    inputStream?.close()
                    if (bytes != null) {
                        val requestFile = bytes.toRequestBody(
                            (context.contentResolver.getType(uri) ?: "image/*").toMediaTypeOrNull()
                        )
                        val body = okhttp3.MultipartBody.Part.createFormData("image", "profile.jpg", requestFile)
                        val response = MacaoApiClient.apiService.uploadProfileImage(body)
                        if (response.success && response.data != null) {
                            userProfile = response.data
                            Toast.makeText(context, "Profile picture uploaded successfully!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Upload failed: ${response.error?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error uploading: ${e.message}", Toast.LENGTH_LONG).show()
                } finally {
                    isSaving = false
                }
            }
        }
    }
    var nameInput by remember { mutableStateOf("") }
    var usernameInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }

    // Connection states
    var fbConnected by remember { mutableStateOf(false) }
    var googleConnected by remember { mutableStateOf(true) }

    // Preferences states
    var soundEffects by remember { mutableStateOf(true) }
    var animations by remember { mutableStateOf(true) }
    var darkModeOption by remember { mutableStateOf("OFF") }
    var motivationalMessages by remember { mutableStateOf(true) }
    var listeningExercises by remember { mutableStateOf(false) }

    var showDeleteConfirmation by remember { mutableStateOf(false) }

    // Load user profile on start
    LaunchedEffect(Unit) {
        val result = userRepository.getUserProfile()
        if (result.isSuccess) {
            val profile = result.getOrNull()
            userProfile = profile
            if (profile != null) {
                nameInput = profile.displayName
                usernameInput = profile.username
                emailInput = profile.email
                fbConnected = profile.facebookConnected
                googleConnected = profile.googleConnected
                soundEffects = profile.soundEffects
                animations = profile.animations
                darkModeOption = profile.darkMode
                motivationalMessages = profile.motivationalMessages
                listeningExercises = profile.listeningExercises
            }
        } else {
            Toast.makeText(context, "Failed to load settings: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
        }
        isLoading = false
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = AppBackground,
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = OrangeAccent)
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    // ─── Header: Solid bright green top section ────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(84.dp)
                            .background(Color(0xFF58CC02)),
                    ) {
                        // Back button on the left
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 16.dp)
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                                .clickable(onClick = onBack),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.back),
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp),
                            )
                        }

                        // Three dots menu button on the right
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 16.dp)
                                .size(36.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, Color.White, CircleShape)
                                .clickable { /* Actions */ },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.threedots),
                                contentDescription = "Menu",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }

                    // Scrollable content area
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 20.dp),
                    ) {
                        // Title and SAVE CHANGES button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Account",
                                fontFamily = SettingsInterFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = MindfulBrown,
                            )

                            // Save Changes Button
                            Button(
                                onClick = {
                                    isSaving = true
                                    coroutineScope.launch {
                                        val settingsMap = mapOf(
                                            "facebookConnected" to fbConnected,
                                            "googleConnected" to googleConnected,
                                            "soundEffects" to soundEffects,
                                            "animations" to animations,
                                            "darkMode" to darkModeOption,
                                            "motivationalMessages" to motivationalMessages,
                                            "listeningExercises" to listeningExercises
                                        )
                                        val saveResult = userRepository.updateUserProfile(
                                            name = nameInput,
                                            username = usernameInput,
                                            settings = settingsMap
                                        )
                                        isSaving = false
                                        if (saveResult.isSuccess) {
                                            Toast.makeText(context, "Changes saved successfully", Toast.LENGTH_SHORT).show()
                                            onBack()
                                        } else {
                                            Toast.makeText(context, "Failed to save: ${saveResult.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                },
                                enabled = !isSaving,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSaving) MindfulBrown10 else Color(0xFFE0E0E0),
                                    contentColor = if (isSaving) MindfulBrown40 else MindfulBrown80,
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            ) {
                                if (isSaving) {
                                    CircularProgressIndicator(color = MindfulBrown80, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                } else {
                                    Text(
                                        text = "SAVE CHANGES",
                                        fontFamily = SettingsInterFont,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        // ─── Profile Picture Row ───────────────────────────────────────
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            LabelColumn(text = "Profile\npicture")
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            // Avatar preview
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(MindfulBrown10)
                                    .border(1.dp, MindfulBrown40, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                val currentUrl = userProfile?.profileImageUrl
                                if (!currentUrl.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = currentUrl,
                                        contentDescription = "Avatar Preview",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text(
                                        text = nameInput.take(1).uppercase(),
                                        fontFamily = SettingsInterFont,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 24.sp,
                                        color = MindfulBrown80,
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Choose file button
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .border(1.5.dp, MindfulBrown10, RoundedCornerShape(16.dp))
                                            .background(Color.White)
                                            .clickable(enabled = !isSaving) { 
                                                photoPickerLauncher.launch("image/*") 
                                            }
                                            .padding(horizontal = 14.dp, vertical = 8.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = "CHOOSE FILE",
                                            fontFamily = SettingsInterFont,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color(0xFF00A2FF),
                                        )
                                    }

                                    // Remove button if image exists
                                    if (!userProfile?.profileImageUrl.isNullOrEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(16.dp))
                                                .border(1.5.dp, Color(0xFFFF4D4D).copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                                                .background(Color.White)
                                                .clickable(enabled = !isSaving) {
                                                    isSaving = true
                                                    coroutineScope.launch {
                                                        try {
                                                            val response = MacaoApiClient.apiService.deleteProfileImage()
                                                            if (response.success && response.data != null) {
                                                                userProfile = response.data
                                                                Toast.makeText(context, "Profile picture removed!", Toast.LENGTH_SHORT).show()
                                                            } else {
                                                                Toast.makeText(context, "Removal failed: ${response.error?.message}", Toast.LENGTH_LONG).show()
                                                            }
                                                        } catch (e: Exception) {
                                                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                                        } finally {
                                                            isSaving = false
                                                        }
                                                    }
                                                }
                                                .padding(horizontal = 14.dp, vertical = 8.dp),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text(
                                                text = "REMOVE",
                                                fontFamily = SettingsInterFont,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = Color(0xFFFF4D4D),
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Maximum image size is 5 MB",
                                    fontFamily = SettingsInterFont,
                                    fontSize = 11.sp,
                                    color = MindfulBrown40,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // ─── Name TextField ────────────────────────────────────────────
                        SettingsRow(labelText = "Name") {
                            SettingsTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                placeholder = "Enter your name",
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // ─── Username TextField ────────────────────────────────────────
                        SettingsRow(labelText = "Username") {
                            SettingsTextField(
                                value = usernameInput,
                                onValueChange = { usernameInput = it },
                                placeholder = "Enter username",
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // ─── Email TextField (Read Only) ───────────────────────────────
                        SettingsRow(labelText = "Email") {
                            SettingsTextField(
                                value = emailInput,
                                onValueChange = {},
                                enabled = false,
                                placeholder = "Email",
                            )
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        // ─── Facebook Connect Switch ───────────────────────────────────
                        SettingsRow(labelText = "Facebook\nConnect") {
                            SettingsSwitch(
                                checked = fbConnected,
                                onCheckedChange = { fbConnected = it },
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // ─── Google Connect Switch ─────────────────────────────────────
                        SettingsRow(labelText = "Google\nConnect") {
                            SettingsSwitch(
                                checked = googleConnected,
                                onCheckedChange = { googleConnected = it },
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // ─── Sound effects Switch ──────────────────────────────────────
                        SettingsRow(labelText = "Sound effects") {
                            SettingsSwitch(
                                checked = soundEffects,
                                onCheckedChange = { soundEffects = it },
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // ─── Animations Switch ─────────────────────────────────────────
                        SettingsRow(labelText = "Animations") {
                            SettingsSwitch(
                                checked = animations,
                                onCheckedChange = { animations = it },
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // ─── Dark Mode Dropdown ────────────────────────────────────────
                        SettingsRow(labelText = "Dark mode") {
                            var dropdownExpanded by remember { mutableStateOf(false) }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.5.dp, MindfulBrown10, RoundedCornerShape(12.dp))
                                    .background(Color.White)
                                    .clickable { dropdownExpanded = true }
                                    .padding(horizontal = 14.dp),
                                contentAlignment = Alignment.CenterStart,
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = darkModeOption,
                                        fontFamily = SettingsInterFont,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MindfulBrown80,
                                    )
                                    Icon(
                                        painter = painterResource(id = R.drawable.arrow_right),
                                        contentDescription = "Expand",
                                        tint = MindfulBrown60,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                                DropdownMenu(
                                    expanded = dropdownExpanded,
                                    onDismissRequest = { dropdownExpanded = false },
                                    modifier = Modifier.background(Color.White),
                                ) {
                                    listOf("OFF", "ON", "SYSTEM").forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(text = option, color = MindfulBrown80) },
                                            onClick = {
                                                darkModeOption = option
                                                dropdownExpanded = false
                                            },
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // ─── Motivational Messages Switch ──────────────────────────────
                        SettingsRow(labelText = "Motivational\nmessages") {
                            SettingsSwitch(
                                checked = motivationalMessages,
                                onCheckedChange = { motivationalMessages = it },
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // ─── Listening Exercises Switch ────────────────────────────────
                        SettingsRow(labelText = "Listening\nexercises") {
                            SettingsSwitch(
                                checked = listeningExercises,
                                onCheckedChange = { listeningExercises = it },
                            )
                        }

                        Spacer(modifier = Modifier.height(36.dp))

                        // ─── Actions Section ───────────────────────────────────────────
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(20.dp),
                        ) {
                            // LOGOUT
                            Text(
                                text = "LOGOUT",
                                fontFamily = SettingsInterFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MindfulBrown60,
                                modifier = Modifier
                                    .clickable {
                                        onSignOut()
                                    }
                                    .padding(vertical = 4.dp),
                            )

                            // EXPORT MY DATA
                            Text(
                                text = "EXPORT MY DATA",
                                fontFamily = SettingsInterFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MindfulBrown60,
                                modifier = Modifier
                                    .clickable {
                                        Toast.makeText(context, "Exporting account data to your email...", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(vertical = 4.dp),
                            )

                            // DELETE MY ACCOUNT
                            Text(
                                text = "DELETE MY ACCOUNT",
                                fontFamily = SettingsInterFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.Red,
                                modifier = Modifier
                                    .clickable { showDeleteConfirmation = true }
                                    .padding(vertical = 4.dp),
                            )
                        }

                        Spacer(modifier = Modifier.height(100.dp)) // padding for bottom nav
                    }
                }

                BottomNavBar(
                    selectedTab = selectedTab,
                    onTabSelected = onTabSelected,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }

    // Deletion confirmation modal
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = {
                Text(
                    text = "Delete Account?",
                    fontFamily = SettingsInterFont,
                    fontWeight = FontWeight.Bold,
                    color = MindfulBrown,
                )
            },
            text = {
                Text(
                    text = "This action is permanent and will delete all your settings, streaks, progress, and account information from MacaoAI.",
                    fontFamily = SettingsInterFont,
                    color = MindfulBrown80,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation = false
                        isLoading = true
                        coroutineScope.launch {
                            val deleteResult = userRepository.deleteAccount()
                            isLoading = false
                            if (deleteResult.isSuccess) {
                                Toast.makeText(context, "Your account has been deleted.", Toast.LENGTH_LONG).show()
                                onSignOut()
                            } else {
                                Toast.makeText(context, "Failed to delete account: ${deleteResult.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                ) {
                    Text(text = "Delete Permanent", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text(text = "Cancel", color = MindfulBrown60)
                }
            },
            containerColor = Color.White,
        )
    }
}

// ─── Settings helper layout structures ────────────────────────────────────────

@Composable
private fun LabelColumn(text: String) {
    Box(
        modifier = Modifier
            .width(90.dp)
            .padding(top = 10.dp),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Text(
            text = text,
            fontFamily = SettingsInterFont,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = MindfulBrown80,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun SettingsRow(labelText: String, content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LabelColumn(text = labelText)
        Spacer(modifier = Modifier.width(16.dp))
        content()
    }
}

// ─── Custom Textfield Styled matching Settings ───────────────────────────────

@Composable
private fun RowScope.SettingsTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    enabled: Boolean = true,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        singleLine = true,
        textStyle = TextStyle(
            fontFamily = SettingsInterFont,
            fontSize = 15.sp,
            color = if (enabled) MindfulBrown80 else MindfulBrown40,
        ),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = 1.5.dp,
                        color = if (enabled) MindfulBrown10 else MindfulBrown10.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(if (enabled) Color.White else Color(0xFFF0F0F0))
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        fontFamily = SettingsInterFont,
                        fontSize = 14.sp,
                        color = MindfulBrown40,
                    )
                }
                innerTextField()
            }
        },
        modifier = Modifier.weight(1f),
    )
}

// ─── Animated custom toggle switches matching mockup ─────────────────────────

@Composable
private fun SettingsSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val trackBg = if (checked) Color(0xFF00A2FF) else Color(0xFFE0E0E0)
    val thumbOffset by animateOffsetAsState(
        targetValue = if (checked) Offset(22f, 0f) else Offset(0f, 0f),
        label = "switch_thumb",
    )

    Box(
        modifier = Modifier
            .size(54.dp, 32.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(trackBg)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onCheckedChange(!checked) },
            )
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset.x.dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(Color.White)
                .shadow(2.dp, CircleShape),
        )
    }
}

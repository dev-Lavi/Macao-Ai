# Firebase Authentication Integration Guide (MacaoAI)

## Overview
This document explains how Firebase Authentication was integrated into the MacaoAI Android application.

### Tech Stack
- Kotlin
- Jetpack Compose
- Gradle Kotlin DSL
- Version Catalog (`libs.versions.toml`)
- Firebase Authentication
- Google Credential Manager

---

# 1. Firebase Setup

1. Create Firebase project.
2. Register Android app with package:
   `ai.macao.app`
3. Download `google-services.json`.
4. Place it in:

```text
app/google-services.json
```

5. Enable Authentication → Email/Password (and Google if required).

---

# 2. Files Modified

## gradle/libs.versions.toml

Added:
- google-services plugin
- firebase-bom
- firebase-auth
- credentials
- googleid (if Google Sign-In is used)

## build.gradle.kts (Project)

Added:

```kotlin
alias(libs.plugins.google.services) apply false
```

## app/build.gradle.kts

Applied plugin:

```kotlin
alias(libs.plugins.google.services)
```

Added dependencies:

```kotlin
implementation(platform(libs.firebase.bom))
implementation(libs.firebase.auth)
```

---

# 3. Authentication Flow

```
Launch App
      │
      ▼
FirebaseAuth.currentUser
      │
 ┌────┴─────┐
 │          │
Logged Out  Logged In
 │          │
 ▼          ▼
Sign In   Dashboard
```

---

# 4. Project Classes

## FirebaseAuthHelper.kt

Responsibilities:

- Sign Up
- Sign In
- Google Sign-In
- Password Reset
- Sign Out

## MainActivity

Observes FirebaseAuth.AuthStateListener.

## SignInScreen

Calls FirebaseAuthHelper.signIn()

## SignUpScreen

Calls FirebaseAuthHelper.signUp()

---

# 5. Important Firebase Terms

## SHA-1

A certificate fingerprint identifying your Android signing key.

Used for:

- Google Sign-In
- Phone Authentication
- Dynamic Links

Example:

```
FD:F6:27:47:04:56:4D:81:F8:35:90:32:C9:81:66:B0:FE:1C:DA:E6
```

---

## SHA-256

A stronger fingerprint than SHA-1.

Recommended to register alongside SHA-1.

---

## MD5

Older fingerprint format.

Rarely used today.

---

## Debug Keystore

Automatically created by Android Studio.

Location:

```
C:\Users\<username>\.android\debug.keystore
```

Used while developing.

---

## Release Keystore

Your production signing key.

Never lose it.

---

## Alias

Friendly name inside a keystore.

Example:

```
AndroidDebugKey
```

---

## Namespace

Kotlin package namespace.

Example:

```kotlin
namespace = "ai.macao.app"
```

---

## Application ID

Unique Play Store identifier.

```kotlin
applicationId = "ai.macao.app"
```

---

## google-services.json

Configuration downloaded from Firebase.

Contains:

- API Keys
- Project ID
- App ID
- OAuth configuration

Never edit manually.

---

## Firebase BoM

Bill of Materials.

Lets Firebase libraries use compatible versions.

---

## Version Catalog

`libs.versions.toml`

Stores versions in one place.

---

## Credential Manager

Modern Android API replacing old Google Sign-In APIs.

---

## Google ID Token

Identity token received from Google.

Exchanged for Firebase credentials using:

```kotlin
GoogleAuthProvider.getCredential()
```

---

# 6. Firebase Console Checklist

- Create project
- Register Android App
- Download google-services.json
- Add SHA-1
- Add SHA-256
- Enable Email/Password
- Enable Google Sign-In (optional)

---

# 7. Gradle Verification

Run:

```bash
./gradlew assembleDebug
```

Expected:

```
BUILD SUCCESSFUL
```

Generate fingerprints:

```bash
./gradlew signingReport
```

---

# 8. Common Errors

## Default FirebaseApp is not initialized

Cause:
google-services.json missing.

---

## API Key invalid

Wrong Firebase project.

---

## Sign-in failed

Wrong email/password.

---

## Google Sign-In fails

Usually missing SHA-1 or OAuth client.

---

# 9. Future Improvements

- Phone OTP
- Anonymous Auth
- Email Verification
- Account Linking
- Multi-factor Authentication

---

# 10. Quick Commands

```bash
./gradlew signingReport
./gradlew assembleDebug
./gradlew clean
```

---

# 11. Summary of Your Integration

- Version Catalog detected.
- Google Services plugin configured.
- Firebase BoM added.
- Firebase Authentication added.
- google-services.json placed correctly.
- Auth helper implemented.
- Sign In connected.
- Sign Up connected.
- Password Reset connected.
- Google Sign-In integrated using Credential Manager.
- AuthStateListener added.
- Navigation based on authentication state implemented.

This document should be updated whenever authentication-related files or flows change.

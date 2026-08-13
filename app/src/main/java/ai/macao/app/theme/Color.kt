package ai.macao.app.theme

import androidx.compose.ui.graphics.Color

// ─── App-wide color palette ────────────────────────────────────────────────────
//
// Base: #F7F4F2 (soft warm white background)
// MindfulBrown: #5C3D2E  (earthy, warm, calming)
//   Used at 80 / 60 / 40 / 10 opacity variants throughout the app.

/** Page / scaffold background — warm off-white */
val AppBackground    = Color(0xFFF7F4F2)

/** Core brand brown */
val MindfulBrown     = Color(0xFF5C3D2E)

/** 80% — primary text, headings */
val MindfulBrown80   = MindfulBrown.copy(alpha = 0.80f)

/** 60% — secondary text, labels, subtitles */
val MindfulBrown60   = MindfulBrown.copy(alpha = 0.60f)

/** 40% — tertiary text, hints, placeholders */
val MindfulBrown40   = MindfulBrown.copy(alpha = 0.40f)

/** 10% — surface tints, pill backgrounds, dividers */
val MindfulBrown10   = MindfulBrown.copy(alpha = 0.10f)

// ─── Accent / interactive colours ─────────────────────────────────────────────

/** Orange accent (CTA buttons, borders, active indicators) */
val OrangeAccent     = Color(0xFFE8572A)

/** Gold / checkpoint colour */
val GoldAccent       = Color(0xFFFFBF00)

/** Active level circle — purple gradient start */
val PurpleLevel      = Color(0xFF9B59B6)

/** Active level circle — purple gradient end */
val PurpleLevelDark  = Color(0xFF6C3483)

// ─── Legacy Material colour slots (still required by Theme.kt) ────────────────

val Purple80         = Color(0xFFD0BCFF)
val PurpleGrey80     = Color(0xFFCCC2DC)
val Pink80           = Color(0xFFEFB8C8)

val Purple40         = Color(0xFF6650a4)
val PurpleGrey40     = Color(0xFF625b71)
val Pink40           = Color(0xFF7D5260)
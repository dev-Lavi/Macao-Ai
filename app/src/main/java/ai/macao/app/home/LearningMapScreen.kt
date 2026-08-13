package ai.macao.app.home

import ai.macao.app.R
import ai.macao.app.home.data.SampleStageData
import ai.macao.app.home.data.StageDecoration
import ai.macao.app.home.data.StageModel
import ai.macao.app.home.data.StageState
import ai.macao.app.theme.GoldAccent
import ai.macao.app.theme.MindfulBrown
import ai.macao.app.theme.MindfulBrown10
import ai.macao.app.theme.MindfulBrown40
import ai.macao.app.theme.MindfulBrown60
import ai.macao.app.theme.MindfulBrown80
import ai.macao.app.theme.OrangeAccent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import ai.macao.app.auth.ClashGroteskFontFamily
import androidx.compose.material3.CardDefaults

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Vertical learning path map screen.
 * Displays floating stage platforms (stage.png), connecting stone paths (path.png),
 * stage state indicators, badges, avatar bubbles, and floating mascot decorations.
 */
@Composable
fun LearningMapScreen(
    userName: String,
    stages: List<StageModel> = SampleStageData.sampleStages,
    onStageClick: (StageModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val activeStage = remember(stages) {
        stages.find { it.state == StageState.CURRENT } ?: stages.firstOrNull()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 120.dp), // Clear top bar and bottom nav
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Journey Card Header showing current active level details
            LevelJourneyHeader(stage = activeStage)

            Spacer(modifier = Modifier.height(16.dp))

            stages.forEachIndexed { index, stage ->
                // Render stage island node
                StageNode(
                    stage = stage,
                    userName = userName,
                    onStageClick = { onStageClick(stage) },
                )

                // Render connecting path of stepping stones to the next stage
                if (index < stages.lastIndex) {
                    val nextStage = stages[index + 1]
                    ConnectingPath(
                        currentXOffset = stage.xOffsetDp.dp,
                        nextXOffset = nextStage.xOffsetDp.dp,
                        stageIndex = index,
                    )
                }
            }
        }
    }
}

/**
 * Level Journey Card Header showing active level details, estimated study time, and owl_books mascot.
 */
@Composable
private fun LevelJourneyHeader(
    stage: StageModel?,
    modifier: Modifier = Modifier
) {
    if (stage == null) return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .shadow(6.dp, RoundedCornerShape(24.dp), spotColor = MindfulBrown.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "YOUR CURRENT LEVEL",
                    fontFamily = ClashGroteskFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = OrangeAccent,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stage.lessonData.levelTitle,
                    fontFamily = ClashGroteskFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MindfulBrown
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Active Unit: ${stage.title}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MindfulBrown80
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Keep going! You're making awesome progress.",
                    fontSize = 11.sp,
                    color = MindfulBrown60,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Image(
                painter = painterResource(id = R.drawable.owl_books),
                contentDescription = "Active Level Mascot",
                modifier = Modifier.size(76.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

/**
 * Single Stage Platform Node using stage.png as the floating island asset.
 */
@Composable
private fun StageNode(
    stage: StageModel,
    userName: String,
    onStageClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isPressed by remember { mutableStateOf(false) }

    // Tactile press scale spring animation
    val scaleAnim by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(stiffness = 400f, dampingRatio = 0.6f),
        label = "stageScale",
    )

    // Pulsing aura animation for CURRENT unlocked stage
    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (stage.state == StageState.CURRENT) 1.08f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseScale",
    )

    // Subtle floating animation for current stage
    val floatY by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = if (stage.state == StageState.CURRENT) 3f else -3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "floatY",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .offset(x = stage.xOffsetDp.dp, y = floatY.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // ── Floating Avatar / Player Indicator above current stage ───
            if (stage.hasPlayerAvatar && stage.state == StageState.CURRENT) {
                PlayerAvatarBadge(
                    userName = userName,
                    modifier = Modifier.padding(bottom = 6.dp),
                )
            }

            // ── Stage Island Container ─────────────────────────────────────
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = scaleAnim
                        scaleY = scaleAnim
                    }
                    .pointerInput(stage.state) {
                        detectTapGestures(
                            onPress = {
                                if (stage.state != StageState.LOCKED) {
                                    isPressed = true
                                    tryAwaitRelease()
                                    isPressed = false
                                }
                            },
                            onTap = {
                                if (stage.state != StageState.LOCKED) {
                                    onStageClick()
                                }
                            },
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                // Glow aura ring for current stage
                if (stage.state == StageState.CURRENT) {
                    Box(
                        modifier = Modifier
                            .size(135.dp, 95.dp)
                            .scale(pulseScale)
                            .shadow(
                                elevation = 20.dp,
                                shape = RoundedCornerShape(40.dp),
                                spotColor = Color(0xFFFFB703).copy(alpha = 0.5f),
                                ambientColor = Color(0xFFFB8500).copy(alpha = 0.3f),
                            )
                            .background(
                                color = Color(0xFFFFB703).copy(alpha = 0.18f),
                                shape = RoundedCornerShape(40.dp),
                            ),
                    )
                }

                // Main Stage Asset (stage.png)
                val colorFilter = if (stage.state == StageState.LOCKED) {
                    ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0.2f) })
                } else null

                Image(
                    painter = painterResource(id = R.drawable.stage),
                    contentDescription = "Stage ${stage.levelNumber}: ${stage.title}",
                    colorFilter = colorFilter,
                    modifier = Modifier
                        .size(width = 125.dp, height = 85.dp)
                        .alpha(if (stage.state == StageState.LOCKED) 0.65f else 1f),
                    contentScale = ContentScale.Fit,
                )

                // ── Stage Platform Top Decoration ─────────────────────────
                StageDecorationOverlay(decoration = stage.decoration)

                // ── Lock / Completion / Start Badge ──────────────────────
                when (stage.state) {
                    StageState.LOCKED -> {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .shadow(4.dp, CircleShape)
                                .clip(CircleShape)
                                .background(Color(0xFF3D2C24).copy(alpha = 0.85f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.lock),
                                contentDescription = "Locked Stage",
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }

                    StageState.COMPLETED -> {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .shadow(6.dp, CircleShape)
                                .clip(CircleShape)
                                .background(Color(0xFF2A9D8F)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.tick),
                                contentDescription = "Completed Stage",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }

                    StageState.CURRENT -> {
                        // "START" pill indicator floating near front edge of stage
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .offset(y = 10.dp)
                                .shadow(8.dp, RoundedCornerShape(20.dp), spotColor = OrangeAccent.copy(0.4f))
                                .clip(RoundedCornerShape(20.dp))
                                .background(OrangeAccent)
                                .padding(horizontal = 14.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "START",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                letterSpacing = 1.sp,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Stage Title Label under island
            Text(
                text = stage.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (stage.state == StageState.LOCKED) MindfulBrown40 else MindfulBrown,
            )
        }
    }
}

/**
 * Renders decorative items sitting on top of stage island (Lightning, Coin, Chest, Star).
 */
@Composable
private fun StageDecorationOverlay(decoration: StageDecoration) {
    val infiniteTransition = rememberInfiniteTransition(label = "badgeBob")
    val bobY by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "bobY",
    )

    Box(
        modifier = Modifier
            .offset(y = (-22).dp + bobY.dp),
        contentAlignment = Alignment.Center,
    ) {
        when (decoration) {
            StageDecoration.LIGHTNING -> {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .shadow(8.dp, CircleShape, spotColor = GoldAccent)
                        .clip(CircleShape)
                        .background(GoldAccent),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "⚡",
                        fontSize = 18.sp,
                    )
                }
            }

            StageDecoration.COIN -> {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .shadow(6.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color(0xFFFFD166))
                        .border(2.dp, Color(0xFFF7B801), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "🪙",
                        fontSize = 16.sp,
                    )
                }
            }

            StageDecoration.CHEST -> {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .shadow(8.dp, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE76F51)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "🎁",
                        fontSize = 20.sp,
                    )
                }
            }

            StageDecoration.STAR -> {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .shadow(8.dp, CircleShape)
                        .clip(CircleShape)
                        .background(GoldAccent),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.star),
                        contentDescription = "Star",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            StageDecoration.NONE -> {}
        }
    }
}

/**
 * Floating user profile avatar badge (like reference screen 1 avatar marker).
 */
@Composable
private fun PlayerAvatarBadge(
    userName: String,
    modifier: Modifier = Modifier,
) {
    val initial = userName.take(1).uppercase().ifEmpty { "U" }

    Box(
        modifier = modifier
            .shadow(12.dp, CircleShape, spotColor = MindfulBrown.copy(0.3f))
            .clip(CircleShape)
            .background(Color.White)
            .border(3.dp, OrangeAccent, CircleShape)
            .padding(4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MindfulBrown),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = initial,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White,
            )
        }
    }
}

/**
 * Connecting pathway of stepping stones (path.png) between two stage platforms.
 */
@Composable
private fun ConnectingPath(
    currentXOffset: Dp,
    nextXOffset: Dp,
    stageIndex: Int,
) {
    val stoneCount = 5
    val pathHeight = 85.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(pathHeight),
    ) {
        // Optional floating mascot owls on alternating sides for world immersion
        if (stageIndex == 1) {
            // Owl mascot on left side
            Image(
                painter = painterResource(id = R.drawable.owl_1),
                contentDescription = "Mascot Owl",
                modifier = Modifier
                    .size(68.dp)
                    .align(Alignment.CenterStart)
                    .offset(x = 24.dp),
            )
        } else if (stageIndex == 3) {
            // Owl mascot on right side
            Image(
                painter = painterResource(id = R.drawable.owl_jump),
                contentDescription = "Jumping Mascot Owl",
                modifier = Modifier
                    .size(72.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = (-24).dp),
            )
        }

        // Stepping stones along interpolated curve
        for (i in 1..stoneCount) {
            val fraction = i.toFloat() / (stoneCount + 1)
            // Linear interpolation of X position between current and next stage
            val currentXVal = currentXOffset.value
            val nextXVal = nextXOffset.value
            val interpolatedX = currentXVal + (nextXVal - currentXVal) * fraction
            val interpolatedY = pathHeight.value * fraction

            Image(
                painter = painterResource(id = R.drawable.path),
                contentDescription = null,
                modifier = Modifier
                    .size(width = 30.dp, height = 22.dp)
                    .offset(
                        x = interpolatedX.dp,
                        y = (interpolatedY - 11).dp,
                    )
                    .align(Alignment.TopCenter),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

package ai.macao.app.home

import ai.macao.app.R
import ai.macao.app.theme.AppBackground
import ai.macao.app.theme.MindfulBrown
import ai.macao.app.theme.MindfulBrown40
import ai.macao.app.theme.OrangeAccent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

// ─── Home tab enum ────────────────────────────────────────────────────────────

enum class HomeTab { Home, AiTalk, Notification, Chart, Profile }

// ─── Bottom Nav Bar ───────────────────────────────────────────────────────────

/**
 * Pill-shaped bottom navigation bar with five icons.
 *
 * The active tab is highlighted with [OrangeAccent]; inactive icons are
 * tinted with [MindfulBrown40] to stay consistent with the app-wide palette.
 */
@Composable
fun BottomNavBar(
    selectedTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .shadow(
                    elevation     = 16.dp,
                    shape         = RoundedCornerShape(40.dp),
                    ambientColor  = MindfulBrown.copy(alpha = 0.15f),
                    spotColor     = MindfulBrown.copy(alpha = 0.25f),
                )
                .clip(RoundedCornerShape(40.dp))
                .background(Color.White)
                .padding(horizontal = 24.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(22.dp),
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            NavItem(
                iconRes  = R.drawable.home,
                selected = selectedTab == HomeTab.Home,
                onClick  = { onTabSelected(HomeTab.Home) },
            )
            NavItem(
                iconRes  = R.drawable.chatbot,
                selected = selectedTab == HomeTab.AiTalk,
                onClick  = { onTabSelected(HomeTab.AiTalk) },
            )
            NavItem(
                iconRes  = R.drawable.notification,
                selected = selectedTab == HomeTab.Notification,
                onClick  = { onTabSelected(HomeTab.Notification) },
            )
            NavItem(
                iconRes  = R.drawable.chart,
                selected = selectedTab == HomeTab.Chart,
                onClick  = { onTabSelected(HomeTab.Chart) },
            )
            NavItem(
                iconRes  = R.drawable.profile,
                selected = selectedTab == HomeTab.Profile,
                onClick  = { onTabSelected(HomeTab.Profile) },
            )
        }
    }
}

// ─── Single nav item ──────────────────────────────────────────────────────────

@Composable
private fun NavItem(
    iconRes : Int,
    selected: Boolean,
    onClick : () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier            = Modifier
            .clickable(
                interactionSource = interactionSource,
                indication        = null,
                onClick           = onClick,
            ),
    ) {
        Icon(
            painter            = painterResource(id = iconRes),
            contentDescription = null,
            tint               = if (selected) OrangeAccent else MindfulBrown40,
            modifier           = Modifier.size(26.dp),
        )
        Spacer(modifier = Modifier.height(4.dp))
        // Active indicator dot
        Box(
            modifier = Modifier
                .size(4.dp)
                .clip(RoundedCornerShape(50))
                .background(if (selected) OrangeAccent else Color.Transparent),
        )
    }
}

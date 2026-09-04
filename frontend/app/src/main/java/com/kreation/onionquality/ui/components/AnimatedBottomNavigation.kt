package com.kreation.onionquality.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kreation.onionquality.theme.DarkPlum
import com.kreation.onionquality.theme.PrimaryMagenta
import com.kreation.onionquality.theme.White

data class NavItem(val route: String, val title: String, val icon: ImageVector)

val navItems = listOf(
    NavItem("dashboard", "HOME", Icons.Outlined.Home),
    NavItem("history", "INSPECTIONS", Icons.Outlined.List),
    NavItem("reports", "REPORTS", Icons.Outlined.Assessment),
    NavItem("profile", "PROFILE", Icons.Outlined.Person)
)

@Composable
fun AnimatedBottomNavigation(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val selectedIndex = navItems.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .height(84.dp), // extra height for the floating circle
        contentAlignment = Alignment.BottomCenter
    ) {
        // Background Bar with elevation
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp),
            shape = RoundedCornerShape(22.dp),
            color = DarkPlum,
            shadowElevation = 8.dp
        ) {}

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(84.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            navItems.forEachIndexed { index, item ->
                val isSelected = selectedIndex == index
                
                val yOffset by animateDpAsState(
                    targetValue = if (isSelected) (-26).dp else 0.dp,
                    animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing), label = "yOffset"
                )
                
                val iconScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.1f else 1.0f,
                    animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing), label = "iconScale"
                )

                val labelAlpha by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0f,
                    animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing), label = "labelAlpha"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onNavigate(item.route) },
                    contentAlignment = Alignment.BottomCenter
                ) {
                    // Active indicator
                    if (isSelected) {
                        Surface(
                            modifier = Modifier
                                .offset(y = yOffset)
                                .size(58.dp),
                            shape = CircleShape,
                            color = PrimaryMagenta,
                            shadowElevation = 6.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    tint = White,
                                    modifier = Modifier.scale(iconScale)
                                )
                            }
                        }
                    } else {
                        // Inactive Icon
                        Box(
                            modifier = Modifier
                                .offset(y = (-24).dp)
                                .size(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = Color(0xFFB9A7B2)
                            )
                        }
                    }

                    // Label (appears only when active)
                    Text(
                        text = item.title,
                        color = PrimaryMagenta,
                        fontSize = 10.sp,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .alpha(labelAlpha)
                    )
                }
            }
        }
    }
}

package com.tony.gardenflow.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class GardenMetrics(
    val screenPadding: Dp,
    val cardPadding: Dp,
    val listGap: Dp,
    val iconSize: Dp,
    val smallIconSize: Dp,
    val isTablet: Boolean,
    val maxContentWidth: Dp,
    val bottomNavMaxWidth: Dp
)

@Composable
fun rememberGardenMetrics(): GardenMetrics {
    val width = LocalConfiguration.current.screenWidthDp
    val compact = width < 380
    val tablet = width >= 720
    return GardenMetrics(
        screenPadding = when {
            compact -> 16.dp
            tablet -> 40.dp
            else -> 24.dp
        },
        cardPadding = if (tablet) 22.dp else if (compact) 14.dp else 18.dp,
        listGap = if (tablet) 22.dp else if (compact) 14.dp else 18.dp,
        iconSize = if (tablet) 64.dp else if (compact) 50.dp else 58.dp,
        smallIconSize = if (tablet) 46.dp else if (compact) 38.dp else 42.dp,
        isTablet = tablet,
        maxContentWidth = if (tablet) 1120.dp else Dp.Unspecified,
        bottomNavMaxWidth = if (tablet) 520.dp else Dp.Unspecified
    )
}

package com.ftl.hires.audioplayer.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ftl.hires.audioplayer.presentation.theme.SubcoderColors
import com.ftl.hires.audioplayer.presentation.theme.FTLAudioTypography
import com.ftl.hires.audioplayer.presentation.screens.*

/**
 * FTL Hi-Res Audio Player Navigation System
 * 
 * Cyberpunk-themed navigation with smooth transitions:
 * - Command Center (Home): Main control hub
 * - Audio Archive (Library): Music collection
 * - Audio Matrix (Equalizer): DSP control center
 * - System Config (Settings): Configuration panel
 * - Audio Bridge (Now Playing): Active playback control
 */

// Navigation destinations with cyberpunk naming
sealed class FTLDestination(
    val route: String,
    val displayName: String,
    val cyberpunkName: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
    val description: String
) {
    object CommandCenter : FTLDestination(
        route = "command_center",
        displayName = "Home",
        cyberpunkName = "COMMAND CENTER",
        icon = Icons.Outlined.Dashboard,
        selectedIcon = Icons.Filled.Dashboard,
        description = "Main control hub with system overview"
    )
    
    object AudioArchive : FTLDestination(
        route = "audio_archive",
        displayName = "Library",
        cyberpunkName = "AUDIO ARCHIVE",
        icon = Icons.Outlined.LibraryMusic,
        selectedIcon = Icons.Filled.LibraryMusic,
        description = "Digital music collection database"
    )
    
    object AudioMatrix : FTLDestination(
        route = "audio_matrix",
        displayName = "Equalizer",
        cyberpunkName = "AUDIO MATRIX",
        icon = Icons.Outlined.GraphicEq,
        selectedIcon = Icons.Filled.GraphicEq,
        description = "32-band parametric EQ control matrix"
    )
    
    object SystemConfig : FTLDestination(
        route = "system_config",
        displayName = "Settings",
        cyberpunkName = "SYSTEM CONFIG",
        icon = Icons.Outlined.Settings,
        selectedIcon = Icons.Filled.Settings,
        description = "System configuration and preferences"
    )
    
    object AudioBridge : FTLDestination(
        route = "audio_bridge",
        displayName = "Now Playing",
        cyberpunkName = "AUDIO BRIDGE",
        icon = Icons.Outlined.MusicNote,
        selectedIcon = Icons.Filled.MusicNote,
        description = "Active audio stream control bridge"
    )
}

// List of main navigation destinations
val ftlDestinations = listOf(
    FTLDestination.CommandCenter,
    FTLDestination.AudioArchive,
    FTLDestination.AudioMatrix,
    FTLDestination.SystemConfig,
    FTLDestination.AudioBridge
)

/**
 * Main Navigation Host with cyberpunk transitions
 */
@Composable
fun FTLNavigationHost(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = FTLDestination.CommandCenter.route,
        modifier = modifier,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        }
    ) {
        composable(FTLDestination.CommandCenter.route) {
            CommandCenterScreen(
                onNavigateToAudioBridge = {
                    navController.navigate(FTLDestination.AudioBridge.route)
                },
                onNavigateToAudioArchive = {
                    navController.navigate(FTLDestination.AudioArchive.route)
                }
            )
        }
        
        composable(FTLDestination.AudioArchive.route) {
            com.ftl.hires.audioplayer.presentation.screens.library.AudioArchiveScreen(
                onTrackSelected = { track ->
                    // Navigate to Audio Bridge when track is selected
                    navController.navigate(FTLDestination.AudioBridge.route)
                },
                onPlaylistSelected = { playlist ->
                    navController.navigate(FTLDestination.AudioBridge.route)
                }
            )
        }
        
        composable(FTLDestination.AudioMatrix.route) {
            AudioMatrixScreen(
                onPresetSelected = { preset ->
                    // Apply EQ preset
                },
                onCustomEQSaved = { eqSettings ->
                    // Save custom EQ configuration
                }
            )
        }
        
        composable(FTLDestination.SystemConfig.route) {
            SystemConfigScreen(
                onAudioSettingsChanged = { settings ->
                    // Apply audio configuration changes
                },
                onThemeChanged = { theme ->
                    // Apply theme changes
                }
            )
        }
        
        composable(FTLDestination.AudioBridge.route) {
            AudioBridgeScreen(
                onNavigateToAudioMatrix = {
                    navController.navigate(FTLDestination.AudioMatrix.route)
                },
                onNavigateToAudioArchive = {
                    navController.navigate(FTLDestination.AudioArchive.route)
                }
            )
        }
    }
}

/**
 * Cyberpunk-themed Bottom Navigation Bar
 */
@Composable
fun FTLBottomNavigation(
    navController: NavController,
    currentRoute: String?,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    // Cyberpunk gradient background
    val navigationBackground = Brush.horizontalGradient(
        colors = listOf(
            SubcoderColors.PureBlack,
            SubcoderColors.DarkGrey.copy(alpha = 0.95f),
            SubcoderColors.PureBlack
        )
    )
    
    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(navigationBackground)
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
        containerColor = Color.Transparent,
        contentColor = SubcoderColors.Cyan
    ) {
        ftlDestinations.forEach { destination ->
            val isSelected = currentDestination?.hierarchy?.any { 
                it.route == destination.route 
            } == true
            
            NavigationBarItem(
                icon = {
                    FTLNavigationIcon(
                        destination = destination,
                        isSelected = isSelected
                    )
                },
                label = {
                    FTLNavigationLabel(
                        destination = destination,
                        isSelected = isSelected
                    )
                },
                selected = isSelected,
                onClick = {
                    navController.navigate(destination.route) {
                        // Pop up to the start destination to avoid building up a large stack
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = SubcoderColors.Cyan,
                    selectedTextColor = SubcoderColors.Cyan,
                    unselectedIconColor = SubcoderColors.LightGrey,
                    unselectedTextColor = SubcoderColors.LightGrey,
                    indicatorColor = SubcoderColors.Cyan.copy(alpha = 0.1f)
                )
            )
        }
    }
}

/**
 * Custom navigation icon with cyberpunk effects
 */
@Composable
private fun FTLNavigationIcon(
    destination: FTLDestination,
    isSelected: Boolean
) {
    val icon = if (isSelected) destination.selectedIcon else destination.icon
    val iconColor = if (isSelected) {
        SubcoderColors.Cyan
    } else {
        SubcoderColors.LightGrey
    }
    
    Box(
        modifier = Modifier.size(24.dp),
        contentAlignment = Alignment.Center
    ) {
        // Glow effect for selected icon
        if (isSelected) {
            Icon(
                imageVector = icon,
                contentDescription = destination.cyberpunkName,
                tint = iconColor.copy(alpha = 0.3f),
                modifier = Modifier.size(28.dp) // Slightly larger for glow
            )
        }
        
        // Main icon
        Icon(
            imageVector = icon,
            contentDescription = destination.cyberpunkName,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Custom navigation label with cyberpunk styling
 */
@Composable
private fun FTLNavigationLabel(
    destination: FTLDestination,
    isSelected: Boolean
) {
    val textColor = if (isSelected) {
        SubcoderColors.Cyan
    } else {
        SubcoderColors.LightGrey
    }
    
    val fontWeight = if (isSelected) {
        FontWeight.SemiBold
    } else {
        FontWeight.Normal
    }
    
    Text(
        text = destination.displayName,
        color = textColor,
        fontSize = 10.sp,
        fontWeight = fontWeight,
        style = FTLAudioTypography.settingDescription
    )
}

/**
 * Navigation utilities and helper functions
 */
object FTLNavigationUtils {
    
    /**
     * Get destination by route
     */
    fun getDestinationByRoute(route: String): FTLDestination? {
        return ftlDestinations.find { it.route == route }
    }
    
    /**
     * Check if route requires audio service
     */
    fun requiresAudioService(route: String): Boolean {
        return when (route) {
            FTLDestination.AudioBridge.route,
            FTLDestination.AudioMatrix.route -> true
            else -> false
        }
    }
    
    /**
     * Get navigation transition direction
     */
    fun getTransitionDirection(
        fromRoute: String,
        toRoute: String
    ): AnimatedContentTransitionScope.SlideDirection {
        val fromIndex = ftlDestinations.indexOfFirst { it.route == fromRoute }
        val toIndex = ftlDestinations.indexOfFirst { it.route == toRoute }
        
        return if (toIndex > fromIndex) {
            AnimatedContentTransitionScope.SlideDirection.Left
        } else {
            AnimatedContentTransitionScope.SlideDirection.Right
        }
    }
    
    /**
     * Get cyberpunk name for route
     */
    fun getCyberpunkName(route: String): String {
        return getDestinationByRoute(route)?.cyberpunkName ?: "UNKNOWN SECTOR"
    }
}

/**
 * Navigation state management
 */
@Stable
data class FTLNavigationState(
    val currentRoute: String = FTLDestination.CommandCenter.route,
    val isLoading: Boolean = false,
    val hasBackStack: Boolean = false,
    val audioServiceRequired: Boolean = false
)

/**
 * Remember navigation state
 */
@Composable
fun rememberFTLNavigationState(
    navController: NavHostController
): FTLNavigationState {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: FTLDestination.CommandCenter.route
    
    return remember(currentRoute) {
        FTLNavigationState(
            currentRoute = currentRoute,
            hasBackStack = navController.previousBackStackEntry != null,
            audioServiceRequired = FTLNavigationUtils.requiresAudioService(currentRoute)
        )
    }
}

/**
 * Top App Bar with cyberpunk styling for navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FTLTopAppBar(
    currentDestination: FTLDestination?,
    onNavigationClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = currentDestination?.cyberpunkName ?: "UNKNOWN SECTOR",
                    style = FTLAudioTypography.librarySection,
                    color = SubcoderColors.Cyan
                )
                Text(
                    text = currentDestination?.description ?: "System status unknown",
                    style = FTLAudioTypography.settingDescription,
                    color = SubcoderColors.LightGrey
                )
            }
        },
        modifier = modifier,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = SubcoderColors.PureBlack.copy(alpha = 0.95f),
            titleContentColor = SubcoderColors.Cyan,
            navigationIconContentColor = SubcoderColors.Cyan
        )
    )
}
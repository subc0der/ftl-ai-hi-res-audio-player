package com.ftl.hires.audioplayer

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import com.ftl.hires.audioplayer.presentation.theme.FTLAudioPlayerTheme
import com.ftl.hires.audioplayer.presentation.navigation.FTLNavigationHost
import com.ftl.hires.audioplayer.presentation.components.FTLBottomNavigation
import com.ftl.hires.audioplayer.presentation.viewmodel.MainViewModel
import com.ftl.hires.audioplayer.presentation.theme.SubcoderColors

/**
 * FTL Hi-Res Audio Player - Main Activity
 * 
 * Features:
 * - Hilt dependency injection with @AndroidEntryPoint
 * - Cyberpunk-themed Compose UI with FTL navigation
 * - Splash screen with animation
 * - Edge-to-edge display support
 * - Media file intent handling
 * - Professional audio player interface
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // ViewModels injected by Hilt
    private val mainViewModel: MainViewModel by viewModels()

    // Splash screen state
    private var keepSplashScreen = true

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen before super.onCreate()
        val splashScreen = installSplashScreen()
        
        super.onCreate(savedInstanceState)
        
        Timber.d("MainActivity onCreate - FTL Hi-Res Audio Player starting")
        
        // Configure splash screen
        configureSplashScreen(splashScreen)
        
        // Enable edge-to-edge display
        enableEdgeToEdge()
        
        // Configure window for optimal audio experience
        configureWindowForAudio()
        
        // Handle incoming media file intents
        handleMediaFileIntent(intent)
        
        // Set up Compose content
        setContent {
            FTLAudioPlayerTheme {
                FTLAudioPlayerApp(
                    mainViewModel = mainViewModel,
                    onSplashScreenFinished = { keepSplashScreen = false }
                )
            }
        }
    }

    /**
     * Configure splash screen with loading animation
     */
    private fun configureSplashScreen(splashScreen: androidx.core.splashscreen.SplashScreen) {
        splashScreen.setKeepOnScreenCondition { keepSplashScreen }
        
        // Set splash screen exit animation
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            splashScreen.setOnExitAnimationListener { splashScreenView ->
                // Custom exit animation
                val slideUp = android.animation.ObjectAnimator.ofFloat(
                    splashScreenView.view,
                    android.view.View.TRANSLATION_Y,
                    0f,
                    -splashScreenView.view.height.toFloat()
                )
                slideUp.interpolator = android.view.animation.AnticipateInterpolator()
                slideUp.duration = 500L
                slideUp.doOnEnd { splashScreenView.remove() }
                slideUp.start()
            }
        }
    }

    /**
     * Configure window properties for optimal audio experience
     */
    private fun configureWindowForAudio() {
        // Keep screen on during playback
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        // Optimize for audio processing
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            )
        }
        
        // Enable hardware acceleration for smooth audio visualizations
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        Timber.d("Window configured for optimal audio experience")
    }

    /**
     * Handle incoming media file intents from other apps or file manager
     */
    private fun handleMediaFileIntent(intent: Intent?) {
        intent?.let { receivedIntent ->
            when (receivedIntent.action) {
                Intent.ACTION_VIEW -> {
                    receivedIntent.data?.let { uri ->
                        Timber.d("Received media file intent: $uri")
                        // Handle media file through MainViewModel
                        mainViewModel.handleMediaFileUri(uri)
                    }
                }
                Intent.ACTION_SEND -> {
                    receivedIntent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.let { uri ->
                        Timber.d("Received shared media file: $uri")
                        mainViewModel.handleMediaFileUri(uri)
                    }
                }
                Intent.ACTION_SEND_MULTIPLE -> {
                    receivedIntent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)?.let { uris ->
                        Timber.d("Received multiple shared media files: ${uris.size} files")
                        mainViewModel.handleMediaFileUris(uris)
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // Handle new intents when activity is already running
        handleMediaFileIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        // Resume audio processing optimizations
        mainViewModel.onActivityResumed()
        Timber.d("MainActivity resumed")
    }

    override fun onPause() {
        super.onPause()
        // Optimize for background audio playback
        mainViewModel.onActivityPaused()
        Timber.d("MainActivity paused")
    }
}

/**
 * Main Compose application with FTL cyberpunk navigation
 */
@Composable
fun FTLAudioPlayerApp(
    mainViewModel: MainViewModel,
    onSplashScreenFinished: () -> Unit
) {
    val systemUiController = rememberSystemUiController()
    val navController = rememberNavController()
    
    // Configure system UI for cyberpunk theme
    LaunchedEffect(Unit) {
        systemUiController.setSystemBarsColor(
            color = androidx.compose.ui.graphics.Color.Transparent,
            darkIcons = false
        )
        systemUiController.setNavigationBarColor(
            color = SubcoderColors.PureBlack.copy(alpha = 0.95f),
            darkIcons = false
        )
    }
    
    // Main FTL Navigation with bottom navigation
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SubcoderColors.PureBlack)
    ) {
        // FTL Navigation Host
        FTLNavigationHost(
            navController = navController,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
        )
        
        // Bottom navigation bar
        FTLBottomNavigation(
            navController = navController,
            currentRoute = navController.currentBackStackEntry?.destination?.route,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

/**
 * Extension functions for MainViewModel integration
 */
private fun MainViewModel.handleMediaFileUri(uri: Uri) {
    // Handle media file URI through MainViewModel
    Timber.d("Handling media file URI: $uri")
    // Implementation would load media file from URI
}

private fun MainViewModel.handleMediaFileUris(uris: List<Uri>) {
    // Handle multiple media file URIs through MainViewModel
    Timber.d("Handling ${uris.size} media file URIs")
    // Implementation would load playlist from URIs
}
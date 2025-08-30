# FTL Hi-Res Audio Player - Custom Settings Specification
## Complete Configuration System for AI-Assisted Development

---

## 🎨 Visual & Interface Settings

### Theme & Appearance
```kotlin
// Core Theme Settings
data class ThemeSettings(
    val primaryColor: String = "#00ffff", // Cyan
    val accentColor: String = "#ff6600", // Orange
    val backgroundColor: String = "#000000", // Pure Black
    val enableGlowEffects: Boolean = true,
    val glowIntensity: Float = 0.8f, // 0.0 - 1.0
    val neuralNetworkBackground: Boolean = true,
    val animationSpeed: Float = 1.0f, // 0.5 - 2.0
    val particleEffects: Boolean = true,
    val matrixRainEffect: Boolean = false, // Easter egg mode
)

// Typography Settings
data class TypographySettings(
    val primaryFont: String = "Orbitron",
    val secondaryFont: String = "Inter", 
    val technicalFont: String = "JetBrains Mono",
    val fontSizeScale: Float = 1.0f, // 0.8 - 1.4
    val fontWeight: String = "Medium", // Light, Medium, Bold, Black
    val letterSpacing: Float = 0.1f // 0.0 - 0.3
)

// UI Layout Settings
data class LayoutSettings(
    val compactMode: Boolean = false,
    val showMiniPlayer: Boolean = true,
    val bottomNavigationStyle: String = "Cyberpunk", // Classic, Cyberpunk, Minimal
    val cardCornerRadius: Float = 15f, // 0f - 25f
    val elementSpacing: Float = 16f, // 8f - 24f
    val enableHapticFeedback: Boolean = true,
    val screenOrientation: String = "Auto" // Portrait, Landscape, Auto
)
```

### Visual Effects
```kotlin
data class VisualEffectsSettings(
    val spectrumAnalyzer: Boolean = true,
    val visualizerStyle: String = "Neural Network", // Bars, Circular, Wave, Neural Network
    val visualizerSensitivity: Float = 0.7f, // 0.1 - 1.0
    val albumArtEffects: Boolean = true,
    val albumArtBlur: Boolean = false,
    val backgroundVisualization: Boolean = true,
    val particleCount: Int = 50, // 0 - 200
    val waveformDisplay: Boolean = true,
    val frequencyBands: Int = 32, // 16, 32, 64, 128
    val peakMeters: Boolean = true,
    val vuMeters: Boolean = false
)
```

---

## 🎵 Audio Engine Settings

### Core Audio Processing
```kotlin
data class AudioEngineSettings(
    val audioQuality: String = "Hi-Res", // Standard, Hi-Res, Studio
    val sampleRate: Int = 192000, // 44100, 48000, 96000, 192000, 384000
    val bitDepth: Int = 24, // 16, 24, 32
    val audioBuffer: String = "Auto", // Low, Normal, High, Auto
    val dsdSupport: Boolean = true,
    val mqaDecoding: Boolean = true,
    val gaplessPlayback: Boolean = true,
    val crossfade: Boolean = false,
    val crossfadeDuration: Int = 3, // 0-10 seconds
    val replayGain: Boolean = true,
    val normalizeVolume: Boolean = false
)

data class DigitalFilterSettings(
    val filterType: String = "Linear Phase", // Minimum Phase, Linear Phase, Hybrid
    val oversamplingRate: String = "4x", // None, 2x, 4x, 8x
    val ditherType: String = "TPDF", // None, RPDF, TPDF, Triangular
    val noiseshaping: Boolean = true,
    val antialiasingFilter: Boolean = true
)
```

### 32-Band Parametric Equalizer
```kotlin
data class EqualizerSettings(
    val isEnabled: Boolean = false,
    val preampGain: Float = 0f, // -20dB to +20dB
    val bands: List<EQBand> = createDefaultEQBands(),
    val currentPreset: String = "Flat",
    val customPresets: List<EQPreset> = emptyList(),
    val autoEQ: Boolean = false,
    val roomCorrection: Boolean = false,
    val bassBoost: Float = 0f, // 0-12dB
    val bassBoostFreq: Float = 60f, // 20-120Hz
    val virtualSurround: Boolean = false,
    val stereoWide: Float = 0f // 0-100%
)

data class EQBand(
    val frequency: Float, // 10Hz - 40kHz
    val gain: Float, // -20dB to +20dB  
    val q: Float, // 0.1 - 30.0
    val isEnabled: Boolean = true
)

data class EQPreset(
    val name: String,
    val bands: List<Float>, // 32 gain values
    val preamp: Float,
    val isSystem: Boolean = false,
    val genre: String = "",
    val description: String = ""
)
```

### Advanced Audio Features
```kotlin
data class AudioEnhancementSettings(
    val spatialAudio: Boolean = false,
    val headphoneVirtualization: Boolean = false,
    val speakerCorrection: Boolean = false,
    val dynamicRange: String = "Full", // Compressed, Normal, Full, Extended
    val loudnessCompensation: Boolean = true,
    val bassEnhancement: String = "Psychoacoustic", // Off, Harmonic, Psychoacoustic
    val trebleEnhancement: Boolean = false,
    val midrangeClarity: Boolean = false,
    val stereoImaging: String = "Natural" // Narrow, Natural, Wide, Ultra-Wide
)
```

---

## 🎚️ Hardware & Connectivity Settings

### Audio Output
```kotlin
data class AudioOutputSettings(
    val primaryOutput: String = "Internal", // Internal, USB, Bluetooth, Cast
    val usbAudioMode: String = "Exclusive", // Shared, Exclusive, WASAPI
    val bluetoothCodec: String = "Auto", // SBC, AAC, aptX, aptX HD, LDAC, Auto
    val bluetoothBitrate: String = "Auto", // Low, Medium, High, Auto
    val sampleRateConversion: String = "Auto", // Off, Auto, Force 44.1k, Force 48k
    val outputChannels: String = "Stereo", // Mono, Stereo, 5.1, 7.1
    val headphoneDetection: Boolean = true,
    val autoSwitchOutput: Boolean = true
)

data class ExternalDACSettings(
    val enableUSBDAC: Boolean = true,
    val exclusiveMode: Boolean = true,
    val dacFilters: String = "Auto", // Auto, Sharp, Slow, NOS
    val dacUpsampling: Boolean = false,
    val clockSource: String = "Auto", // Internal, External, Auto
    val jitterReduction: Boolean = true
)
```

### Network & Streaming
```kotlin
data class NetworkSettings(
    val wifiStreamingQuality: String = "Hi-Res", // Standard, Hi-Res, Lossless
    val mobileStreamingQuality: String = "Standard",
    val bufferSize: String = "Large", // Small, Medium, Large, Massive
    val enableCaching: Boolean = true,
    val cacheSize: Int = 2048, // MB
    val backgroundSync: Boolean = true,
    val metadataFetching: Boolean = true,
    val albumArtQuality: String = "High" // Low, Medium, High, Ultra
)
```

---

## 🏋️ Fitness & Wellness Settings

### Workout Timer Configuration
```kotlin
data class WorkoutSettings(
    val enableWorkoutMode: Boolean = true,
    val defaultWorkDuration: Int = 45, // seconds
    val defaultRestDuration: Int = 15, // seconds
    val workoutPresets: List<WorkoutPreset> = createDefaultPresets(),
    val autoStart: Boolean = false,
    val voiceAnnouncements: Boolean = true,
    val hapticAlerts: Boolean = true,
    val musicSync: Boolean = true, // Sync timer with BPM
    val intensityAdjustment: Boolean = true,
    val heartRateIntegration: Boolean = false
)

data class WorkoutPreset(
    val name: String,
    val workSeconds: Int,
    val restSeconds: Int,
    val rounds: Int = 0, // 0 = infinite
    val description: String = "",
    val musicGenre: String = "Energy",
    val isActive: Boolean = true
)
```

### Sleep Timer & Wellness
```kotlin
data class SleepSettings(
    val enableSleepMode: Boolean = true,
    val defaultSleepTimer: Int = 30, // minutes
    val fadeDuration: Int = 5, // minutes
    val sleepEQProfile: String = "Relaxing",
    val enableBrainwaves: Boolean = false, // 40Hz enhancement
    val whiteNoise: Boolean = false,
    val natureSound: String = "None", // None, Rain, Ocean, Forest
    val nightMode: Boolean = true, // Dim UI
    val automaticSleep: Boolean = false, // Auto-detect sleep time
    val gentleWake: Boolean = true
)
```

### Biometric Integration
```kotlin
data class BiometricSettings(
    val enableBiometrics: Boolean = false,
    val heartRateMonitoring: Boolean = false,
    val stressDetection: Boolean = false,
    val activityTracking: Boolean = false,
    val adaptiveMusic: Boolean = false, // Change music based on biometrics
    val privacyMode: Boolean = true, // Local processing only
    val dataRetention: Int = 30 // days
)
```

---

## 🧠 AI & Intelligence Settings

### Smart Music Features
```kotlin
data class AISettings(
    val enableAI: Boolean = true,
    val smartPlaylists: Boolean = true,
    val moodDetection: Boolean = true,
    val genreClassification: Boolean = true,
    val personalizedEQ: Boolean = false,
    val listeningPatternAnalysis: Boolean = true,
    val musicDiscovery: Boolean = true,
    val contextualPlaylists: Boolean = true, // Time, weather, activity based
    val mlModelUpdates: Boolean = true,
    val privacyMode: Boolean = true // Local AI processing only
)

data class VoiceControlSettings(
    val enableVoiceControl: Boolean = false,
    val wakeWord: String = "Hey Matrix",
    val voiceLanguage: String = "English",
    val voiceSensitivity: Float = 0.7f, // 0.1 - 1.0
    val continuousListening: Boolean = false,
    val voiceConfirmation: Boolean = true,
    val offlineMode: Boolean = true // Local voice processing
)
```

### Adaptive Features
```kotlin
data class AdaptiveSettings(
    val environmentalAdaptation: Boolean = true,
    val timeBasedEQ: Boolean = false, // Different EQ for time of day
    val locationBasedPlaylists: Boolean = false,
    val weatherBasedMusic: Boolean = false,
    val activityDetection: Boolean = false, // Walking, running, etc.
    val automaticGenreSwitch: Boolean = false,
    val learningRate: Float = 0.5f, // 0.1 - 1.0
    val adaptationStrength: Float = 0.3f // 0.0 - 1.0
)
```

---

## 🎮 Gesture & Control Settings

### Touch & Gesture Controls
```kotlin
data class ControlSettings(
    val enableGestures: Boolean = true,
    val swipeToSkip: Boolean = true,
    val swipeToSeek: Boolean = true,
    val tiltVolumeControl: Boolean = false,
    val shakeToShuffle: Boolean = false,
    val proximityPause: Boolean = true,
    val doubleTapAction: String = "Play/Pause", // Skip, Repeat, Favorite
    val longPressAction: String = "Favorite",
    val volumeButtonBehavior: String = "Volume", // Volume, Skip, Seek
    val gestureSensitivity: Float = 0.7f // 0.1 - 1.0
)

data class ExternalControlSettings(
    val headphoneControls: Boolean = true,
    val bluetoothControls: Boolean = true,
    val mediaSessionControls: Boolean = true,
    val androidAutoIntegration: Boolean = true,
    val lockscreenControls: Boolean = true,
    val notificationControls: Boolean = true,
    val widgetSupport: Boolean = true
)
```

---

## 📱 System Integration Settings

### Android Integration
```kotlin
data class SystemSettings(
    val audioFocus: Boolean = true,
    val respectSystemVolume: Boolean = true,
    val systemEQBypass: Boolean = false,
    val mediaButtonPriority: Boolean = true,
    val backgroundPlayback: Boolean = true,
    val batteryOptimization: String = "Balanced", // Aggressive, Balanced, Performance
    val cpuPriority: String = "High", // Low, Normal, High, Realtime
    val thermalThrottling: Boolean = true,
    val powerSavingMode: Boolean = false
)

data class StorageSettings(
    val musicScanPaths: List<String> = listOf("/storage/emulated/0/Music"),
    val excludePaths: List<String> = emptyList(),
    val autoScan: Boolean = true,
    val scanFrequency: String = "Daily", // Manual, Daily, Weekly
    val albumArtCache: Boolean = true,
    val metadataCache: Boolean = true,
    val cacheLocation: String = "Internal", // Internal, SD Card, Auto
    val maxCacheSize: Int = 5120 // MB
)
```

### Privacy & Security
```kotlin
data class PrivacySettings(
    val analyticsEnabled: Boolean = false,
    val crashReporting: Boolean = true,
    val dataCollection: String = "Anonymous", // None, Anonymous, Full
    val cloudSync: Boolean = false,
    val encryptLocalData: Boolean = true,
    val requireAuthentication: Boolean = false,
    val biometricLock: Boolean = false,
    val sessionTimeout: Int = 0 // 0 = never, minutes
)
```

---

## 🎯 Performance & Advanced Settings

### Performance Optimization
```kotlin
data class PerformanceSettings(
    val audioLatency: String = "Ultra-Low", // Normal, Low, Ultra-Low
    val renderingPriority: String = "Audio", // UI, Audio, Balanced
    val memoryManagement: String = "Aggressive", // Conservative, Balanced, Aggressive
    val gcOptimization: Boolean = true,
    val preloadTracks: Int = 3, // 0-10
    val backgroundProcessing: Boolean = true,
    val multiCoreProcessing: Boolean = true,
    val vectorOptimization: Boolean = true // NEON/SSE optimization
)

data class DeveloperSettings(
    val debugMode: Boolean = false,
    val showPerformanceStats: Boolean = false,
    val audioLatencyMonitor: Boolean = false,
    val memoryUsageDisplay: Boolean = false,
    val fpsCounter: Boolean = false,
    val logLevel: String = "Warning", // Verbose, Debug, Info, Warning, Error
    val enableBenchmarks: Boolean = false,
    val forceRefreshRate: Int = 0 // 0 = auto, 60, 90, 120
)
```

---

## 📱 User Experience Settings

### Accessibility
```kotlin
data class AccessibilitySettings(
    val highContrastMode: Boolean = false,
    val largeTextMode: Boolean = false,
    val colorBlindnessSupport: String = "None", // None, Protanopia, Deuteranopia, Tritanopia
    val voiceAnnouncements: Boolean = false,
    val simplifiedUI: Boolean = false,
    val reducedMotion: Boolean = false,
    val increasedTouchTargets: Boolean = false,
    val screenReaderSupport: Boolean = true
)

data class PersonalizationSettings(
    val userName: String = "",
    val favoriteGenres: List<String> = emptyList(),
    val listeningGoals: List<String> = emptyList(), // Focus, Energy, Relaxation
    val skillLevel: String = "Beginner", // Beginner, Intermediate, Audiophile, Professional
    val preferredLanguage: String = "English",
    val timeZone: String = "Auto",
    val firstLaunch: Boolean = true,
    val onboardingComplete: Boolean = false
)
```

---

## 🔧 Implementation Notes for Claude Code

### Settings Data Structure
```kotlin
// Main settings container
@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val settingKey: String,
    val settingValue: String,
    val settingType: String, // Boolean, Int, Float, String, List
    val category: String,
    val lastModified: Long = System.currentTimeMillis()
)

// Settings categories for organization
enum class SettingCategory {
    THEME_APPEARANCE,
    AUDIO_ENGINE, 
    EQUALIZER,
    HARDWARE_CONNECTIVITY,
    FITNESS_WELLNESS,
    AI_INTELLIGENCE,
    GESTURE_CONTROL,
    SYSTEM_INTEGRATION,
    PERFORMANCE_ADVANCED,
    USER_EXPERIENCE
}
```

### Settings Implementation Strategy
1. **Modular Architecture** - Each settings category as separate module
2. **Type-Safe Configuration** - Sealed classes for setting values
3. **Real-time Updates** - LiveData/StateFlow for reactive UI updates
4. **Validation Layer** - Input validation with cyberpunk error messages
5. **Import/Export** - Settings backup/restore functionality
6. **Cloud Sync** - Optional encrypted settings synchronization
7. **A/B Testing** - Feature flags for experimental settings

### UI Implementation Guidelines
- **Cyberpunk Settings Cards** with glowing borders and neural effects
- **Interactive Sliders** with real-time audio preview
- **Preset Management** with visual EQ curve display  
- **Voice Command Setup** with waveform visualization
- **Biometric Setup** with pulse animation effects
- **Export/Import** with Matrix-style data transfer animations

---

This comprehensive settings system provides over 200 customizable parameters while maintaining the cyberpunk aesthetic that matches your Subcoder Cipher Matrix perfectly! Each setting is designed for maximum user control and audio quality optimization. 🎵⚡
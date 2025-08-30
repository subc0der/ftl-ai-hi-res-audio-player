# Settings Implementation Guide
## Developer Reference for FTL Hi-Res Audio Player

---

## 🚀 Quick Start Checklist

### Database Setup
- [ ] Create `UserSettingsEntity` in `/data/local/database/entity/`
- [ ] Implement `UserSettingsDao` with reactive queries
- [ ] Add settings tables to `AudioDatabase`
- [ ] Create migration scripts for updates

### Repository Layer  
- [ ] Implement `UserSettingsRepository` interface
- [ ] Create `UserSettingsRepositoryImpl` with Room integration
- [ ] Add settings caching with StateFlow
- [ ] Implement import/export functionality

### UI Components
- [ ] `CyberpunkSlider` - Glowing sliders with real-time updates
- [ ] `SettingsCard` - Bordered cards with neural effects
- [ ] `ToggleSwitch` - Animated switches with haptic feedback
- [ ] `PresetSelector` - Dropdown with cyberpunk styling

---

## 🎨 UI Component Specifications

### CyberpunkSlider
```kotlin
@Composable
fun CyberpunkSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    label: String,
    unit: String = "",
    showValue: Boolean = true,
    enableGlow: Boolean = true,
    hapticFeedback: Boolean = true
)
```

### SettingsCard  
```kotlin
@Composable
fun SettingsCard(
    title: String,
    description: String? = null,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
)
```

---

## 🎵 Critical Audio Settings Implementation

### EQ Band Implementation
```kotlin
// Real-time EQ updates
class EqualizerViewModel : ViewModel() {
    private val _eqBands = MutableStateFlow(createDefaultEQBands())
    val eqBands: StateFlow<List<EQBand>> = _eqBands.asStateFlow()
    
    fun updateBand(index: Int, gain: Float) {
        val updatedBands = _eqBands.value.toMutableList()
        updatedBands[index] = updatedBands[index].copy(gain = gain)
        _eqBands.value = updatedBands
        
        // Apply to audio engine immediately
        audioEngine.updateEQBand(index, gain)
    }
}
```

### Settings Validation
```kotlin
sealed class SettingValidationResult {
    object Valid : SettingValidationResult()
    data class Invalid(val message: String) : SettingValidationResult()
}

fun validateAudioSetting(key: String, value: Any): SettingValidationResult {
    return when (key) {
        "sample_rate" -> {
            val rate = value as? Int ?: return Invalid("Invalid sample rate")
            if (rate in listOf(44100, 48000, 96000, 192000, 384000)) {
                Valid
            } else {
                Invalid("Unsupported sample rate: $rate Hz")
            }
        }
        "eq_gain" -> {
            val gain = value as? Float ?: return Invalid("Invalid gain value")
            if (gain in -20f..20f) Valid else Invalid("Gain must be between -20dB and +20dB")
        }
        else -> Valid
    }
}
```

---

## 🔧 Database Schema Implementation

### Settings Entity
```kotlin
@Entity(
    tableName = "user_settings",
    indices = [Index(value = ["category", "setting_key"], unique = true)]
)
data class UserSettingsEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "setting_key") val settingKey: String,
    @ColumnInfo(name = "setting_value") val settingValue: String,
    @ColumnInfo(name = "setting_type") val settingType: SettingType,
    @ColumnInfo(name = "category") val category: SettingCategory,
    @ColumnInfo(name = "last_modified") val lastModified: Long = System.currentTimeMillis()
)

enum class SettingType { BOOLEAN, INT, FLOAT, STRING, JSON_ARRAY }
```

### Settings DAO
```kotlin
@Dao
interface UserSettingsDao {
    @Query("SELECT * FROM user_settings WHERE category = :category")
    fun getSettingsByCategory(category: SettingCategory): Flow<List<UserSettingsEntity>>
    
    @Query("SELECT * FROM user_settings WHERE setting_key = :key LIMIT 1")
    suspend fun getSetting(key: String): UserSettingsEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSetting(setting: UserSettingsEntity)
    
    @Query("DELETE FROM user_settings WHERE setting_key = :key")
    suspend fun deleteSetting(key: String)
}
```

---

## 🎮 Reactive Settings Updates

### StateFlow Integration
```kotlin
class SettingsRepository @Inject constructor(
    private val settingsDao: UserSettingsDao
) {
    private val _themeSettings = MutableStateFlow(ThemeSettings())
    val themeSettings: StateFlow<ThemeSettings> = _themeSettings.asStateFlow()
    
    suspend fun updateThemeSetting(key: String, value: Any) {
        val entity = UserSettingsEntity(
            settingKey = key,
            settingValue = value.toString(),
            settingType = getSettingType(value),
            category = SettingCategory.THEME_APPEARANCE
        )
        
        settingsDao.insertOrUpdateSetting(entity)
        refreshThemeSettings()
    }
    
    private suspend fun refreshThemeSettings() {
        val settings = settingsDao.getSettingsByCategory(SettingCategory.THEME_APPEARANCE)
        // Convert entities back to ThemeSettings data class
        _themeSettings.value = mapToThemeSettings(settings.first())
    }
}
```

---

## 🎨 Cyberpunk UI Styling

### Color System
```kotlin
@Composable
fun SettingsCyberpunkTheme(content: @Composable () -> Unit) {
    val colors = darkColorScheme(
        primary = Color(0xFF00FFFF),      // Cyan
        secondary = Color(0xFFFF6600),    // Orange  
        background = Color(0xFF000000),   // Pure Black
        surface = Color(0xFF111111),      // Dark surface
        onPrimary = Color(0xFF000000),
        onSecondary = Color(0xFF000000),
        onBackground = Color(0xFFFFFFFF),
        onSurface = Color(0xFFCCCCCC)
    )
    
    MaterialTheme(
        colorScheme = colors,
        typography = CyberpunkTypography,
        content = content
    )
}
```

### Glow Effects
```kotlin
@Composable
fun Modifier.cyberpunkGlow(
    enabled: Boolean = true,
    color: Color = MaterialTheme.colorScheme.primary,
    intensity: Float = 0.8f
): Modifier {
    return if (enabled) {
        this.drawBehind {
            drawRoundRect(
                color = color.copy(alpha = intensity * 0.3f),
                cornerRadius = CornerRadius(8.dp.toPx()),
                style = Stroke(width = 2.dp.toPx())
            )
        }
    } else this
}
```

---

## ⚡ Performance Considerations

### Settings Caching Strategy
```kotlin
class SettingsCache {
    private val cache = mutableMapOf<String, Any>()
    private val cacheTimestamps = mutableMapOf<String, Long>()
    private val cacheTimeout = 300_000L // 5 minutes
    
    fun get(key: String): Any? {
        val timestamp = cacheTimestamps[key] ?: return null
        return if (System.currentTimeMillis() - timestamp < cacheTimeout) {
            cache[key]
        } else {
            cache.remove(key)
            cacheTimestamps.remove(key)
            null
        }
    }
    
    fun put(key: String, value: Any) {
        cache[key] = value
        cacheTimestamps[key] = System.currentTimeMillis()
    }
}
```

### Batch Updates
```kotlin
suspend fun updateSettingsBatch(settings: Map<String, Any>) {
    val entities = settings.map { (key, value) ->
        UserSettingsEntity(
            settingKey = key,
            settingValue = value.toString(),
            settingType = getSettingType(value),
            category = getCategoryForKey(key)
        )
    }
    
    settingsDao.insertOrUpdateSettingsBatch(entities)
}
```

---

## 🔍 Testing Strategy

### Unit Tests
```kotlin
@Test
fun `updateEQBand should apply gain immediately`() = runTest {
    val viewModel = EqualizerViewModel(mockRepository, mockAudioEngine)
    
    viewModel.updateBand(0, 5.0f)
    
    verify(mockAudioEngine).updateEQBand(0, 5.0f)
    assertEquals(5.0f, viewModel.eqBands.value[0].gain)
}
```

### UI Tests  
```kotlin
@Test
fun `cyberpunk_slider_should_update_value_on_drag`() {
    composeTestRule.setContent {
        CyberpunkSlider(
            value = 0.5f,
            onValueChange = { /* test callback */ },
            label = "Test Slider"
        )
    }
    
    // Perform drag gesture and verify value change
}
```

---

## 📁 File Organization

```
presentation/screens/settings/
├── SettingsScreen.kt              # Main hub screen
├── categories/
│   ├── ThemeSettingsScreen.kt     # Visual customization
│   ├── AudioEngineScreen.kt       # Core audio settings
│   ├── EqualizerScreen.kt         # 32-band EQ interface
│   ├── FitnessSettingsScreen.kt   # Timers & wellness
│   ├── AISettingsScreen.kt        # Intelligence features
│   ├── ControlsSettingsScreen.kt  # Gestures & controls
│   └── AdvancedSettingsScreen.kt  # Developer options
├── components/
│   ├── CyberpunkSlider.kt         # Custom slider component
│   ├── SettingsCard.kt            # Settings card wrapper
│   ├── ToggleSwitch.kt            # Animated toggle
│   ├── PresetSelector.kt          # Preset dropdown
│   ├── EQVisualizerCard.kt        # Real-time EQ display
│   └── SettingsSearchBar.kt       # Search functionality
├── viewmodels/
│   ├── SettingsViewModel.kt       # Main settings state
│   ├── ThemeViewModel.kt          # Theme-specific logic
│   ├── EqualizerViewModel.kt      # EQ state management
│   └── AudioEngineViewModel.kt    # Audio settings state
└── utils/
    ├── SettingsValidator.kt       # Input validation
    ├── SettingsMapper.kt          # Entity <-> Data class mapping
    └── SettingsConstants.kt       # Default values & ranges
```

---

*This guide provides everything needed to implement the settings system with cyberpunk styling and audiophile-grade functionality.* 🎵⚡
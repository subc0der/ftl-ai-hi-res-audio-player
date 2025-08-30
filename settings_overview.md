# FTL Hi-Res Audio Player - Settings Overview
## Quick Reference for AI-Assisted Development

---

## 🎯 Settings System Summary

This audiophile music player features **200+ customizable settings** organized into **10 categories**, maintaining the cyberpunk aesthetic from Subcoder Cipher Matrix.

### 📊 Settings Categories & Count

| Category | Settings Count | Key Features |
|----------|----------------|--------------|
| **🎨 Visual & Interface** | 25 | Cyberpunk theme, neural animations, typography |
| **🎵 Audio Engine** | 35 | 32-band EQ, Hi-res support, DSP processing |
| **🎚️ Hardware & Connectivity** | 20 | USB DAC, Bluetooth codecs, streaming |
| **🏋️ Fitness & Wellness** | 25 | Workout timers, sleep mode, biometrics |
| **🧠 AI & Intelligence** | 30 | Smart playlists, voice control, ML features |
| **🎮 Gesture & Control** | 15 | Touch gestures, external controls |
| **📱 System Integration** | 20 | Android features, storage, privacy |
| **🎯 Performance & Advanced** | 15 | Optimization, developer tools |
| **📱 User Experience** | 15 | Accessibility, personalization |
| **🔧 Implementation** | N/A | Database schema, validation, UI specs |

---

## 🎨 Core Design Language

### Color Palette (Matching Cipher Matrix)
```css
Primary Cyan: #00ffff    /* Main accents, active states */
Accent Orange: #ff6600   /* Highlights, warnings */
Background: #000000      /* Pure black base */
Text Primary: #ffffff    /* Main content */
Text Secondary: #cccccc  /* Supporting text */
Border/Frame: #333333    /* UI element borders */
```

### Typography
- **Headers:** Orbitron Black (timers, displays)
- **Body:** Orbitron Regular (settings, descriptions)  
- **Technical:** JetBrains Mono (data, values)
- **Readable:** Inter (body text where needed)

---

## 🚀 Key Implementation Patterns

### Settings Data Structure
```kotlin
@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val settingKey: String,
    val settingValue: String,
    val settingType: String, // Boolean, Int, Float, String, List
    val category: String,
    val lastModified: Long
)

enum class SettingCategory {
    THEME_APPEARANCE, AUDIO_ENGINE, EQUALIZER,
    HARDWARE_CONNECTIVITY, FITNESS_WELLNESS, AI_INTELLIGENCE,
    GESTURE_CONTROL, SYSTEM_INTEGRATION, PERFORMANCE_ADVANCED,
    USER_EXPERIENCE
}
```

### Critical Audio Settings
```kotlin
// 32-Band Parametric EQ
data class EQBand(
    val frequency: Float,    // 10Hz - 40kHz
    val gain: Float,        // -20dB to +20dB  
    val q: Float,           // 0.1 - 30.0
    val isEnabled: Boolean = true
)

// Hi-Res Audio Engine
data class AudioEngineSettings(
    val sampleRate: Int = 192000,     // Up to 384kHz
    val bitDepth: Int = 24,           // 16, 24, 32-bit
    val dsdSupport: Boolean = true,   // DSD512 support
    val mqaDecoding: Boolean = true   // Full MQA decode
)
```

---

## 🏋️ Unique Features

### Workout Timer Matrix
- **3 customizable presets** (work/rest intervals)
- **Infinite loop system** with voice coaching
- **BPM synchronization** with music
- **Heart rate integration** (optional)

### Sleep Intelligence
- **Progressive audio fade** (5-60 minutes)
- **Smart EQ adjustment** for relaxation
- **Brainwave enhancement** (40Hz and below)
- **Biometric sleep detection**

### AI-Powered Features
- **Adaptive EQ** based on listening patterns
- **Contextual playlists** (time, weather, activity)
- **Voice control** with natural language
- **Environmental adaptation**

---

## 📱 UI Implementation Notes

### Cyberpunk Design Elements
- **Glowing borders** with cyan/orange accents
- **Neural network backgrounds** with particle effects
- **Matrix-style animations** for data display
- **Holographic UI elements** with depth
- **Responsive haptic feedback**

### Settings Screen Architecture
```
screens/settings/
├── SettingsScreen.kt           # Main settings hub
├── categories/
│   ├── ThemeSettingsScreen.kt  # Visual customization
│   ├── AudioSettingsScreen.kt  # Engine & EQ controls
│   ├── FitnessSettingsScreen.kt # Timers & wellness
│   ├── AISettingsScreen.kt     # Intelligence features
│   └── AdvancedSettingsScreen.kt # Developer options
├── components/
│   ├── CyberpunkSlider.kt      # Custom slider with glow
│   ├── SettingsCard.kt         # Glowing setting cards
│   ├── EQVisualizerCard.kt     # Real-time EQ display
│   └── PresetSelector.kt       # Preset management
└── viewmodels/
    └── SettingsViewModel.kt    # State management
```

---

## 🔧 Development Priorities

### Phase 1: Core Settings Infrastructure
1. **Database schema** with Room entities
2. **Settings repository** with reactive updates
3. **Basic UI components** with cyberpunk styling
4. **Theme system** implementation

### Phase 2: Audio-Critical Settings  
1. **32-band EQ system** with real-time preview
2. **Audio engine configuration**
3. **Hardware output selection**
4. **Format support toggles**

### Phase 3: Advanced Features
1. **Workout timer settings** with presets
2. **AI feature toggles** and configuration
3. **Voice control setup**
4. **Biometric integration**

### Phase 4: Polish & Optimization
1. **Import/export functionality** 
2. **Settings search and filtering**
3. **Validation and error handling**
4. **Performance optimization**

---

## 📂 File References

- **Full Specification:** `docs/specifications/FTL_Custom_Settings_Specification.md`
- **Database Schema:** `app/src/main/kotlin/.../data/local/database/`
- **UI Components:** `app/src/main/kotlin/.../presentation/screens/settings/`
- **Theme System:** `app/src/main/kotlin/.../presentation/theme/`

---

## 🎯 Claude Code Usage Notes

### Context Loading
- Reference this overview for quick project understanding
- Use full specification for detailed implementation
- Maintain cyberpunk aesthetic consistency throughout

### Implementation Approach
1. **Generate data classes** from specifications first
2. **Create UI components** with real-time preview
3. **Implement reactive updates** with StateFlow/LiveData
4. **Add validation** with cyberpunk error styling

---

*This overview provides Claude Code with essential context for implementing the world's most customizable audiophile music player while maintaining the signature cyberpunk aesthetic.* 🎵⚡
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.room)
    kotlin("kapt")
}

android {
    namespace = "com.ftl.hires.audioplayer"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.ftl.hires.audioplayer"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // Native C++ support for hi-res audio and DSP
        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
        }

        // External native build for C++ audio processing
        externalNativeBuild {
            cmake {
                cppFlags += listOf(
                    "-std=c++17",
                    "-frtti",
                    "-fexceptions",
                    "-O3",
                    "-DANDROID_STL=c++_shared"
                )
                arguments += listOf(
                    "-DANDROID_ARM_NEON=TRUE",
                    "-DANDROID_TOOLCHAIN=clang"
                )
            }
        }

        // Audio configuration for hi-res support
        manifestPlaceholders["audioLatency"] = "low_latency"
        manifestPlaceholders["audioFeatures"] = "microphone,audio_output"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
            
            // Optimize for audiophile performance
            ndk {
                debugSymbolLevel = "SYMBOL_TABLE"
            }
        }
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            
            // Enable native debugging
            isJniDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=androidx.media3.common.util.UnstableApi",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=androidx.compose.animation.ExperimentalAnimationApi",
            "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi"
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
        viewBinding = false
        dataBinding = false
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    // CMake configuration for native C++ audio processing
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    packaging {
        resources {
            excludes += listOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "/META-INF/DEPENDENCIES",
                "/META-INF/LICENSE",
                "/META-INF/LICENSE.txt",
                "/META-INF/NOTICE",
                "/META-INF/NOTICE.txt"
            )
        }
        jniLibs {
            useLegacyPackaging = false
        }
    }

    room {
        schemaDirectory("$projectDir/schemas")
    }

    // Audio-specific configurations
    androidResources {
        generateLocaleConfig = true
    }
}

dependencies {
    // Core Android with latest versions for audio optimization
    implementation(libs.androidx.core.ktx)
    implementation(libs.bundles.lifecycle)
    implementation(libs.androidx.activity.compose)
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Jetpack Compose - Complete UI toolkit
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.androidx.navigation.compose)
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.animation:animation-graphics")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.constraintlayout:constraintlayout-compose:1.0.1")

    // Media3 - Complete hi-res audio stack
    implementation(libs.bundles.media3)
    implementation("androidx.media3:media3-exoplayer-rtsp:${libs.versions.media3.get()}")
    implementation("androidx.media3:media3-datasource-okhttp:${libs.versions.media3.get()}")
    implementation("androidx.media3:media3-transformer:${libs.versions.media3.get()}")
    implementation("androidx.media3:media3-effect:${libs.versions.media3.get()}")

    // Room Database - Complete persistence stack
    implementation(libs.bundles.room)
    implementation("androidx.room:room-paging:${libs.versions.room.get()}")
    kapt(libs.androidx.room.compiler)

    // Hilt Dependency Injection - Complete DI stack
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation("androidx.hilt:hilt-work:1.1.0")
    kapt(libs.hilt.compiler)
    kapt("androidx.hilt:hilt-compiler:1.1.0")

    // Coroutines & Flow for reactive audio processing
    implementation(libs.bundles.coroutines)
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.7")

    // Audio Format Support & Codecs
    implementation("com.arthenica:ffmpeg-kit-audio:6.0-2")
    implementation("org.jaudiotagger:jaudiotagger:3.0.1")

    // Audio Permissions & System Integration
    implementation(libs.accompanist.permissions)
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.32.0")

    // DSP & Audio Processing Libraries
    implementation("org.apache.commons:commons-math3:3.6.1")
    implementation("com.github.wendykierp:JTransforms:3.1")

    // File System & Storage
    implementation("androidx.documentfile:documentfile:1.0.1")
    implementation("com.anggrayudi:storage:1.5.5")

    // JSON & Serialization for audio metadata
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
    kapt("com.squareup.moshi:moshi-kotlin-codegen:1.15.0")

    // Networking for streaming & online features
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Image Loading for album art
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("io.coil-kt:coil-svg:2.5.0")

    // Audio Visualization & Charts
    implementation("com.patrykandpatrick.vico:compose:1.13.1")
    implementation("com.patrykandpatrick.vico:compose-m3:1.13.1")

    // Preferences & Settings
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Timber for logging audio processing
    implementation("com.jakewharton.timber:timber:5.0.1")

    // Testing - Comprehensive test suite
    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${libs.versions.coroutines.get()}")
    testImplementation("androidx.room:room-testing:${libs.versions.room.get()}")
    testImplementation("com.google.dagger:hilt-android-testing:${libs.versions.hilt.get()}")
    kaptTest("com.google.dagger:hilt-android-compiler:${libs.versions.hilt.get()}")
    
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation("com.google.dagger:hilt-android-testing:${libs.versions.hilt.get()}")
    androidTestImplementation("androidx.work:work-testing:2.9.0")
    kaptAndroidTest("com.google.dagger:hilt-android-compiler:${libs.versions.hilt.get()}")
    
    // Debug tools for audio development
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.12")
}
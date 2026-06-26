plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // Kotlin 2.0.0 uses the Compose Compiler plugin
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.pcv.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.pcv.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        // NOTE: The signingConfigs block has been completely removed to resolve Error 7.
        // Debug builds do not require explicit signing configs.
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
    
    buildFeatures {
        compose = true
    }
}

dependencies {
    // --- Compose BOM (Version from handover document) ---
    val composeBom = platform("androidx.compose:compose-bom:2024.09.03")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // --- Core Compose UI & Navigation ---
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.8.0") 

    // --- CRITICAL FIXES: Hardcoded dependencies to resolve Error 8 ---
    implementation("androidx.media3:media3-exoplayer:1.4.1")
    implementation("com.google.code.gson:gson:2.11.0")
    
    // --- Other required dependencies from handover document ---
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    // --- AndroidX Core & Lifecycle ---
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.1")

    // --- Debug Tooling ---
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

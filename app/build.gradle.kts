import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
val releaseSigningFile = rootProject.file("keystore.properties")

android {
    namespace = "com.tony.gardenflow"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.tony.gardenflow"
        minSdk = 29
        targetSdk = 35
        versionCode = 10000
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "RELEASE_CERT_SHA256", "\"${localProps.getProperty("RELEASE_CERT_SHA256", "")}\"")
        buildConfigField("Boolean", "ENABLE_PLAY_INTEGRITY", localProps.getProperty("ENABLE_PLAY_INTEGRITY", "false"))
        buildConfigField("String", "PRIVACY_POLICY_URL", "\"${localProps.getProperty("PRIVACY_POLICY_URL", "https://bottony329.github.io/gardenflow/")}\"")
    }

    signingConfigs {
        create("releaseConfig") {
            if (releaseSigningFile.exists()) {
                val props = Properties().apply { releaseSigningFile.inputStream().use { load(it) } }
                storeFile = props.getProperty("storeFile")?.takeIf { it.isNotBlank() }?.let { rootProject.file(it) }
                storePassword = props.getProperty("storePassword")
                keyAlias = props.getProperty("keyAlias")
                keyPassword = props.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            buildConfigField("String", "DEEPSEEK_API_URL", "\"${localProps.getProperty("DEEPSEEK_API_URL", "")}\"")
            buildConfigField("String", "DEEPSEEK_API_KEY", "\"${localProps.getProperty("DEEPSEEK_API_KEY", "")}\"")
            buildConfigField("String", "DEEPSEEK_MODEL", "\"${localProps.getProperty("DEEPSEEK_MODEL", "")}\"")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            buildConfigField("String", "DEEPSEEK_API_URL", "\"\"")
            buildConfigField("String", "DEEPSEEK_API_KEY", "\"\"")
            buildConfigField("String", "DEEPSEEK_MODEL", "\"\"")
            signingConfig = if (releaseSigningFile.exists()) signingConfigs.getByName("releaseConfig") else signingConfigs.getByName("debug")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    lint {
        disable += setOf(
            "AutoboxingStateCreation",
            "CoroutineCreationDuringComposition",
            "MutableCollectionMutableState",
            "ReturnFromAwaitPointerEventScope",
            "SuspiciousCompositionLocalModifierRead",
            "UnrememberedGetBackStackEntry"
        )
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.navigation:navigation-compose:2.8.5")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("io.coil-kt:coil-compose:2.7.0")

    implementation("com.google.dagger:hilt-android:2.52")
    ksp("com.google.dagger:hilt-compiler:2.52")

    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    implementation("androidx.work:work-runtime-ktx:2.10.0")
    implementation("androidx.hilt:hilt-work:1.2.0")
    ksp("androidx.hilt:hilt-compiler:1.2.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.android.play:integrity:1.6.0")

    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.mlkit:text-recognition:16.0.1")
    implementation("androidx.camera:camera-camera2:1.4.1")
    implementation("androidx.camera:camera-lifecycle:1.4.1")
    implementation("androidx.camera:camera-view:1.4.1")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.4")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
}

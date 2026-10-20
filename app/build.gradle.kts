import java.util.Properties

val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
fun localProp(key: String, default: String) = localProps.getProperty(key, default)

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.josh.tvhome"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.josh.tvhome"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "0.1"

        // Weather location comes from local.properties (see local.properties.example).
        buildConfigField("double", "TV_LAT", localProp("tv.lat", "34.05"))
        buildConfigField("double", "TV_LON", localProp("tv.lon", "-118.24"))
        buildConfigField("String", "TV_LABEL", "\"" + localProp("tv.label", "LOS ANGELES") + "\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
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
        buildConfig = true
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.10.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.core:core-ktx:1.13.1")
}

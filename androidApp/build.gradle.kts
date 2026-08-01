import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinMultiplatform)
}

// Load signing config from keystore.properties (local) or CI environment variables
val keystorePropertiesFile = rootProject.file("androidApp/keystore.properties")
val signingProps = Properties()
if (keystorePropertiesFile.exists()) {
    signingProps.load(keystorePropertiesFile.inputStream())
}

fun signingProp(key: String): String =
    signingProps.getProperty(key) ?: System.getenv(key.uppercase().replace('.', '_')) ?: ""

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
}

android {
    namespace = "com.dari.dermek.android"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.dari.dermek.android"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }
    signingConfigs {
        create("release") {
            val sf = signingProp("storeFile")
            storeFile = if (sf.isNotEmpty()) file(sf) else null
            storePassword = signingProp("storePassword")
            keyAlias = signingProp("keyAlias")
            keyPassword = signingProp("keyPassword")
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            val relConfig = signingConfigs.getByName("release")
            if (relConfig.storeFile != null) signingConfig = relConfig
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    sourceSets {
        getByName("main") {
            manifest.srcFile("src/main/AndroidManifest.xml")
        }
    }
}

dependencies {
    implementation(project(":shared"))
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.appcompat:appcompat:1.7.0")
}

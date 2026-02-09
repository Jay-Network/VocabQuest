import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.jworks.vocabquest.android"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.jworks.vocabquest"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // Load Supabase credentials from local.properties
        val localPropertiesFile = rootProject.file("local.properties")
        val localProperties = Properties().apply {
            if (localPropertiesFile.exists()) {
                load(localPropertiesFile.inputStream())
            }
        }

        buildConfigField(
            "String",
            "SUPABASE_URL",
            "\"${localProperties.getProperty("SUPABASE_URL", "")}\""
        )
        buildConfigField(
            "String",
            "SUPABASE_ANON_KEY",
            "\"${localProperties.getProperty("SUPABASE_ANON_KEY", "")}\""
        )
    }

    signingConfigs {
        create("release") {
            val localPropertiesFile = rootProject.file("local.properties")
            val localProperties = Properties().apply {
                if (localPropertiesFile.exists()) load(localPropertiesFile.inputStream())
            }
            storeFile = file(localProperties.getProperty("RELEASE_STORE_FILE", "../keystore/vocabquest-release.jks"))
            storePassword = localProperties.getProperty("RELEASE_STORE_PASSWORD", "")
            keyAlias = localProperties.getProperty("RELEASE_KEY_ALIAS", "vocabquest")
            keyPassword = localProperties.getProperty("RELEASE_KEY_PASSWORD", "")
            enableV1Signing = true
            enableV2Signing = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/CONTRIBUTORS.md"
            excludes += "/META-INF/LICENSE.md"
        }
    }
}

dependencies {
    implementation(project(":shared-core"))

    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // kotlinx-datetime
    implementation(libs.kotlinx.datetime)

    // WorkManager for background J Coin sync
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.hilt.compiler)

    // Testing
    testImplementation(libs.junit)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

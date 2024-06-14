plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.parcelize")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    kotlin("plugin.serialization")
}

android {
    namespace = "put.paginarum"
    compileSdk = 34
    buildFeatures.buildConfig = true

    defaultConfig {
        applicationId = "put.paginarum"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val apiKey = project.findProperty("AL_KEY") ?: ""
        buildConfigField("String", "AL_KEY", apiKey.toString())

        manifestPlaceholders["appAuthRedirectScheme"] = "put.paginarum"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
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
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildToolsVersion = "34.0.0"
}

dependencies {
    implementation(libs.coil.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.activity)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.material)

    // HILT
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.hilt.android)
    implementation(libs.dagger)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.ui.tooling.preview.android)
    implementation(libs.androidx.material3.android)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.androidx.ui.text.google.fonts)

    ksp(libs.dagger.compiler)
    ksp(libs.hilt.compiler)

    // OkHttp
    implementation(libs.okhttp)
    // Retrofit
    implementation(libs.retrofit)
//    implementation(libs.converter.moshi)
//    implementation(libs.converter.scalars)

    // implementation(libs.moshi.kotlin)
    // Json... a było trzeba Gsonem
    implementation(libs.gson)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)

    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    // Czujniczki
    implementation(libs.composesensors)
    // Paluszki? XD
    implementation(libs.androidx.biometric)

    // WROOMBA
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Jackson
    implementation(libs.jackson.module.kotlin)
    implementation(libs.jackson.dataformat.xml)
    implementation(libs.stax.api)

    // Voyager
    implementation(libs.voyager.hilt)
    implementation(libs.voyager.tab.navigator.android)
    implementation(libs.voyager.transitions)

    // Jsoup
    implementation(libs.jsoup)

    // Auth
    implementation(libs.appauth)

    debugImplementation(libs.androidx.ui.tooling)
    androidTestImplementation(platform(libs.androidx.compose.bom))
}

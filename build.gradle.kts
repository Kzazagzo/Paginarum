plugins {
    id("com.android.application") version "8.2.2" apply false // Nie zmieniaj
    id("org.jetbrains.kotlin.android") version "1.9.23" apply false
    id("com.google.dagger.hilt.android") version "2.49" apply false
    id("com.google.devtools.ksp") version "1.9.23-1.0.20" apply false
    id("org.jetbrains.kotlin.jvm") version "1.7.20" apply false // Potrzebne do serialize xD
    id("org.jetbrains.kotlin.plugin.serialization") version "1.7.20" // eeee Serializacja
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(libs.secrets.gradle.plugin)
    }
}

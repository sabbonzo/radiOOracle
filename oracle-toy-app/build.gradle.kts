// Top-level build file. Version numbers are declared here; modules apply the plugins.
plugins {
    id("com.android.application") version "8.5.2" apply false
    // Kotlin 2.0+ is required for the org.jetbrains.kotlin.plugin.compose plugin
    // (it did not exist for Kotlin 1.9.x — Compose was configured via
    // composeOptions.kotlinCompilerExtensionVersion back then).
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
}

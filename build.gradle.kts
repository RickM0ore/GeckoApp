import java.io.ByteArrayOutputStream

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("com.google.devtools.ksp") version "2.2.21-2.0.4" apply false
    kotlin("jvm")
}


fun getGitHeadHash(): String {
    return try {
        providers.exec {
            commandLine("git", "rev-parse", "--short", "HEAD")
        }.standardOutput.asText.get().trim()
    } catch (e: Exception) {
        e.printStackTrace()
        "nohash"
    }
}

fun getGitCommitCount(): String {
    return try {
        providers.exec {
            commandLine("git", "rev-list", "--count", "HEAD")
        }.standardOutput.asText.get().trim()
    } catch (e: Exception) {
        e.printStackTrace()
        "1"
    }
}

ext {
    set("versionCode", getGitCommitCount())
    set("versionName", getGitHeadHash())
}
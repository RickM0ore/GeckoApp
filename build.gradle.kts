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
        val stdout = ByteArrayOutputStream()
        providers.exec {
            commandLine("git", "rev-parse", "--short", "HEAD")
            standardOutput = stdout
        }
        stdout.toString().trim()
    } catch (e: Exception) {
        "nohash"
    }
}

fun getGitCommitCount(): String {
    return try {
        val stdout = ByteArrayOutputStream()
        providers.exec {
            commandLine("git", "rev-list", "--count", "HEAD")
            standardOutput = stdout
        }
        stdout.toString().trim()
    } catch (e: Exception) {
        "1"
    }
}

ext {
    set("versionCode", getGitCommitCount())
    set("versionName", getGitHeadHash())
}
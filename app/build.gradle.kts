import com.android.build.gradle.api.ApkVariantOutput


plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
}

android {
    namespace = "re.rickmoo.gecko"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "re.rickmoo.gecko"
        minSdk = 28
        targetSdk = 36
        versionCode = rootProject.ext["versionCode"].toString().toInt()
        versionName = "${rootProject.ext["versionName"].toString()}.r$versionCode.${rootProject.ext["hash"].toString()}"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    android.applicationVariants.configureEach {
        val variant = this
        variant.outputs.configureEach {
            @Suppress("DEPRECATION")
            val output = this as? ApkVariantOutput
            if (output != null) {
                val abi = output.filters.find { filter -> filter.filterType == "ABI" }?.identifier
                    ?: "noarch"
                if (output.outputFileName.endsWith(".apk")) {
                    output.outputFileName =
                        "${rootProject.name}-${versionName}-${abi}-${variant.buildType.name}.apk"
                }
            }
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
        }
    }
    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a", "armeabi-v7a", "x86")
            isUniversalApk = false
        }
    }
    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.core.splashscreen)
    implementation("androidx.startup:startup-runtime:1.2.0")
    implementation("androidx.documentfile:documentfile:1.1.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)


    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation("org.jetbrains.kotlin:kotlin-reflect:2.2.21")
    // https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind
    implementation(libs.jackson.databind)
    implementation(libs.okhttp)
    implementation(libs.androidx.work.runtime)
    // https://mvnrepository.com/artifact/org.mozilla.geckoview/geckoview
    implementation(libs.geckoview)
    implementation("com.github.bumptech.glide:glide:5.0.5")
}
kotlin {
    jvmToolchain(21)
}
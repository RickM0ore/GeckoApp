import com.android.build.api.artifact.SingleArtifact


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
        val tagName = rootProject.ext["tagName"].toString()
        versionName = "${if (tagName.isBlank()) "" else "$tagName."}r$versionCode.${rootProject.ext["hash"].toString()}"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
androidComponents {
    onVariants { variant ->
        val copyTaskName = "copy${variant.name.replaceFirstChar { it.uppercase() }}Apk"

        tasks.register<Copy>(copyTaskName) {
            description = "Copies and renames the APK for variant ${variant.name}"
            group = "distribution"

            // 指定输入：直接获取该 Variant 生成的 APK 文件夹 artifact
            // 这会自动建立依赖关系，确保先执行 assemble 任务，再执行 copy
            from(variant.artifacts.get(SingleArtifact.APK))

            val tagName = rootProject.ext["tagName"].toString()
            val outputsDir = rootProject.ext["outputsDir"].toString()
            val versionCode = rootProject.ext["versionCode"].toString().toInt()
            val versionName =
                "${if (tagName.isBlank()) "" else "$tagName."}r$versionCode.${rootProject.ext["hash"].toString()}"
            into(layout.projectDirectory.dir("${outputsDir}${tagName.ifBlank { "nightly" }}/${variant.name}"))

            rename { originalFileName ->
                val abi = when {
                    originalFileName.contains("arm64-v8a") -> "arm64-v8a"
                    originalFileName.contains("armeabi-v7a") -> "armeabi-v7a"
                    originalFileName.contains("x86_64") -> "x86_64"
                    originalFileName.contains("x86") -> "x86"
                    else -> "noarch"
                }
                // 格式: RootProject-Version-ABI-BuildType.apk
                "${rootProject.name}-${versionName}-${abi}-${variant.name}.apk"
            }
        }.also { task ->
            val assembleTaskName = "assemble${variant.name.replaceFirstChar { it.uppercase() }}"
            tasks.matching { it.name == assembleTaskName }.configureEach {
                finalizedBy(task)
            }
        }
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
    implementation(libs.androidx.startup.runtime)
    implementation(libs.androidx.documentfile)

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

    implementation(libs.kotlin.reflect)
    // https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind
    implementation(libs.jackson.databind)
    implementation(libs.okhttp)
    implementation(libs.androidx.work.runtime)
    // https://mvnrepository.com/artifact/org.mozilla.geckoview/geckoview
    implementation(libs.geckoview)
    implementation(libs.glide)
    implementation(libs.compose.markdown)
}
kotlin {
    jvmToolchain(21)
}
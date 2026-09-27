plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

val sourceCommit = providers.environmentVariable("GITHUB_SHA").orElse("LOCAL").get()
val buildCommit = if (sourceCommit == "LOCAL") sourceCommit else sourceCommit.take(12)

android {
    namespace = "com.jk.operatorperformance"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.jk.operatorperformance"
        minSdk = 28
        targetSdk = 36
        versionCode = 47
        versionName = "0.38.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "BUILD_COMMIT", "\"$buildCommit\"")
    }

    val releaseKeystorePath = System.getenv("OPERATOR_RELEASE_KEYSTORE_PATH")
    val releaseStorePassword = System.getenv("OPERATOR_RELEASE_STORE_PASSWORD")
    val releaseKeyAlias = System.getenv("OPERATOR_RELEASE_KEY_ALIAS")
    val releaseKeyPassword = System.getenv("OPERATOR_RELEASE_KEY_PASSWORD")
    val releaseSigningValues = listOf(
        releaseKeystorePath,
        releaseStorePassword,
        releaseKeyAlias,
        releaseKeyPassword,
    )
    val persistentReleaseSigningAvailable = releaseSigningValues.all { !it.isNullOrBlank() }
    val partialReleaseSigningConfigured = releaseSigningValues.any { !it.isNullOrBlank() } && !persistentReleaseSigningAvailable
    require(!partialReleaseSigningConfigured) {
        "Persistent release signing is partially configured. Supply all four owner signing values or none of them."
    }

    signingConfigs {
        if (persistentReleaseSigningAvailable) {
            create("persistentRelease") {
                storeFile = file(requireNotNull(releaseKeystorePath))
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
                enableV1Signing = true
                enableV2Signing = true
                enableV3Signing = true
                enableV4Signing = true
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            if (persistentReleaseSigningAvailable) {
                signingConfig = signingConfigs.getByName("persistentRelease")
            }
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        animationsDisabled = true
        managedDevices {
            localDevices {
                create("pixel2api35") {
                    device = "Pixel 2"
                    apiLevel = 35
                    systemImageSource = "aosp"
                }
            }
        }
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2025.08.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:core:1.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}

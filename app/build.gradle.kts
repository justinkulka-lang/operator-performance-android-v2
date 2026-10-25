import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.X509Certificate

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

val sourceCommit = providers.environmentVariable("GITHUB_SHA").orElse("LOCAL").get()
val buildCommit = if (sourceCommit == "LOCAL") sourceCommit else sourceCommit.take(12)

val ownerSigningNames = listOf(
    "OPERATOR_RELEASE_KEYSTORE_PATH", "OPERATOR_RELEASE_STORE_PASSWORD",
    "OPERATOR_RELEASE_KEY_ALIAS", "OPERATOR_RELEASE_KEY_PASSWORD",
)
val validateReleaseSigning = tasks.register("validateReleaseSigning") {
    group = "verification"
    description = "Refuse release output without the complete owner-managed signing configuration."
    doLast {
        val values = ownerSigningNames.associateWith { providers.environmentVariable(it).orNull }
        check(values.values.all { !it.isNullOrBlank() }) {
            "RELEASE_SIGNING_REQUIRED: all four owner-managed signing values must be configured."
        }
        val keystore = file(requireNotNull(values["OPERATOR_RELEASE_KEYSTORE_PATH"]))
        check(keystore.isFile && keystore.canRead()) {
            "RELEASE_SIGNING_REQUIRED: the owner keystore must be a readable file."
        }
        // Never fall back to the generated Android debug key.
        check(values["OPERATOR_RELEASE_KEY_ALIAS"] != "androiddebugkey") {
            "RELEASE_SIGNING_REQUIRED: the Android debug alias is not a production identity."
        }
        val store = KeyStore.getInstance(KeyStore.getDefaultType())
        keystore.inputStream().use {
            store.load(it, requireNotNull(values["OPERATOR_RELEASE_STORE_PASSWORD"]).toCharArray())
        }
        val alias = requireNotNull(values["OPERATOR_RELEASE_KEY_ALIAS"])
        check(store.getKey(alias, requireNotNull(values["OPERATOR_RELEASE_KEY_PASSWORD"]).toCharArray()) is PrivateKey) {
            "RELEASE_SIGNING_REQUIRED: the configured alias must contain the owner's private signing key."
        }
        val certificate = store.getCertificate(alias) as? X509Certificate
        check(certificate != null && !certificate.subjectX500Principal.name.contains("Android Debug", ignoreCase = true)) {
            "RELEASE_SIGNING_REQUIRED: a production certificate is required."
        }
        certificate.checkValidity()
    }
}

// Wire every release artifact path, including direct package/sign task invocations.
tasks.configureEach {
    if (name.contains("Release") && (
        name.startsWith("pre") || name.startsWith("assemble") || name.startsWith("bundle") ||
        name.startsWith("package") || name.startsWith("sign")
    )) dependsOn(validateReleaseSigning)
}

android {
    namespace = "com.jk.operatorperformance"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.jk.operatorperformance"
        minSdk = 28
        targetSdk = 36
        versionCode = 52
        versionName = "0.43.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["additionalTestOutputDir"] = "/sdcard/Android/media/com.jk.operatorperformance/legend-evidence"
        buildConfigField("String", "BUILD_COMMIT", "\"$buildCommit\"")
        buildConfigField("String", "SOURCE_COMMIT", "\"$sourceCommit\"")
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

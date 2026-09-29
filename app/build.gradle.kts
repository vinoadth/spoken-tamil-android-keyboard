import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

fun loadProperties(name: String): Properties = Properties().apply {
    val file = rootProject.file(name)
    if (file.exists()) file.inputStream().use { load(it) }
}

val versionProps = loadProperties("version.properties")
val appVersionCode = versionProps.getProperty("VERSION_CODE").toInt()
val appVersionName = versionProps.getProperty("VERSION_NAME")

// Signing secrets live in the git-ignored keystore.properties; without it release builds are unsigned.
val keystoreProps = loadProperties("keystore.properties")

base {
    archivesName = "spoken-tamil-keyboard-$appVersionName"
}

android {
    namespace = "com.standardspokentamil"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.standardspokentamil"
        minSdk = 26
        targetSdk = 37
        versionCode = appVersionCode
        versionName = appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    if (!keystoreProps.isEmpty) {
        signingConfigs {
            create("release") {
                storeFile = rootProject.file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
        }
        release {
            signingConfigs.findByName("release")?.let { signingConfig = it }
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

tasks.withType<Test>().configureEach {
    // Word lists read by the dictionary file tests; declared so edits re-run the tests.
    inputs.files("src/main/assets/dictionary.tsv", rootProject.file("sample-dictionary.tsv"))
        .withPropertyName("dictionaryFiles")
        .withPathSensitivity(PathSensitivity.RELATIVE)
    testLogging {
        events("failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
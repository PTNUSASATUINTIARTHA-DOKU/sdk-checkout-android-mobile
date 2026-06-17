import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.devtools.ksp") version "1.9.23-1.0.20"
    id("maven-publish")
}

android {
    namespace = "com.doku.sdkcheckoutandroid"
    compileSdk = 34

    buildFeatures {
        viewBinding = true
    }

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("androidx.fragment:fragment-ktx:1.6.1")
    implementation("com.google.zxing:core:3.5.1")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    implementation ("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(FileInputStream(localPropertiesFile))
    }
}

val gprUser: String = System.getenv("GITHUB_ACTOR") ?: localProperties.getProperty("gpr.user") ?: ""
val gprKey: String = System.getenv("GITHUB_TOKEN") ?: localProperties.getProperty("gpr.key") ?: ""

afterEvaluate {
    publishing {
        publications {
            register<MavenPublication>("release") {
                groupId = "com.doku.sdk-checkout"
                artifactId = "doku-checkout"
                version = "1.0.0"

                // Mengambil komponen binary (.aar) yang dihasilkan oleh Android Studio
                from(components["release"])
            }
        }

        // Target lokasi pengunggahan (GitHub Packages)
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/PTNUSASATUINTIARTHA-DOKU/sdk-checkout-android-mobile")

                credentials {
                    username = gprUser
                    password = gprKey
                }
            }
        }
    }
}
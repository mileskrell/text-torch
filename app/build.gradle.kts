import org.jetbrains.kotlin.config.KotlinCompilerVersion

plugins {
    id("com.android.application")
    kotlin("android")
    id("io.sentry.android.gradle") version "3.1.2"
    id("com.mikepenz.aboutlibraries.plugin") version "10.3.1"
}

android {
    compileSdk = 32
    defaultConfig {
        applicationId = "com.mileskrell.texttorch"
        minSdk = 21
        targetSdk = 32
        versionCode = 4
        versionName = "1.1.1"
    }
    buildFeatures {
        viewBinding = true
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(kotlin("stdlib-jdk8", KotlinCompilerVersion.VERSION))

    val lifecycleVersion = "2.5.0"
    val navigationVersion = "2.5.0"
    implementation("androidx.activity:activity-ktx:1.5.0")
    implementation("androidx.appcompat:appcompat:1.4.2")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.core:core-ktx:1.8.0")
    implementation("androidx.fragment:fragment-ktx:1.5.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    implementation("androidx.navigation:navigation-fragment-ktx:$navigationVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navigationVersion")
    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.viewpager:viewpager:1.0.0")

    implementation("com.google.android.material:material:1.6.1")
    implementation("com.mikepenz:aboutlibraries:10.3.1")
    implementation("io.sentry:sentry-android:6.2.1")
    implementation("io.sentry:sentry-android-navigation:6.2.1")

    val coroutinesVersion = "1.6.3"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")

    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.9.1")
}

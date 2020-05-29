import org.jetbrains.kotlin.config.KotlinCompilerVersion

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("android.extensions")
    id("com.mikepenz.aboutlibraries.plugin")
}

android {
    compileSdkVersion(28)
    defaultConfig {
        applicationId = "com.mileskrell.texttorch"
        minSdkVersion(21)
        targetSdkVersion(28)
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
    // This appears to be required starting with androidx.navigation 2.1.0. Found here:
    // https://stackoverflow.com/questions/48988778/cannot-inline-bytecode-built-with-jvm-target-1-8-into-bytecode-that-is-being-bui/48988779#comment93879366_50991772
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            kotlinOptions.jvmTarget = "1.8"
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(kotlin("stdlib-jdk7", KotlinCompilerVersion.VERSION))

    val navigationVersion = "2.2.2"
    implementation("androidx.appcompat:appcompat:1.1.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")
    implementation("androidx.core:core-ktx:1.3.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.2.0")
    implementation("androidx.navigation:navigation-fragment-ktx:$navigationVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navigationVersion")
    implementation("androidx.preference:preference-ktx:1.1.1")

    val aboutLibrariesVersion: String by rootProject.extra
    implementation("com.google.android.material:material:1.1.0")
    implementation("com.mikepenz:aboutlibraries:$aboutLibrariesVersion")
    implementation("com.romandanylyk:pageindicatorview:1.0.3")
    implementation("ly.count.android:sdk:20.04.1")

    val coroutinesVersion = "1.3.6"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")

    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.2")

    testImplementation("junit:junit:4.13")
    androidTestImplementation("androidx.test:runner:1.2.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")
}

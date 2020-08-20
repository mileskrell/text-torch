// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.0.1")
        classpath(kotlin("gradle-plugin", "1.4.0"))

        classpath("io.sentry:sentry-android-gradle-plugin:1.7.35")
        val aboutLibrariesVersion by rootProject.extra("8.3.0")
        classpath("com.mikepenz.aboutlibraries.plugin:aboutlibraries-plugin:$aboutLibrariesVersion")
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        google()
        jcenter()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

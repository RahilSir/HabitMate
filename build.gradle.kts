// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // These aliases reference versions defined in the 'libs.versions.toml' file in a new project.
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false

    // The Google Services plugin MUST be declared here
   
}




buildscript {
    repositories {
        google()      // MUST include
        mavenCentral()
    }
    dependencies {
        classpath("com.google.gms:google-services:4.4.3") // or latest version
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services") // Apply the Google Services plugin
}

android {
    namespace = "com.example.habittracker"
    compileSdk = 36 // Use a recent SDK

    defaultConfig {
        applicationId = "com.example.habittracker"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

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
    }
    compileOptions {
        // Use Java 11 for modern Android development
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    // ANDROIDX & CORE LIBRARIES
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)

    // FIREBASE - Using Bill of Materials (BOM) for safe versioning
    // The BOM allows you to omit versions for individual Firebase libraries
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))


    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // GSON for easy JSON parsing (or use kotlinx.serialization)
    implementation("com.google.code.gson:gson:2.10.1")


    // Firebase Realtime Database KTX
    implementation("com.google.firebase:firebase-database-ktx")

    // Other Firebase dependencies you may need (like Auth for user ID)
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation(libs.androidx.activity)

    // TESTING
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // ANDROIDX & CORE LIBRARIES
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)

    // FIREBASE - Using Bill of Materials (BOM) for safe versioning
    // The BOM allows you to omit versions for individual Firebase libraries
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))

    // Firebase Realtime Database KTX
    implementation("com.google.firebase:firebase-database-ktx")

    // Other Firebase dependencies you may need (like Auth for user ID)
    implementation("com.google.firebase:firebase-auth-ktx")

    // >>>>>>> NETWORKING LIBRARIES REQUIRED FOR RetrofitClient and ApiService <<<<<<<
    // Retrofit core library
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    // Gson converter (to parse JSON into Kotlin objects)
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    // OkHttp Logging Interceptor (optional, but helpful for debugging network calls)
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    // <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

    // TESTING
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)



}

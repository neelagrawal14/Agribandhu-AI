plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.agribandhu"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.agribandhu"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    implementation("com.google.firebase:firebase-analytics")
    implementation(platform("com.google.firebase:firebase-bom:34.5.0"))
    // ✅ Networking and JSON
    implementation("com.squareup.okhttp3:okhttp:5.2.1")
    implementation("com.google.code.gson:gson:2.13.2")
    implementation("com.google.firebase:firebase-storage:20.3.0")
    // ✅ AnyChart library for graphs
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation(libs.firebase.database)

    // (Optional) Remove this if not needed anymore
    // implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

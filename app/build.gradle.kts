plugins {
    alias(libs.plugins.android.application)
    // Add other plugins if needed
}

android {
    namespace = "com.example.machinenote"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.machinenote"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.logging.interceptor)
    implementation(libs.converter.gson)
    implementation(libs.retrofit)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.activity)
    implementation(libs.camera.view)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation (libs.barcode.scanning)

    implementation (libs.camera.lifecycle)
    implementation (libs.camera.core)
    implementation (libs.camera.camera2)

    implementation ("com.github.leondzn:simple-analog-clock:1.0.1")
}

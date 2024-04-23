plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    kotlin("kapt")
}

android {
    namespace = "com.example.quickquery"
    compileSdk = 34
    buildFeatures {
        dataBinding = true
    }
    defaultConfig {
        applicationId = "com.example.quickquery"
        minSdk = 21
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material.components)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.activity.ktx)

    // ViewModel and LiveData
    implementation(libs.androidx.lifecycleViewModel)
    implementation(libs.androidx.lifecycleLiveData)
    // Retrofit for network requests
    implementation(libs.retrofit2)
    implementation(libs.retrofit2ConverterGson)
    // OkHttp for HTTP client
    implementation(libs.okhttp3)
    implementation(libs.okhttp3LoggingInterceptor)
    // Coroutines for asynchronous tasks
    implementation(libs.kotlinx.coroutinesAndroid)
    // Room for local database
    implementation(libs.androidx.roomRuntime)
    kapt(libs.androidx.roomCompiler)
    // Optional - Room support for coroutines
    implementation(libs.androidx.roomKtx)
    // WebView
    implementation(libs.androidx.webkit)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.lottie)

    // Unit and Instrumentation Test Implementations
    testImplementation(libs.androidx.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.inline)
    testImplementation(libs.robolectric.lib)
    testImplementation(libs.coroutines.test)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

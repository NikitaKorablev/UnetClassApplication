plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-parcelize")
}

android {
    namespace = "com.app.unetclass"
    compileSdk {
        version = release(34)
    }

    defaultConfig {
        applicationId = "com.app.unetclass"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        externalNativeBuild {
            ndkBuild {
                abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a"))
            }
        }
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
    buildFeatures {
        viewBinding = true
    }

    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("libs")
        }
    }
}

dependencies {
    implementation("org.pytorch:pytorch_android:2.1.0")
    implementation("org.pytorch:pytorch_android_torchvision:2.1.0")

//    // Ядро PyTorch Mobile
//    implementation("org.pytorch:pytorch_android_lite:1.13.0")
//    // TorchVision для удобной работы с изображениями/тензорами
//    implementation("org.pytorch:pytorch_android_torchvision_lite:1.13.0")
//
//    implementation("com.facebook.soloader:soloader:0.10.5")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.activity:activity-ktx:1.8.0")

    // PhotoView для масштабирования изображений
    implementation("com.github.chrisbanes:PhotoView:2.3.0")
    // Glide для загрузки и кэширования изображений
    implementation("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

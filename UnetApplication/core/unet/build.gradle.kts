plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.app.unet"
    compileSdk {
        version = release(36)
    }

    androidResources {
        noCompress += "tflite"
    }

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.datastore)

    // Dagger-Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.work.runtime)

    //PyTorch
    implementation(libs.pytorch.android)
    implementation(libs.pytorch.android.torchvision)

    // Основное ядро LiteRT
    implementation(libs.litert)
//    implementation(libs.litert.gpu)
//    implementation(libs.litert.support.api)
    implementation(libs.litert.support.api) {
        exclude(group = "com.google.ai.edge.litert", module = "litert-api")
    }
    implementation(libs.litert.gpu.api)
//    implementation(libs.play.services.tflite.java)
//    implementation(libs.play.services.tflite.gpu)

    // Glide для загрузки и кэширования изображений
    implementation(libs.glide)
    annotationProcessor(libs.compiler)

    // PhotoView для масштабирования изображений
    implementation(libs.photoview)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
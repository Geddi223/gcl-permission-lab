plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.gcl.permissionlab"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.gcl.permissionlab"
        minSdk = 26
        targetSdk = 34
        versionCode = 2
        versionName = "2.0"
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
    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.camera:camera-core:1.4.1")
    implementation("androidx.camera:camera-camera2:1.4.1")
    implementation("androidx.camera:camera-lifecycle:1.4.1")
    implementation("androidx.camera:camera-view:1.4.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("com.google.android.gms:play-services-location:21.3.0")
}

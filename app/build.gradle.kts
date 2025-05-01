plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp") version "2.0.21-1.0.27"
}

android {
    namespace = "com.example.summitdiary"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.summitdiary"
        minSdk = 26
        targetSdk = 35
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures{
        viewBinding = true
    }
    sourceSets {
        getByName("main").java.srcDir("build/generated/ksp/main/kotlin")
    }
}

dependencies {
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime.android)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.coil)
    implementation(libs.glide)
    ksp(libs.glideCompiler)
    implementation(libs.osmdroid.android)
    implementation(libs.osmdroid.wms)
    implementation(libs.photoview)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.logging.interceptor)

}
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

android {
    namespace = "gold.app.ghahremani"
    compileSdk = 35

    signingConfigs {
        create("release") {
            storeFile = file("/home/user/workspace/gold-release.keystore")
            storePassword = "goldcalculator2026"
            keyAlias = "gold-key"
            keyPassword = "goldcalculator2026"
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
        }
    }

    defaultConfig {
        applicationId = "gold.app.ghahremani"
        minSdk = 24
        targetSdk = 35
        versionCode = 5
        versionName = "1.5"

        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a")
        }

        // تنظیمات مایکت برای پرداخت درون‌برنامه‌ای
        val marketApplicationId = "ir.mservices.market"
        val marketBindAddress = "ir.mservices.market.InAppBillingService.BIND"
        manifestPlaceholders["marketApplicationId"] = marketApplicationId
        manifestPlaceholders["marketBindAddress"] = marketBindAddress
        manifestPlaceholders["marketPermission"] = "$marketApplicationId.BILLING"
        buildConfigField("String", "MYKET_PUBLIC_KEY", "\"MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCHdIaay28couOD+/ljVTUEowPRG/+vsH/F8m4gUuM2kpXCtfWMtKO50b4lxe1EHupZynijWFUvzKUViVMaii1pYDonLZY+vvYMUGxgGeEmbHX7nqTwDkkhNivrSl3UVe0KPwXoIWjJpXLZoHn6mGI/HM+kwcAVgbkrWmOK+0O70QIDAQAB\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        // Kotlin 2.0+ uses the Compose plugin, no kotlinCompilerExtensionVersion needed
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)

    // Core
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")

    // Compose
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    // آیکون‌های پایه به‌صورت پیش‌فرض در material3 گنجانده شده‌اند

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.4")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // OkHttp برای دریافت قیمت طلا
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Myket In-App Billing
    implementation("com.github.myketstore:myket-billing-client:1.19")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // WorkManager برای نوتیفیکیشن‌های دوره‌ای
    implementation("androidx.work:work-runtime-ktx:2.10.0")

    // Splash Screen API - استفاده از اسپلش اختصاصی
    // implementation("androidx.core:core-splashscreen:1.0.1")

    // Debugging
    debugImplementation("androidx.compose.ui:ui-tooling")
}

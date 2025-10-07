/*
 * MIT License
 *
 * Copyright (c) 2020 Shreyas Patil
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("org.jlleitschuh.gradle.ktlint")
}

android {
    compileSdkVersion(31)
    buildToolsVersion("30.0.3")

    defaultConfig {
        applicationId = "dev.shreyaspatil.foodium"
        minSdkVersion(21)
        targetSdkVersion(31)
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "dev.shreyaspatil.foodium.CustomTestRunner"

        javaCompileOptions {
            annotationProcessorOptions {
                arguments.plusAssign(
                    hashMapOf(
                        "room.schemaLocation" to "$projectDir/schemas",
                        "room.incremental" to "true",
                        "room.expandProjection" to "true"
                    )
                )
            }
        }
    }

    buildFeatures.viewBinding = true

    buildTypes {
        getByName("release") {
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

    packagingOptions {
        exclude("META-INF/*.kotlin_module")
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    // Kotlin
    implementation(Dependencies.kotlin)

    // Coroutines
    implementation(Coroutines.core)
    implementation(Coroutines.android)

    // Android
    implementation(Android.appcompat)
    implementation(Android.activityKtx)
    implementation(Android.coreKtx)
    implementation(Android.constraintLayout)
    implementation(Android.swipeRefreshLayout)

    // Architecture Components
    implementation(Lifecycle.viewModel)
    implementation(Lifecycle.liveData)
    implementation(Lifecycle.runtimeKtx)

    // Room components
    implementation(Room.runtime)
    implementation(Room.ktx)
    kapt(Room.compiler)

    // Material Design
    implementation(Dependencies.materialDesign)
    implementation(Dependencies.materialDialog)

    // Coil-kt
    implementation(Dependencies.coil)

    // Retrofit
    implementation(Retrofit.retrofit)
    implementation(Retrofit.moshiRetrofitConverter)

    // Moshi
    implementation(Moshi.moshi)
    implementation(Moshi.codeGen)
    kapt(Moshi.codeGen)

    // Hilt + Dagger
    implementation(Hilt.hiltAndroid)
    implementation(Hilt.hiltViewModel)
    kapt(Hilt.daggerCompiler)
    kapt(Hilt.hiltCompiler)

    // Testing
    testImplementation(Testing.core)
    testImplementation(Testing.coroutines)
    testImplementation(Testing.room)
    testImplementation(Testing.okHttp)
    testImplementation(Testing.jUnit)
    testImplementation(Testing.truth)

    // Android Testing
    androidTestImplementation(Testing.extJUnit)
    androidTestImplementation(Testing.espresso)
    androidTestImplementation(Testing.runner)
    androidTestImplementation(Testing.rules)
    androidTestImplementation(Testing.coreKtx)
    androidTestImplementation(Testing.espressoContrib)
    androidTestImplementation(Hilt.hiltAndroid)
    androidTestImplementation(Hilt.hiltTesting)
    kaptAndroidTest(Hilt.daggerCompiler)

    // Additional unit test tools
    testImplementation("org.robolectric:robolectric:4.6.1")
    testImplementation("androidx.test:core:1.3.0")
}

ktlint {
    android.set(true)
    outputColorName.set("RED")
}

// Copy debug APK to app/prod after assembleDebug
val copyDebugApkToProd by tasks.register<org.gradle.api.tasks.Copy>("copyDebugApkToProd") {
    val apk = layout.buildDirectory.file("outputs/apk/debug/app-debug.apk")
    from(apk)
    into("$projectDir/prod")
}

afterEvaluate {
    tasks.named("assembleDebug").configure {
        finalizedBy(copyDebugApkToProd)
    }
}

// AndroidTest helper tasks (API-agnostic; target device is chosen via -PdeviceSerial or ANDROID_SERIAL)
val deviceSerial = providers.gradleProperty("deviceSerial").orElse("emulator-5556")
val adbPath = File(android.sdkDirectory, "platform-tools/adb").absolutePath

tasks.register("disableDeviceAnimations") {
    group = "verification"
    description = "Disable window/transition/animator animations on the target device"
    doLast {
        exec { commandLine(adbPath, "-s", deviceSerial.get(), "shell", "settings", "put", "global", "window_animation_scale", "0") }
        exec { commandLine(adbPath, "-s", deviceSerial.get(), "shell", "settings", "put", "global", "transition_animation_scale", "0") }
        exec { commandLine(adbPath, "-s", deviceSerial.get(), "shell", "settings", "put", "global", "animator_duration_scale", "0") }
    }
}

tasks.register("installDebugAndTestsOnDevice") {
    group = "verification"
    description = "Install app debug and androidTest APKs on the target device"
    dependsOn("assembleDebug", "assembleDebugAndroidTest")
    doLast {
        val debugApk = layout.buildDirectory.file("outputs/apk/debug/app-debug.apk").get().asFile.absolutePath
        val testApk = layout.buildDirectory.file("outputs/apk/androidTest/debug/app-debug-androidTest.apk").get().asFile.absolutePath
        exec { commandLine(adbPath, "-s", deviceSerial.get(), "install", "-r", "-d", debugApk) }
        exec { commandLine(adbPath, "-s", deviceSerial.get(), "install", "-r", "-d", testApk) }
    }
}

tasks.register("runMainActivityTestOnDevice") {
    group = "verification"
    description = "Run MainActivityTest via instrumentation on the target device"
    dependsOn("installDebugAndTestsOnDevice", "disableDeviceAnimations")
    doLast {
        exec {
            commandLine(
                adbPath,
                "-s",
                deviceSerial.get(),
                "shell",
                "am",
                "instrument",
                "-w",
                "-r",
                "-e",
                "class",
                "dev.shreyaspatil.foodium.ui.main.MainActivityTest",
                "dev.shreyaspatil.foodium.test/dev.shreyaspatil.foodium.CustomTestRunner"
            )
        }
    }
}

tasks.register("androidTestApi30") {
    group = "verification"
    description = "Disable animations, install APKs, and run MainActivityTest on the device (use -PdeviceSerial)"
    dependsOn("runMainActivityTestOnDevice")
}

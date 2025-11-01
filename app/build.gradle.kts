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

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

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
}

ktlint {
    android.set(true)
    outputColorName.set("RED")
}

// --- Allure CLI (from JUnit XML) & Packaging tasks ---
val allureVersion = "2.13.9"
val allureBaseDir = File(buildDir, "allure-cli")
val allureZip = File(allureBaseDir, "allure-${allureVersion}.zip")
val allureHome = File(allureBaseDir, "allure-${allureVersion}")
val allureBin = File(allureHome, "bin/allure").absolutePath

tasks.register("downloadAllureCli") {
    group = "verification"
    description = "Download Allure CLI ${allureVersion}"
    doLast {
        allureBaseDir.mkdirs()
        exec {
            commandLine(
                "bash", "-lc",
                "set -euo pipefail; cd '${allureBaseDir.absolutePath}' && " +
                    "curl -L -o '${allureZip.name}' 'https://repo1.maven.org/maven2/io/qameta/allure/allure-commandline/${allureVersion}/allure-commandline-${allureVersion}.zip'"
            )
        }
    }
}

tasks.register("unpackAllureCli") {
    group = "verification"
    description = "Unpack Allure CLI"
    dependsOn("downloadAllureCli")
    doLast {
        exec {
            commandLine(
                "bash", "-lc",
                "set -euo pipefail; cd '${allureBaseDir.absolutePath}' && unzip -o '${allureZip.name}'"
            )
        }
    }
}

fun ensureReportsDir(path: String) = File(rootDir, path).apply { mkdirs() }

tasks.register("generateAllureUnitReportFromJUnitXml") {
    group = "verification"
    description = "Generate Allure report for unit tests from JUnit XML"
    dependsOn("unpackAllureCli", "testDebugUnitTest")
    doLast {
        val resultsDir = File(buildDir, "test-results/testDebugUnitTest").absolutePath
        val outDir = ensureReportsDir("reports/allure/unit").absolutePath
        exec {
            commandLine("bash", "-lc", "'${allureBin}' generate '${resultsDir}' -c -o '${outDir}'")
        }
    }
}

tasks.register("generateAllureAndroidTestReportFromXml") {
    group = "verification"
    description = "Generate Allure report for androidTest from JUnit XML"
    dependsOn("unpackAllureCli")
    doLast {
        val resultsDir = File(buildDir, "outputs/androidTest-results/connected").absolutePath
        val outDir = ensureReportsDir("reports/allure/androidTest").absolutePath
        exec {
            commandLine("bash", "-lc", "'${allureBin}' generate '${resultsDir}' -c -o '${outDir}'")
        }
    }
}

tasks.register("generateAllureAndroidTestAggReportFromXml") {
    group = "verification"
    description = "Generate aggregated Allure report for androidTest"
    dependsOn("unpackAllureCli")
    doLast {
        val resultsDir = File(buildDir, "outputs/androidTest-results/connected").absolutePath
        val outDir = ensureReportsDir("reports/allure/androidTest-agg").absolutePath
        exec {
            commandLine("bash", "-lc", "'${allureBin}' generate '${resultsDir}' -c -o '${outDir}'")
        }
    }
}

tasks.register("openAllureUnitServer") {
    group = "verification"
    description = "Open local server for Allure Unit report on :5252"
    dependsOn("generateAllureUnitReportFromJUnitXml")
    doLast {
        exec { commandLine("bash", "-lc", "'${allureBin}' open '${rootDir}/reports/allure/unit' -p 5252") }
    }
}

tasks.register("openAllureAndroidTestServer") {
    group = "verification"
    description = "Open local server for Allure androidTest report on :5254"
    dependsOn("generateAllureAndroidTestReportFromXml")
    doLast {
        exec { commandLine("bash", "-lc", "'${allureBin}' open '${rootDir}/reports/allure/androidTest' -p 5254") }
    }
}

tasks.register("openAllureAndroidTestAggServer") {
    group = "verification"
    description = "Open local server for Allure aggregated androidTest report on :5256"
    dependsOn("generateAllureAndroidTestAggReportFromXml")
    doLast {
        exec { commandLine("bash", "-lc", "'${allureBin}' open '${rootDir}/reports/allure/androidTest-agg' -p 5256") }
    }
}

// Copy debug APK into app/prod
tasks.register("copyDebugApkToProd") {
    group = "distribution"
    description = "Copy app-debug.apk to app/prod/"
    dependsOn("assembleDebug")
    doLast {
        val src = File(buildDir, "outputs/apk/debug/app-debug.apk")
        val dstDir = File(projectDir, "prod").apply { mkdirs() }
        if (src.exists()) {
            src.copyTo(File(dstDir, "app-debug.apk"), overwrite = true)
        } else {
            throw GradleException("APK not found: ${src.absolutePath}")
        }
    }
}

// Package distribution zip with reports and APK
tasks.register<org.gradle.api.tasks.bundling.Zip>("buildDist") {
    group = "distribution"
    description = "Create dist/Foodium_diplom.zip with reports and APK"
    destinationDirectory.set(File(rootDir, "dist"))
    archiveFileName.set("Foodium_diplom.zip")
    dependsOn("copyDebugApkToProd")

    // Include reports if already generated
    from(File(rootDir, "reports")) {
        into("Foodium_diplom/reports")
    }
    from(File(projectDir, "prod")) {
        into("Foodium_diplom/app")
    }
    from(File(rootDir, "readme_dip.md")) {
        into("Foodium_diplom")
    }
}

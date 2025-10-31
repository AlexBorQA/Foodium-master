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
    id("jacoco")
    id("io.qameta.allure") version "2.11.2"
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
        getByName("debug") {
            // Enable code coverage for androidTest on debug variant
            isTestCoverageEnabled = true
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

allure {
    report {
        version.set("2.13.9")
    }
    adapter {
        autoconfigure.set(false)
        frameworks {
            junit4 {
                enabled.set(false)
            }
        }
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

// Configure Allure results directory for unit tests
tasks.withType<Test>().configureEach {
    systemProperty("allure.results.directory", "$buildDir/allure-results")
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
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")
    androidTestImplementation(Hilt.hiltAndroid)
    androidTestImplementation(Hilt.hiltTesting)
    kaptAndroidTest(Hilt.daggerCompiler)

    // Additional unit test tools
    testImplementation("org.robolectric:robolectric:4.6.1")
    testImplementation("androidx.test:core:1.3.0")

    // Allure for unit tests (Kotlin JUnit4)
    testImplementation("io.qameta.allure:allure-kotlin-model:2.4.0")
    testImplementation("io.qameta.allure:allure-kotlin-commons:2.4.0")
    testImplementation("io.qameta.allure:allure-kotlin-junit4:2.4.0")
    // Classic Allure JUnit4 adapter (stable for JVM tests)
    testImplementation("io.qameta.allure:allure-junit4:2.13.9")
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

// Jacoco configuration and reports (unit + androidTest)
jacoco {
    toolVersion = "0.8.8"
}

tasks.withType<org.gradle.testing.jacoco.tasks.JacocoReport>().configureEach {
    reports {
        xml.required.set(true)
        csv.required.set(false)
        html.required.set(true)
    }
}

fun coverageClassDirs() = files(
    fileTree("$buildDir/tmp/kotlin-classes/debug") {
        exclude(
            "**/R.class",
            "**/R$*.class",
            "**/BuildConfig.*",
            "**/*Manifest*.*",
            "**/*Test*.*",
            "**/*Dagger*.*",
            "**/*Hilt*.*",
            "**/*_Factory.*",
            "**/*_MembersInjector.*",
            "**/*_Provide*Factory.*",
            "**/dagger/**",
            "**/hilt_aggregated_deps/**",
            "**/databinding/**",
            "**/android/databinding/**",
            "**/androidx/databinding/**",
            "**/BR.class"
        )
    },
    fileTree("$buildDir/intermediates/javac/debug/classes") {
        exclude(
            "**/R.class",
            "**/R$*.class",
            "**/BuildConfig.*",
            "**/*Manifest*.*",
            "**/*Test*.*",
            "**/*Dagger*.*",
            "**/*Hilt*.*",
            "**/*_Factory.*",
            "**/*_MembersInjector.*",
            "**/*_Provide*Factory.*",
            "**/dagger/**",
            "**/hilt_aggregated_deps/**",
            "**/databinding/**",
            "**/android/databinding/**",
            "**/androidx/databinding/**",
            "**/BR.class"
        )
    }
)

val coverageSourceDirs = files("src/main/java", "src/main/kotlin")

tasks.register<org.gradle.testing.jacoco.tasks.JacocoReport>("jacocoUnitTestReport") {
    group = "verification"
    description = "Generates Jacoco coverage report for unit tests"
    dependsOn("testDebugUnitTest")

    classDirectories.setFrom(coverageClassDirs())
    sourceDirectories.setFrom(coverageSourceDirs)
    executionData.setFrom(
        fileTree(buildDir) {
            include("**/testDebugUnitTest.exec")
        }
    )

    reports {
        html.outputLocation.set(
            file("$buildDir/reports/jacoco/jacocoUnitTestReport/html")
        )
        xml.outputLocation.set(
            file(
                "$buildDir/reports/jacoco/jacocoUnitTestReport/jacoco-unit.xml"
            )
        )
    }
}

tasks.register<org.gradle.testing.jacoco.tasks.JacocoReport>("jacocoAndroidTestReport") {
    group = "verification"
    description = "Generates Jacoco coverage report for android instrumentation tests"
    dependsOn("connectedDebugAndroidTest")

    classDirectories.setFrom(coverageClassDirs())
    sourceDirectories.setFrom(coverageSourceDirs)
    executionData.setFrom(
        fileTree("$buildDir/outputs/code_coverage/debugAndroidTest/connected") {
            include("**/*.ec")
        }
    )

    reports {
        html.outputLocation.set(
            file("$buildDir/reports/jacoco/jacocoAndroidTestReport/html")
        )
        xml.outputLocation.set(
            file(
                "$buildDir/reports/jacoco/jacocoAndroidTestReport/jacoco-androidTest.xml"
            )
        )
    }
}

// AndroidTest helper tasks (API-agnostic; target device is chosen via -PdeviceSerial or ANDROID_SERIAL)
val deviceSerial = providers.gradleProperty("deviceSerial").orElse("emulator-5556")
val adbPath = File(android.sdkDirectory, "platform-tools/adb").absolutePath

tasks.register("disableDeviceAnimations") {
    group = "verification"
    description = "Disable window/transition/animator animations on the target device"
    doLast {
        exec {
            commandLine(
                adbPath, "-s", deviceSerial.get(), "shell", "settings", "put",
                "global", "window_animation_scale", "0"
            )
        }
        exec {
            commandLine(
                adbPath, "-s", deviceSerial.get(), "shell", "settings", "put",
                "global", "transition_animation_scale", "0"
            )
        }
        exec {
            commandLine(
                adbPath, "-s", deviceSerial.get(), "shell", "settings", "put",
                "global", "animator_duration_scale", "0"
            )
        }
    }
}

tasks.register("installDebugAndTestsOnDevice") {
    group = "verification"
    description = "Install app debug and androidTest APKs on the target device"
    dependsOn("assembleDebug", "assembleDebugAndroidTest")
    doLast {
        val debugApk = layout.buildDirectory
            .file("outputs/apk/debug/app-debug.apk")
            .get().asFile.absolutePath
        val testApk = layout.buildDirectory
            .file("outputs/apk/androidTest/debug/app-debug-androidTest.apk")
            .get().asFile.absolutePath
        exec {
            commandLine(adbPath, "-s", deviceSerial.get(), "install", "-r", "-d", debugApk)
        }
        exec {
            commandLine(adbPath, "-s", deviceSerial.get(), "install", "-r", "-d", testApk)
        }
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
    description = "Run MainActivityTest on device (use -PdeviceSerial)"
    dependsOn("runMainActivityTestOnDevice")
}

// Run ALL androidTest via Allure runner on target device
tasks.register("runAndroidTestsAllureOnDevice") {
    group = "verification"
    description = "Run all androidTest via AllureHiltTestRunner on the target device"
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
                "dev.shreyaspatil.foodium.test/dev.shreyaspatil.foodium.AllureHiltTestRunner"
            )
        }
    }
}

// --- Allure reports (unit + androidTest) and copy to root reports/ ---

// Pull allure-results from device after connected tests
val pullAndroidTestAllureResults by tasks.register<DefaultTask>("pullAndroidTestAllureResults") {
    group = "verification"
    description = "Pull /sdcard/allure-results from device to build/allure-results-androidTest"
    dependsOn("connectedDebugAndroidTest")
    doLast {
        val outDir = file("$buildDir/allure-results-androidTest")
        outDir.mkdirs()
        exec {
            commandLine(adbPath, "-s", deviceSerial.get(), "pull", "/sdcard/allure-results", outDir.absolutePath)
            isIgnoreExitValue = true
        }
        exec {
            commandLine(
                adbPath, "-s", deviceSerial.get(), "pull",
                "/sdcard/Android/data/dev.shreyaspatil.foodium.test/files/allure-results",
                outDir.absolutePath
            )
            isIgnoreExitValue = true
        }
    }
}

// Allure CLI configuration for generating reports via JavaExec
val allureCli by configurations.creating

dependencies {
    add("allureCli", "io.qameta.allure:allure-commandline:2.13.9")
}

// Generate Allure HTML for unit tests using Allure CLI
tasks.register<JavaExec>("generateAllureUnitReport") {
    group = "verification"
    description = "Generate Allure HTML report for unit tests using CLI"
    dependsOn("testDebugUnitTest")
    val inputDir = file("$buildDir/allure-results")
    val outDir = file("$buildDir/reports/allure/unit")
    classpath = allureCli
    mainClass.set("io.qameta.allure.CommandLine")
    onlyIf { inputDir.exists() && (inputDir.list()?.isNotEmpty() == true) }
    args("generate", inputDir.absolutePath, "-c", "-o", outDir.absolutePath)
}

// Generate Allure HTML for androidTest using Allure CLI
tasks.register<JavaExec>("generateAllureAndroidTestReport") {
    group = "verification"
    description = "Generate Allure HTML report for androidTest using CLI"
    dependsOn(pullAndroidTestAllureResults)
    val inputDir = file("$buildDir/allure-results-androidTest/allure-results")
    val outDir = file("$buildDir/reports/allure/androidTest")
    classpath = allureCli
    mainClass.set("io.qameta.allure.CommandLine")
    onlyIf { inputDir.exists() && (inputDir.list()?.isNotEmpty() == true) }
    args("generate", inputDir.absolutePath, "-c", "-o", outDir.absolutePath)
}

// Generate Allure HTML for unit tests directly from JUnit XML
tasks.register<JavaExec>("generateAllureUnitReportFromJUnitXml") {
    group = "verification"
    description = "Generate Allure HTML for unit tests from JUnit XML"
    dependsOn("testDebugUnitTest")
    val inputDir = file("$buildDir/test-results/testDebugUnitTest")
    val outDir = file("$buildDir/reports/allure/unit")
    classpath = allureCli
    mainClass.set("io.qameta.allure.CommandLine")
    onlyIf { inputDir.exists() && (inputDir.list()?.isNotEmpty() == true) }
    args("generate", inputDir.absolutePath, "-c", "-o", outDir.absolutePath)
}

// Generate Allure HTML for androidTest directly from connected JUnit XML
tasks.register<JavaExec>("generateAllureAndroidTestReportFromXml") {
    group = "verification"
    description = "Generate Allure HTML for androidTest from connected JUnit XML"
    dependsOn("connectedDebugAndroidTest")
    val inputDir = file("$buildDir/outputs/androidTest-results/connected")
    val outDir = file("$buildDir/reports/allure/androidTest")
    classpath = allureCli
    mainClass.set("io.qameta.allure.CommandLine")
    onlyIf { inputDir.exists() && (inputDir.list()?.isNotEmpty() == true) }
    args("generate", inputDir.absolutePath, "-c", "-o", outDir.absolutePath)
}

tasks.register<DefaultTask>("copyAllureReportsFromXmlToRoot") {
    group = "verification"
    description = "Copy Allure HTML (from XML) to root reports/"
    dependsOn("generateAllureUnitReportFromJUnitXml", "generateAllureAndroidTestReportFromXml")
    doLast {
        copy {
            from("$buildDir/reports/allure/unit")
            into("${rootDir}/reports/allure/unit")
        }
        copy {
            from("$buildDir/reports/allure/androidTest")
            into("${rootDir}/reports/allure/androidTest")
        }
    }
}

// --- Alternative path: download Allure CLI (zip) and run binary ---
tasks.register("downloadAllureCli") {
    group = "verification"
    description = "Download Allure CLI zip"
    doLast {
        val outDir = file("$buildDir/allure-cli").apply { mkdirs() }
        val zipFile = file("$buildDir/allure-cli/allure.zip")
        ant.withGroovyBuilder {
            "get"(
                mapOf(
                    "src" to "https://repo1.maven.org/maven2/io/qameta/allure/allure-commandline/2.13.9/allure-commandline-2.13.9.zip",
                    "dest" to zipFile.absolutePath,
                    "usetimestamp" to true
                )
            )
        }
    }
}

tasks.register<Copy>("unpackAllureCli") {
    group = "verification"
    description = "Unpack Allure CLI zip"
    dependsOn("downloadAllureCli")
    from({ zipTree(file("$buildDir/allure-cli/allure.zip")) })
    into("$buildDir/allure-cli/allure")
}

tasks.register<Exec>("generateAllureUnitReportViaBin") {
    group = "verification"
    description = "Generate Allure unit HTML via downloaded CLI binary (from JUnit XML)"
    dependsOn("testDebugUnitTest", "unpackAllureCli")
    doFirst {
        file("$buildDir/reports/allure/unit").mkdirs()
    }
    val bin = file("$buildDir/allure-cli/allure/allure-2.13.9/bin/allure").absolutePath
    commandLine(bin, "generate", file("$buildDir/test-results/testDebugUnitTest").absolutePath, "-c", "-o", file("$buildDir/reports/allure/unit").absolutePath)
}

tasks.register<Exec>("generateAllureAndroidTestReportViaBin") {
    group = "verification"
    description = "Generate Allure androidTest HTML via downloaded CLI binary (from connected JUnit XML)"
    dependsOn("connectedDebugAndroidTest", "unpackAllureCli")
    doFirst {
        file("$buildDir/reports/allure/androidTest").mkdirs()
    }
    val bin = file("$buildDir/allure-cli/allure/allure-2.13.9/bin/allure").absolutePath
    commandLine(bin, "generate", file("$buildDir/outputs/androidTest-results/connected").absolutePath, "-c", "-o", file("$buildDir/reports/allure/androidTest").absolutePath)
}

tasks.register("copyAllureReportsViaBinToRoot") {
    group = "verification"
    description = "Copy Allure HTML (via bin) to root reports/"
    dependsOn("generateAllureUnitReportViaBin", "generateAllureAndroidTestReportViaBin")
    doLast {
        copy {
            from("$buildDir/reports/allure/unit")
            into("${rootDir}/reports/allure/unit")
        }
        copy {
            from("$buildDir/reports/allure/androidTest")
            into("${rootDir}/reports/allure/androidTest")
        }
    }
}

// Copy Jacoco HTML reports to root reports/
tasks.register<org.gradle.api.tasks.Copy>("copyJacocoReportsToRoot") {
    group = "verification"
    description = "Copy Jacoco HTML reports to root reports/"
    dependsOn("jacocoUnitTestReport", "jacocoAndroidTestReport")
    from("$buildDir/reports/jacoco/jacocoUnitTestReport/html")
    into("${rootDir}/reports/jacoco/unit")
    doLast {
        copy {
            from("$buildDir/reports/jacoco/jacocoAndroidTestReport/html")
            into("${rootDir}/reports/jacoco/androidTest")
        }
    }
}

// Copy Allure HTML reports to root reports/
tasks.register<DefaultTask>("copyAllureReportsToRoot") {
    group = "verification"
    description = "Copy Allure HTML reports (unit + androidTest) to root reports/"
    dependsOn("generateAllureUnitReport", "generateAllureAndroidTestReport")
    doLast {
        copy {
            from("$buildDir/reports/allure/unit")
            into("${rootDir}/reports/allure/unit")
        }
        copy {
            from("$buildDir/reports/allure/androidTest")
            into("${rootDir}/reports/allure/androidTest")
        }
    }
}

// Copy only Allure unit HTML to root reports/
tasks.register<DefaultTask>("copyAllureUnitReportsToRoot") {
    group = "verification"
    description = "Copy Allure unit HTML report to root reports/"
    dependsOn("generateAllureUnitReport")
    doLast {
        copy {
            from("$buildDir/reports/allure/unit")
            into("${rootDir}/reports/allure/unit")
        }
    }
}

// Copy only Allure androidTest HTML to root reports/
tasks.register<DefaultTask>("copyAllureAndroidTestReportsToRoot") {
    group = "verification"
    description = "Copy Allure androidTest HTML report to root reports/"
    dependsOn("generateAllureAndroidTestReport")
    doLast {
        copy {
            from("$buildDir/reports/allure/androidTest")
            into("${rootDir}/reports/allure/androidTest")
        }
    }
}

// Pull allure-results from device after running am instrument (runMainActivityTestOnDevice)
val pullAndroidTestAllureResultsFromAm by tasks.register<DefaultTask>("pullAndroidTestAllureResultsFromAm") {
    group = "verification"
    description = "Pull Allure results after am instrument (runMainActivityTestOnDevice)"
    dependsOn("runMainActivityTestOnDevice")
    doLast {
        val outDir = file("$buildDir/allure-results-androidTest")
        outDir.mkdirs()
        exec {
            commandLine(adbPath, "-s", deviceSerial.get(), "pull", "/sdcard/allure-results", outDir.absolutePath)
            isIgnoreExitValue = true
        }
        exec {
            commandLine(
                adbPath, "-s", deviceSerial.get(), "pull",
                "/sdcard/Android/data/dev.shreyaspatil.foodium.test/files/allure-results",
                outDir.absolutePath
            )
            isIgnoreExitValue = true
        }
    }
}

// Generate Allure HTML for androidTest using results pulled after am instrument
tasks.register<JavaExec>("generateAllureAndroidTestReportFromAm") {
    group = "verification"
    description = "Generate Allure HTML for androidTest from am instrument results"
    dependsOn(pullAndroidTestAllureResultsFromAm)
    val inputDir = file("$buildDir/allure-results-androidTest/allure-results")
    val outDir = file("$buildDir/reports/allure/androidTest")
    classpath = allureCli
    mainClass.set("io.qameta.allure.CommandLine")
    onlyIf { inputDir.exists() && (inputDir.list()?.isNotEmpty() == true) }
    args("generate", inputDir.absolutePath, "-c", "-o", outDir.absolutePath)
}

tasks.register<DefaultTask>("copyAllureAndroidTestReportsToRootFromAm") {
    group = "verification"
    description = "Copy Allure androidTest HTML (from am instrument) to root reports/"
    dependsOn("generateAllureAndroidTestReportFromAm")
    doLast {
        copy {
            from("$buildDir/reports/allure/androidTest")
            into("${rootDir}/reports/allure/androidTest")
        }
    }
}

// Copy only Jacoco androidTest HTML to root reports/
tasks.register<org.gradle.api.tasks.Copy>("copyJacocoAndroidTestReportToRoot") {
    group = "verification"
    description = "Copy Jacoco androidTest HTML report to root reports/"
    dependsOn("jacocoAndroidTestReport")
    from("$buildDir/reports/jacoco/jacocoAndroidTestReport/html")
    into("${rootDir}/reports/jacoco/androidTest")
}

// Copy only Jacoco unit HTML to root reports/
tasks.register<org.gradle.api.tasks.Copy>("copyJacocoUnitTestReportToRoot") {
    group = "verification"
    description = "Copy Jacoco unit HTML report to root reports/"
    dependsOn("jacocoUnitTestReport")
    from("$buildDir/reports/jacoco/jacocoUnitTestReport/html")
    into("${rootDir}/reports/jacoco/unit")
}

// Aggregate: build all reports and copy to root
tasks.register("buildAllReports") {
    group = "verification"
    description = "Generate Allure (unit+androidTest) and Jacoco reports, copy to root reports/"
    dependsOn("generateAllureUnitReport", "generateAllureAndroidTestReport", "jacocoUnitTestReport", "jacocoAndroidTestReport", "copyAllureReportsToRoot", "copyJacocoReportsToRoot")
}

// Open Allure reports via CLI servers (no global allure required)
tasks.register<JavaExec>("openAllureUnitServer") {
    group = "verification"
    description = "Open Allure Unit report on http://localhost:5252"
    classpath = allureCli
    mainClass.set("io.qameta.allure.CommandLine")
    args("open", file("${rootDir}/reports/allure/unit").absolutePath, "-p", "5252")
}

tasks.register<JavaExec>("openAllureAndroidTestServer") {
    group = "verification"
    description = "Open Allure androidTest report on http://localhost:5254"
    classpath = allureCli
    mainClass.set("io.qameta.allure.CommandLine")
    args("open", file("${rootDir}/reports/allure/androidTest").absolutePath, "-p", "5254")
}

tasks.register<JavaExec>("openAllureAndroidTestAggServer") {
    group = "verification"
    description = "Open Allure aggregated androidTest report on http://localhost:5256"
    classpath = allureCli
    mainClass.set("io.qameta.allure.CommandLine")
    args("open", file("${rootDir}/reports/allure/androidTest-agg").absolutePath, "-p", "5256")
}

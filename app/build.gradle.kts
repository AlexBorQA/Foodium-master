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
        // enable coverage for androidTest on debug
        getByName("debug") {
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
}
// Jacoco configuration and reports
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

val coverageClassDirs = files(
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
            "**/dagger/**",
            "**/hilt_aggregated_deps/**",
            "**/databinding/**",
            "**/androidx/databinding/**",
            "**/android/databinding/**",
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
            "**/dagger/**",
            "**/hilt_aggregated_deps/**",
            "**/databinding/**",
            "**/androidx/databinding/**",
            "**/android/databinding/**",
            "**/BR.class"
        )
    }
)

val coverageSources = files("src/main/java", "src/main/kotlin")

tasks.register<org.gradle.testing.jacoco.tasks.JacocoReport>("jacocoUnitTestReport") {
    group = "verification"
    description = "Generates Jacoco coverage report for unit tests"
    dependsOn("testDebugUnitTest")
    classDirectories.setFrom(coverageClassDirs)
    sourceDirectories.setFrom(coverageSources)
    executionData.setFrom(fileTree(buildDir) { include("**/testDebugUnitTest.exec") })
    reports {
        html.outputLocation.set(file("$buildDir/reports/jacoco/jacocoUnitTestReport/html"))
        xml.outputLocation.set(file("$buildDir/reports/jacoco/jacocoUnitTestReport/jacoco-unit.xml"))
    }
}

tasks.register<org.gradle.testing.jacoco.tasks.JacocoReport>("jacocoAndroidTestReport") {
    group = "verification"
    description = "Generates Jacoco coverage report for androidTest"
    dependsOn("connectedDebugAndroidTest")
    classDirectories.setFrom(coverageClassDirs)
    sourceDirectories.setFrom(coverageSources)
    executionData.setFrom(fileTree("$buildDir/outputs/code_coverage/debugAndroidTest/connected") { include("**/*.ec") })
    reports {
        html.outputLocation.set(file("$buildDir/reports/jacoco/jacocoAndroidTestReport/html"))
        xml.outputLocation.set(file("$buildDir/reports/jacoco/jacocoAndroidTestReport/jacoco-androidTest.xml"))
    }
}

tasks.register<org.gradle.api.tasks.Copy>("copyJacocoReportsToRoot") {
    group = "verification"
    description = "Copy Jacoco HTML reports to root reports/"
    dependsOn("jacocoUnitTestReport")
    from("$buildDir/reports/jacoco/jacocoUnitTestReport/html")
    into("${rootDir}/reports/jacoco/unit")
    doLast {
        copy {
            from("$buildDir/reports/jacoco/jacocoAndroidTestReport/html")
            into("${rootDir}/reports/jacoco/androidTest")
        }
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

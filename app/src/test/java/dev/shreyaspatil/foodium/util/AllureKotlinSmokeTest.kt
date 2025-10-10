package dev.shreyaspatil.foodium.util

import io.qameta.allure.kotlin.Allure
import io.qameta.allure.kotlin.junit4.AllureJunit4
import org.junit.Rule
import org.junit.Test

class AllureKotlinSmokeTest {

    @Rule
    @JvmField
    val allure = AllureJunit4()

    @Test
    fun allure_kotlin_smoke() {
        Allure.step("Kotlin Allure smoke step")
        assert(true)
    }
}



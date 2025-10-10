package dev.shreyaspatil.foodium.allure

import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.qameta.allure.android.runners.AllureAndroidJUnit4
import io.qameta.allure.kotlin.Allure
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AllureAndroidJUnit4::class)
@HiltAndroidTest
class AllureSmokeTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun allure_smoke_generates_results() {
        val pkg = InstrumentationRegistry.getInstrumentation().targetContext.packageName
        Allure.step("Smoke: target package = $pkg")
    }
}



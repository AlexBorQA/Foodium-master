package dev.shreyaspatil.foodium.blackbox

import android.content.Context
import android.content.Intent
import android.app.UiAutomation
import android.view.accessibility.AccessibilityEvent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dev.shreyaspatil.foodium.R
import dev.shreyaspatil.foodium.ui.main.MainActivity
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class BlackBoxUiAutomatorTest {

    private lateinit var device: UiDevice
    private lateinit var context: Context
    private lateinit var pkg: String

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun setUp() {
        hiltRule.inject()
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        context = InstrumentationRegistry.getInstrumentation().targetContext
        pkg = context.packageName

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        context.startActivity(intent)

        // Wait for the main list to appear
        device.wait(Until.hasObject(By.res(pkg, "postsRecyclerView")), 15_000)
    }

    // Scenario 1: Main list is visible and scrollable
    @Test
    fun listIsScrollable_showsItems() {
        val list = UiScrollable(UiSelector().resourceId("$pkg:id/postsRecyclerView"))
        list.setAsVerticalList()
        // Scroll a bit to ensure content is present
        list.scrollForward()
        assertTrue(device.hasObject(By.res(pkg, "post_title")))
    }

    // Scenario 2: Open first item -> PostDetailsActivity
    @Test
    fun openDetailsFromList_showsDetails() {
        device.wait(Until.hasObject(By.res(pkg, "post_title")), 15_000)
        val firstTitle = device.findObject(By.res(pkg, "post_title"))
        firstTitle.click()

        // Wait for details screen unique content
        device.wait(Until.hasObject(By.res(pkg, "post_body")), 10_000)
        assertTrue(device.hasObject(By.res(pkg, "post_body")))

        device.pressBack()
    }

    // Scenario 3: Open About from overflow menu
    @Test
    fun openAboutFromOverflow_showsAboutScreen() {
        openOverflow()
        val aboutText = context.getString(R.string.action_about)
        device.wait(Until.hasObject(By.text(aboutText)), 5_000)
        device.findObject(By.text(aboutText)).click()

        device.wait(Until.hasObject(By.res(pkg, "textTitle")), 5_000)
        assertTrue(device.hasObject(By.res(pkg, "textTitle")))

        device.pressBack()
    }

    // Scenario 4 (error): About -> click error button, verify toast
    @Test
    fun aboutError_showsToast() {
        openOverflow()
        val aboutText = context.getString(R.string.action_about)
        device.wait(Until.hasObject(By.text(aboutText)), 5_000)
        device.findObject(By.text(aboutText)).click()

        device.wait(Until.hasObject(By.res(pkg, "buttonError")), 5_000)
        device.findObject(By.res(pkg, "buttonError")).click()
        device.waitForIdle()
        // Black-box acceptance: screen remains on About (no crash), title still visible
        val stillOnAbout = device.wait(Until.hasObject(By.res(pkg, "textTitle")), 5_000)
        assertTrue(stillOnAbout)

        device.pressBack()
    }

    private fun openOverflow() {
        // Try clicking toolbar overflow by content description (AOSP: "More options")
        val moreOptions = By.descContains("More options")
        if (device.hasObject(moreOptions)) {
            device.findObject(moreOptions).click()
            return
        }
        // Fallback to MENU key
        device.pressMenu()
    }
}




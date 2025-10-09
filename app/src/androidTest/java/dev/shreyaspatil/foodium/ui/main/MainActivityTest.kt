package dev.shreyaspatil.foodium.ui.main

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.action.ViewActions.pressMenuKey
import androidx.test.espresso.action.ViewActions.click as clickAction
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.platform.app.InstrumentationRegistry
import dev.shreyaspatil.foodium.R
import dev.shreyaspatil.foodium.util.AppIdleIdlingResource
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest

@LargeTest
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun setUp() {
        hiltRule.inject()
        AppIdleIdlingResource.register()
    }

    private fun launchMain() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val intent = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        InstrumentationRegistry.getInstrumentation().startActivitySync(intent)
    }

    @Test
    fun postsRecyclerView_isDisplayed_onLaunch() {
        launchMain()
        onView(withId(R.id.postsRecyclerView)).check(matches(isDisplayed()))
    }

    @Test
    fun clickFirstItem_opens_PostDetailsActivity() {
        launchMain()
        onView(withId(R.id.postsRecyclerView)).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0,
                click()
            )
        )
        onView(withId(R.id.post_title)).check(matches(isDisplayed()))
    }

    @Test
    fun aboutMenu_opens_AboutActivity() {
        launchMain()
        onView(isRoot()).perform(pressMenuKey())
        onView(withText(R.string.action_about)).perform(clickAction())
        onView(withId(R.id.textTitle)).check(matches(isDisplayed()))
    }
}

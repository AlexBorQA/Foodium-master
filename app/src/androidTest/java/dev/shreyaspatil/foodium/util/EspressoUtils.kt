package dev.shreyaspatil.foodium.util

import android.view.View
import androidx.annotation.IdRes
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.PerformException
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.recyclerview.widget.RecyclerView
import org.hamcrest.Matcher
import java.util.concurrent.TimeoutException

object EspressoUtils {
    fun waitFor(millis: Long): ViewAction = object : ViewAction {
        override fun getConstraints(): Matcher<View> = isRoot()
        override fun getDescription(): String = "Wait for $millis milliseconds."
        override fun perform(uiController: UiController, view: View?) {
            uiController.loopMainThreadForAtLeast(millis)
        }
    }

    fun waitForRecyclerItemCount(
        @IdRes recyclerViewId: Int,
        minCount: Int,
        timeoutMs: Long = 10_000L,
        pollIntervalMs: Long = 200L
    ): ViewAction = object : ViewAction {
        override fun getConstraints(): Matcher<View> = isRoot()
        override fun getDescription(): String = "Wait up to $timeoutMs ms for RecyclerView($recyclerViewId) to have >= $minCount items."
        override fun perform(uiController: UiController, view: View?) {
            requireNotNull(view)
            var elapsed = 0L
            while (elapsed <= timeoutMs) {
                val rv = view.rootView.findViewById<RecyclerView>(recyclerViewId)
                    ?: throw PerformException.Builder()
                        .withCause(IllegalStateException("RecyclerView not found: id=$recyclerViewId"))
                        .build()
                val count = rv.adapter?.itemCount ?: 0
                if (count >= minCount) return
                uiController.loopMainThreadForAtLeast(pollIntervalMs)
                elapsed += pollIntervalMs
            }
            throw PerformException.Builder()
                .withCause(TimeoutException("RecyclerView($recyclerViewId) did not reach $minCount items in $timeoutMs ms"))
                .build()
        }
    }
}



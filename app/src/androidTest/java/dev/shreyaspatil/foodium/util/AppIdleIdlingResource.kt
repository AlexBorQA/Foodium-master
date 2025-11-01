package dev.shreyaspatil.foodium.util

import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource

object AppIdleIdlingResource : IdlingResource {
    private var callback: IdlingResource.ResourceCallback? = null

    override fun getName(): String = "AppIdleIdlingResource"

    override fun isIdleNow(): Boolean {
        val idle = AppIdle.isIdle()
        if (idle) callback?.onTransitionToIdle()
        return idle
    }

    override fun registerIdleTransitionCallback(cb: IdlingResource.ResourceCallback) {
        callback = cb
    }

    fun register() { IdlingRegistry.getInstance().register(this) }
    fun unregister() { IdlingRegistry.getInstance().unregister(this) }
}

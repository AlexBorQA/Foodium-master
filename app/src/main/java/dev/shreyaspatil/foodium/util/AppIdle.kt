package dev.shreyaspatil.foodium.util

import java.util.concurrent.atomic.AtomicInteger

object AppIdle {
    private val counter = AtomicInteger(0)

    fun increment() { counter.incrementAndGet() }

    fun decrement() {
        val v = counter.decrementAndGet()
        if (v < 0) counter.set(0)
    }

    fun isIdle(): Boolean = counter.get() == 0
}

package com.trigonated.ctwtopheadlines.ui.misc

import java.util.concurrent.atomic.AtomicBoolean

object TestUtils {
    private var isRunningTest: AtomicBoolean? = null

    @Synchronized
    fun isOnInstrumentedTest(): Boolean {
        if (isRunningTest == null) {
            isRunningTest = AtomicBoolean(
                try {
                    Class.forName("androidx.test.espresso.Espresso")
                    true
                } catch (e: ClassNotFoundException) {
                    false
                }
            )
        }
        return isRunningTest?.get() ?: false
    }
}
package com.trigonated.ctwtopheadlines.misc

import android.view.View
import androidx.test.espresso.PerformException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import org.hamcrest.Matcher
import org.hamcrest.Matchers.anything

/**
 * Action to wait until a [condition] is met until [timeoutMs], throwing an exception otherwise.
 */
class WaitAction(
    private val condition: Matcher<View>,
    private val timeoutMs: Long
) : ViewAction {

    override fun getConstraints(): Matcher<View> {
        return anything() as Matcher<View>
    }

    override fun getDescription(): String = "wait"

    override fun perform(uiController: UiController, view: View) {
        uiController.loopMainThreadUntilIdle()
        val startTime: Long = System.currentTimeMillis()
        val endTime: Long = startTime + timeoutMs
        // Loop until either the condition is met or it times out
        while (System.currentTimeMillis() < endTime) {
            if (condition.matches(view)) return
            uiController.loopMainThreadForAtLeast(100);
        }
        // Timeout
        throw PerformException.Builder().withCause(Error()).build()
    }
}

/** Wait until a [condition] is met until [timeoutMs], throwing an exception otherwise. */
fun waitFor(condition: Matcher<View>, timeoutMs: Long): WaitAction = WaitAction(condition, timeoutMs)
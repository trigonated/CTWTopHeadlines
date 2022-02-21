package com.trigonated.ctwtopheadlines.misc

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

/**
 * Custom test runner used for providing an [HiltTestApplication].
 * The runner is set using [testInstrumentationRunner] on build.gradle.
 */
class CustomTestRunner() : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
}
package com.trigonated.ctwtopheadlines.misc

import app.cash.turbine.test
import kotlinx.coroutines.flow.Flow

/**
 * Convenience extension for:
 * ```kotlin
 * flow.test {
 *  ... expectMostRecentItem()
 * }
 * ```
 */
suspend fun <T> Flow<T>.expectMostRecentItem(): T {
    var item: T? = null
    test {
        item = expectMostRecentItem()
    }
    return item!!
}
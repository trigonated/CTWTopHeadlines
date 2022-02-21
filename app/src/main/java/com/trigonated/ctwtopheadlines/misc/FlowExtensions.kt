package com.trigonated.ctwtopheadlines.misc

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Convenience method for (example):
 * ```kotlin
 * viewLifecycleOwner.launch {
 *  viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
 *      flow.collect {
 *          // Do something
 *      }
 *  }
 * }
 * ```
 */
inline fun <T> Flow<T>.collectIn(
    owner: LifecycleOwner,
    state: Lifecycle.State = Lifecycle.State.STARTED,
    context: CoroutineContext = EmptyCoroutineContext,
    crossinline action: suspend CoroutineScope.(T) -> Unit
) = owner.lifecycleScope.launch(context) {
    owner.repeatOnLifecycle(state) {
        collect { action(it) }
    }
}
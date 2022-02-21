package com.trigonated.ctwtopheadlines.misc

import kotlinx.coroutines.channels.Channel

/** Convenience method for calling `send(Unit)` **/
suspend fun Channel<Unit>.send() = send(Unit)
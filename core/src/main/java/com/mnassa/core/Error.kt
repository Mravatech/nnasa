package com.mnassa.core

import com.mnassa.core.live.EventLive

val errorMessagesLive: EventLive<String> = EventLive()

/**
 * A function that handles the exception and possibly
 * ignores it or sends an error message.
 */
lateinit var errorHandler: (
    e: Throwable,
    onMessage: (String) -> Unit
) -> Unit

package com.mnassa.domain.extensions

import com.mnassa.core.errorHandler
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

private const val REPEAT_PERIOD_MS = 10000L

fun CoroutineContext.toCoroutineScope() = object : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = this@toCoroutineScope
}

fun CoroutineScope.launchRepeatOnError(
    onError: (Exception) -> Unit = { errorHandler(it) {} },
    block: suspend CoroutineScope.() -> Unit
) {
    launch {
        while (isActive) {
            try {
                coroutineScope {
                    block()
                }
            } catch (e: Exception) {
                onError(e)
            }

            delay(REPEAT_PERIOD_MS)
        }
    }
}

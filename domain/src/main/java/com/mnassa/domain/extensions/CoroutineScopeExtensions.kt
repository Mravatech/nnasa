package com.mnassa.domain.extensions

import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

fun CoroutineContext.toCoroutineScope() = object : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = this@toCoroutineScope
}

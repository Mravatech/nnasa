package com.mnassa.core.addons

import com.mnassa.core.errorHandler
import com.mnassa.core.errorMessagesLive
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * A coroutine scope.
 *
 * @author Artem Chepurnoy
 */
interface SubscriptionContainer : CoroutineScope {
    val coroutineScope: CoroutineScope

    fun openSubscriptionsScope()
    fun closeSubscriptionsScope()
}

/**
 * Simple [SubscriptionContainer] implementation
 *
 * @author Artem Chepurnoy
 */
open class SubscriptionsContainerDelegate(
    private val dispatcher: CoroutineContext = Dispatchers.Main,
    private val jobFactory: () -> Job = { Job() }
) : SubscriptionContainer, CoroutineScope {
    private lateinit var job: Job

    override val coroutineContext: CoroutineContext
        get() = dispatcher + job

    override val coroutineScope: CoroutineScope
        get() = this

    override fun openSubscriptionsScope() {
        job = jobFactory()
    }

    override fun closeSubscriptionsScope() {
        job.cancel()
    }
}

// Worker

val COROUTINE_WORKER_DISPATCHER = Dispatchers.Default

val COROUTINE_WORKER_CONTEXT = COROUTINE_WORKER_DISPATCHER

val COROUTINE_WORKER_CONTEXT_HANDLE_EXCEPTIONS =
    COROUTINE_WORKER_DISPATCHER + CoroutineExceptionHandler { coroutineContext, throwable ->
        errorHandler(throwable) {
            errorMessagesLive.push(it)
        }
    }

val COROUTINE_WORKER_CONTEXT_NO_EXCEPTIONS =
    COROUTINE_WORKER_DISPATCHER + CoroutineExceptionHandler { coroutineContext, throwable ->
        errorHandler(throwable) {
            // ignore error messages
        }
    }

fun CoroutineScope.launchWorker(
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
) = launch(context = COROUTINE_WORKER_CONTEXT, start = start, block = block)

fun CoroutineScope.launchWorkerNoExceptions(
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
) = launch(context = COROUTINE_WORKER_CONTEXT_NO_EXCEPTIONS, start = start, block = block)

fun CoroutineScope.launchWorkerHandleExceptions(
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
) = launch(context = COROUTINE_WORKER_CONTEXT_HANDLE_EXCEPTIONS, start = start, block = block)

fun <Result> CoroutineScope.asyncWorker(
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Result
) = async(context = COROUTINE_WORKER_CONTEXT, start = start, block = block)

// Main

val COROUTINE_MAIN_DISPATCHER = Dispatchers.Main

val COROUTINE_MAIN_CONTEXT = COROUTINE_MAIN_DISPATCHER

fun CoroutineScope.launchCoroutineUI(
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
) = launchUI(start = start, block = block)

fun CoroutineScope.launchUI(
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
) = launch(context = COROUTINE_MAIN_CONTEXT, start = start, block = block)

fun <Result> CoroutineScope.asyncUI(
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Result
) = async(context = COROUTINE_MAIN_CONTEXT, start = start, block = block)

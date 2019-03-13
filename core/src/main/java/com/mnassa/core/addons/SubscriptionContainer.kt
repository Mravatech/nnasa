package com.mnassa.core.addons

import kotlinx.coroutines.*
import timber.log.Timber
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
    private val jobFactory: () -> Job = { Job() }
) : SubscriptionContainer, CoroutineScope {
    private lateinit var job: Job

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override val coroutineScope: CoroutineScope
        get() = this

    override fun openSubscriptionsScope() {
        job = jobFactory()
    }

    override fun closeSubscriptionsScope() {
        job.cancel()
    }
}

private fun createCoroutineExceptionHandler(tag: String) =
    CoroutineExceptionHandler { context, exception ->
        if (exception !is CancellationException) {
            Timber.e(exception, "Unhandled exception in the $tag context; $context")
        }
    }

// Worker

val COROUTINE_WORKER_DISPATCHER = Dispatchers.Default

val COROUTINE_WORKER_CONTEXT = COROUTINE_WORKER_DISPATCHER +
        createCoroutineExceptionHandler("Worker")

fun CoroutineScope.launchWorker(
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
) = launch(context = COROUTINE_WORKER_CONTEXT, start = start, block = block)

fun <Result> CoroutineScope.asyncWorker(
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Result
) = async(context = COROUTINE_WORKER_CONTEXT, start = start, block = block)

// Main

val COROUTINE_MAIN_DISPATCHER = Dispatchers.Main

val COROUTINE_MAIN_CONTEXT = COROUTINE_MAIN_DISPATCHER +
        createCoroutineExceptionHandler("Main")

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

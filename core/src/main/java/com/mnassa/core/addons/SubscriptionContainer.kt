package com.mnassa.core.addons

import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by Peter on 2/20/2018.
 */
/**
 * Container for subscriptions.
 * Should be cleared in [com.androidkotlincore.mvp.MVPPresenter.onDestroyed] or in similar methods
 * */
interface SubscriptionContainer {
    /**
     * Adds job to container
     * @param job - coroutine job; [Job]
     * @return itself
     * */
    fun addJob(job: Job): Job

    /**
     * Removes job from container
     * @param job - coroutine job; [Job]
     * @return itself
     * */
    fun removeJob(job: Job): Job

    /**
     * Clears job container
     * */
    fun cancelAllSubscriptions()

    /**
     * Overloads plus operator to add job into container
     * @param job - [Job]
     * */
    operator fun plusAssign(job: Job)

    /**
     * Overloads minus operator to remove job from container
     * @param job - [Job]
     * */
    operator fun minusAssign(job: Job)
}

/**
 * Simple [SubscriptionContainer] implementation
 */
open class SubscriptionsContainerDelegate : SubscriptionContainer {
    private val jobs = ArrayList<Job>()

    override fun addJob(job: Job): Job = job.also { jobs.add(it) }

    override fun removeJob(job: Job): Job = job.also { jobs.remove(it) }

    override fun cancelAllSubscriptions() {
        jobs.forEach { it.cancel() }
        jobs.clear()
    }

    override fun plusAssign(job: Job) {
        addJob(job)
    }

    override fun minusAssign(job: Job) {
        removeJob(job)
    }
}

////////////////////////////////////////////////////////////////////////////////////////////////////
private val WORKER_POOL = CommonPool
private val UI_POOL = UI

/**
 * Extension to add job into container
 * @param subscriptionContainer - [SubscriptionContainer]
 * @return [Job]
 * */
fun Job.bind(subscriptionContainer: SubscriptionContainer): Job {
    subscriptionContainer += this
    return this
}

/**
 * Creates coroutine, which will be automatically added to the subscription container
 */
fun SubscriptionContainer.launchCoroutineWorker(start: CoroutineStart = CoroutineStart.DEFAULT,
                                                                       block: suspend CoroutineScope.() -> Unit): Job =
        launch(context = WORKER_POOL, start = start, block = block).bind(this)

/**
 * Creates coroutine, which will be automatically added to the subscription container
 */
fun SubscriptionContainer.launchCoroutineUI(start: CoroutineStart = CoroutineStart.DEFAULT,
                                                                   block: suspend CoroutineScope.() -> Unit): Job =
        launch(context = UI_POOL, start = start, block = block).bind(this)

fun <T> SubscriptionContainer.asyncWorker(start: CoroutineStart = CoroutineStart.DEFAULT,
                                                                 block: suspend CoroutineScope.() -> T): Deferred<T> {
    val result = async(context = WORKER_POOL, start = start, block = block)
    result.bind(this)
    return result
}

fun <T> SubscriptionContainer.asyncUI(start: CoroutineStart = CoroutineStart.DEFAULT,
                                                             block: suspend CoroutineScope.() -> T): Deferred<T> {
    val result = async(context = UI_POOL, start = start, block = block)
    result.bind(this)
    return result
}

suspend fun <T, R> ReceiveChannel<T>.consumeTo(sendChannel: SendChannel<R>, mapper: (T) -> R = { it as R }) = consumeEach { sendChannel.send(mapper(it)) }
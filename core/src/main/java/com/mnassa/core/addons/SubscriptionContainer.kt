package com.mnassa.core.addons

import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI
import java.util.concurrent.ConcurrentLinkedQueue

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
    private val jobs = ConcurrentLinkedQueue<Job>()

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

fun <CancellableContext> CancellableContext.launchCoroutineWorker(
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.(ref: Ref<CancellableContext>) -> Unit): Job where CancellableContext : SubscriptionContainer {
    val thisReference = this.asReference()
    val coroutineBlockWrapper: suspend CoroutineScope.() -> Unit = { block(this, thisReference) }
    return launch(context = WORKER_POOL, start = start, block = coroutineBlockWrapper).bind(this)
}

/**
 * Creates coroutine, which will be automatically added to the subscription container
 */
fun <CancellableContext> CancellableContext.launchCoroutineUI(
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.(ref: Ref<CancellableContext>) -> Unit): Job where CancellableContext : SubscriptionContainer {
    val thisReference = this.asReference()
    val coroutineBlockWrapper: suspend CoroutineScope.() -> Unit = { block(this, thisReference) }
    return launch(context = UI_POOL, start = start, block = coroutineBlockWrapper).bind(this)
}

fun <Result, CancellableContext> CancellableContext.asyncWorker(
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.(ref: Ref<CancellableContext>) -> Result): Deferred<Result> where CancellableContext : SubscriptionContainer {
    val thisReference = this.asReference()
    val coroutineBlockWrapper: suspend CoroutineScope.() -> Result = { block(this, thisReference) }

    val result = async(context = WORKER_POOL, start = start, block = coroutineBlockWrapper)
    result.bind(this)
    return result
}

fun <Result, CancellableContext> CancellableContext.asyncUI(
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.(ref: Ref<CancellableContext>) -> Result): Deferred<Result> where CancellableContext : SubscriptionContainer {
    val thisReference = this.asReference()
    val coroutineBlockWrapper: suspend CoroutineScope.() -> Result = { block(this, thisReference) }

    val result = async(context = UI_POOL, start = start, block = coroutineBlockWrapper)
    result.bind(this)
    return result
}

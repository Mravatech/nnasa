package com.mnassa.extensions

/**
 * Created by Peter on 3/15/2018.
 */
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import kotlinx.coroutines.experimental.suspendCancellableCoroutine
import java.util.*
import kotlin.math.abs

val RecyclerView.lastVisibleItemPosition: Int
    get() {
        val lManager = layoutManager
        return when (lManager) {
            null -> throw IllegalStateException("Layout manager of recyclerView $this is null!")
            is LinearLayoutManager -> lManager.findLastVisibleItemPosition()
            is StaggeredGridLayoutManager -> {
                val into = lManager.findLastVisibleItemPositions(null)
                val intoList = into.toList()
                return Collections.max(intoList)
            }
            else -> throw IllegalStateException("Unsupported layout manager of recyclerView $this")
        }
    }

val RecyclerView.firstVisibleItemPosition: Int
    get() {
        val lManager = layoutManager
        return when (lManager) {
            null -> throw IllegalStateException("Layout manager of recyclerView $this is null!")
            is LinearLayoutManager -> lManager.findFirstVisibleItemPosition()
            is StaggeredGridLayoutManager -> {
                val into = lManager.findFirstVisibleItemPositions(null)
                Collections.min(into.toList())
            }

            else -> throw IllegalStateException("Unsupported layout manager of recyclerView $this")
        }
    }

val RecyclerView.isReverseLayout: Boolean
    get() {
        val lManager = layoutManager
        return when (lManager) {
            null -> throw IllegalStateException("Layout manager of recyclerView $this is null!")
            is LinearLayoutManager -> lManager.stackFromEnd
            is StaggeredGridLayoutManager -> false

            else -> throw IllegalStateException("Unsupported layout manager of recyclerView $this")
        }
    }

val RecyclerView.itemsOnScreen: Int
    get() {
        return abs(lastVisibleItemPosition - firstVisibleItemPosition)
    }

//TODO: how to load new items?
//TODO: how to update loaded items?

/**
 * This coefficient determines when PaginationTool must start loading of new items
 * 0.1 - start loading at the start of scrolling
 * 0.9 - start loading at the end of scrolling
 * Must be < 1 and > 0
 */

suspend fun RecyclerView.waitForNewItems(
        emptyListCount: Int = 0,
        startPaginationCoefficient: Float = 0.7f,
        itemsCount: () -> Int = { adapter.itemCount }
) {
    suspendCancellableCoroutine<Unit> { continuation ->

        if (isNewItemsNeeded(emptyListCount, startPaginationCoefficient, itemsCount)) {
            continuation.resume(Unit)
            return@suspendCancellableCoroutine
        }

        val onScrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (isNewItemsNeeded(emptyListCount, startPaginationCoefficient, itemsCount)) {
                    continuation.resume(Unit)
                }
            }
        }
        addOnScrollListener(onScrollListener)

        continuation.invokeOnCompletion {
            removeOnScrollListener(onScrollListener)
        }
    }
}

fun RecyclerView.isNewItemsNeeded(
        emptyListCount: Int = 0,
        startPaginationCoefficient: Float = 0.7f,
        itemsCount: () -> Int = { adapter.itemCount }
): Boolean {
    if (emptyListCount == itemsCount()) {
        return true
    }

    return if (isReverseLayout) {
        val position = firstVisibleItemPosition
        val itemsOnScreen = lastVisibleItemPosition - position
        val updatePosition = (itemsOnScreen * startPaginationCoefficient).toInt()
        position <= updatePosition
    } else {
        val position = lastVisibleItemPosition
        val limit = minOf((itemsOnScreen + 1) * 2, 10)
        val updatePosition = (itemsCount().toFloat() - 1f - limit.toFloat() * startPaginationCoefficient).toInt()
        position >= updatePosition
    }
}


/*
/**
 * Usage example:
 * Disposable d = PaginationTool.builder<List<Item>>(recyclerView) {
        beforeLoadingListener = { _ -> showLoadingProgress() }
        limit = LIMIT
        emptyListCount = localEmptyListCount
        pagingListener = { offset -> getItemsObservable(offset) }
    }.subscribe {
        hideLoadingProgress()
        feedList.addAll(it)
    }
    //when view detached:
    d.dispose()
 */

/**
 * Note! Pagination will be stopped when the query does not change the RecyclerViewAdapter.itemCount or itemsCountProvider
 */
class PaginationTool<T> private constructor(val builder: Builder<T>) {
    private val wrapper = InjectionWrapper()

    val pagingObservable: Observable<T>
        get() {
            return getScrollObservable()
                    .subscribeOn(builder.mainThreadScheduler)
                    .distinctUntilChanged(builder.keySelector)
                    .doOnNext { builder.beforeLoadingListener?.invoke(it) }
                    .observeOn(builder.networkThreadScheduler)
                    .switchMap { offset ->
                        getPagingObservable(
                                observable = builder.pagingListener.invoke(offset),
                                numberOfAttemptToRetry = 0,
                                offset = offset)
                    }
        }

    private fun getScrollObservable(): Observable<Int> {
        val itemsCountProvider = builder.customItemsCountProvider!!
        val startPaginationCoefficient = builder.startPaginationCoefficient

        val listener = { emitter: ObservableEmitter<Int> ->
            object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val itemsCount = itemsCountProvider.invoke()
                    when {
                        recyclerView.isReverseLayout() -> {
                            val position = recyclerView.getFirstVisibleItemPosition()
                            val itemsOnScreen = recyclerView.getLastVisibleItemPosition() - position
                            val updatePosition = (itemsOnScreen * startPaginationCoefficient).toInt()
                            if (position <= updatePosition) {
                                var offset = itemsCount
                                offset -= if (builder.emptyListCountPlusToOffset) 0 else builder.emptyListCount
                                emitter.onNext(offset)
                            }
                        }
                        else -> {
                            val position = recyclerView.getLastVisibleItemPosition()
                            val updatePosition = (itemsCount.toFloat() - 1f - builder.limit.toFloat() * startPaginationCoefficient).toInt()
                            if (position >= updatePosition) {
                                var offset = itemsCount
                                offset -= if (builder.emptyListCountPlusToOffset) 0 else builder.emptyListCount
                                emitter.onNext(offset)
                            }
                        }
                    }
                }
            }
        }

        return Observable.create { emitter ->
            builder.recyclerView.post {
                val localListener = listener(emitter)
                builder.recyclerView.addOnScrollListener(localListener)
                emitter.setCancellable {
                    builder.recyclerView.removeOnScrollListener(localListener)
                }

                val itemsCount = itemsCountProvider.invoke()
                if (itemsCount == builder.emptyListCount) {
                    var offset = itemsCount
                    offset -= if (builder.emptyListCountPlusToOffset) 0 else builder.emptyListCount
                    emitter.onNext(offset)
                }
            }
        }
    }

    private fun getPagingObservable(
            observable: Observable<T>,
            numberOfAttemptToRetry: Int,
            offset: Int): Observable<T> {

        val listener = builder.pagingListener
        val retryCount = builder.retryCount

        return observable.onErrorResumeNextKt { throwable ->
            L.exception(throwable)
            when {
                ExceptionType.isNoInternet(throwable) -> {
                    //wait for internet connection recovery
                    wrapper.bus.register(OnNetworkConnected::class.java).flatMap { _ -> observable }
                }
                numberOfAttemptToRetry < retryCount -> {
                    // retry to load new data portion if error occurred
                    val attemptToRetryInc = numberOfAttemptToRetry + 1
                    getPagingObservable(listener.invoke(offset), attemptToRetryInc, offset)
                }
                else -> Observable.error(throwable)
            }
        }
    }


    class Builder<T> internal constructor(val recyclerView: RecyclerView, private val initializer: Builder<T>.() -> Unit) {
        /**
         * You can specify the limit of data loading
         * Must be > 0
         */
        var limit = DEFAULT_LIMIT
            set(value) {
                if (value <= 0) {
                    throw IllegalArgumentException("Limit must be greater than 0. Cannot set $value")
                }
                field = value
            }

        /**
         * If your recyclerViewAdapter has headers/footers or other items, which not included to the
         * real dataSource, you need to set this items count here
         * Must be >= 0
         */
        var emptyListCount = DEFAULT_EMPTY_LIST_ITEMS_COUNT
            set(value) {
                if (value < 0) {
                    throw IllegalArgumentException("Empty list count must be >= 0. Cannot set $value")
                }
                field = value
            }

        /**
         * Determines how many attempts to load data the PaginationTool must take before returning an error
         * Must be >= 0
         */
        var retryCount = DEFAULT_MAX_ATTEMPTS_TO_RETRY_LOADING
            set(value) {
                if (value < 0) {
                    throw IllegalArgumentException("Retry count must be >= 0. Cannot set $value")
                }
                field = value
            }

        /**
         * This coefficient determines when PaginationTool must start loading of new items
         * 0.1 - start loading at the start of scrolling
         * 0.9 - start loading at the end of scrolling
         * Must be < 1 and > 0
         */
        var startPaginationCoefficient: Float = DEFAULT_START_PAGINATION_COEFFICIENT
            set(value) {
                if (value >= 1f || value <= 0f) {
                    throw IllegalArgumentException("StartPaginationCoefficient must be >0 and <1")
                }
                field = value
            }

        /**
         * Set true, if you want to calculate offset as offset' = offset + emptyListCount
         */
        var emptyListCountPlusToOffset = false

        /**
         * Optional.
         * Implement this callback if you want to set custom items count provider
         */
        var customItemsCountProvider: (() -> Int)? = null

        /**
         * Optional listener.
         * This callback will be invoked before data loading on MAIN thread
         */
        var beforeLoadingListener: ((offset: Int) -> Unit)? = null

        /**
         * Key selector. A function that projects an emitted item to a key value that is used
         * to decide whether an item is distinct from another one or not.
         * You can set custom implementation of this method to add possibility to control when
         * pagination tool must start to load data and when - not.
         *
         * Note! When pagination tool starts to load PART1, key selector must returns the same item (1)
         * until the PART1 loading stops. When loading PART1 was finished, key selector must returns
         * second item (2).
         *
         * This selector can be helpful when server returns "hasMore" flag for pagination. In this
         * case as value to return from keySelector function you can use local AtomicInteger.get() variable.
         * After each successful pagination response and when flag "hasMore" is true, you must increment
         * your local AtomicInteger variable.
         *
         * See [Observable.distinctUntilChanged]
         */
        var keySelector: ((offset: Int) -> Any?) = { it }

        /**
         * Main thread scheduler. Used for scroll observable
         */
        var mainThreadScheduler: Scheduler = MAIN_SCHEDULER

        /**
         * Network thread scheduler. Used for network requests
         */
        var networkThreadScheduler: Scheduler = WORKER_SCHEDULER

        /**
         * !!! Always required !!!
         * This listener must return observable for next items [offset; offset+limit]
         */
        lateinit var pagingListener: (offset: Int) -> Observable<T>


        init {
            recyclerView.adapter!!
            recyclerView.layoutManager!!
        }

        fun build(): PaginationTool<T> {
            initializer.invoke(this)
            if (customItemsCountProvider == null) {
                customItemsCountProvider = { recyclerView.adapter.itemCount }
            }
            return PaginationTool(this)
        }
    }

    class InjectionWrapper {
        @Inject
        lateinit var bus: IEventBusProvider

        init {
            DI.component.inject(this)
        }
    }

    companion object {
        // for first start of items loading then on RecyclerView there are not items and no scrolling
        private val DEFAULT_EMPTY_LIST_ITEMS_COUNT = 0
        // default limit for requests
        private val DEFAULT_LIMIT = 50
        // default max attempts to retry loading request
        private val DEFAULT_MAX_ATTEMPTS_TO_RETRY_LOADING = 5
        // start loading when user scrolled 1/2 items
        private val DEFAULT_START_PAGINATION_COEFFICIENT = 0.7f

        /**
         * You should set pagingListener in the initializer!!!
         * Always dispose observable when View is detached!!!
         */
        fun <T> builder(recyclerView: RecyclerView, initializer: Builder<T>.() -> Unit) = Builder(recyclerView, initializer).build().pagingObservable

        fun <T> customBuilder(recyclerView: RecyclerView, initializer: Builder<T>.() -> Unit) = Builder(recyclerView, initializer).build()
    }

}
 */
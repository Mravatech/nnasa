package com.mnassa.extensions

/**
 * Created by Peter on 3/15/2018.
 */
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
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

/**
 * This coefficient determines when PaginationTool must start loading of new items
 * 0.1 - start loading at the start of scrolling
 * 0.9 - start loading at the end of scrolling
 * Must be < 1 and > 0
 */
private const val DEFAULT_START_PAGINATION_COEFFICIENT = 0.7f

suspend fun RecyclerView.waitForNewItems(
    emptyListCount: Int = 0,
    startPaginationCoefficient: Float = DEFAULT_START_PAGINATION_COEFFICIENT,
    itemsCount: () -> Int = { adapter!!.itemCount }
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
    startPaginationCoefficient: Float = DEFAULT_START_PAGINATION_COEFFICIENT,
    itemsCount: () -> Int = { adapter!!.itemCount }
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

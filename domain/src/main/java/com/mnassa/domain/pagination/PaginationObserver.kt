package com.mnassa.domain.pagination

/**
 * @author Artem Chepurnoy
 */
interface PaginationObserver {

    /**
     * `true` if the this observer is still loading pages,
     * `false` if it's ready to load next page.
     */
    val isBusy: Boolean

    fun onSizeChanged(size: Long)

}

package com.mnassa.extensions

/**
 * Created by Peter on 3/15/2018.
 */
import android.support.v4.view.ViewCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.widget.AbsListView

fun SwipeRefreshLayout.setRefreshLock(canRefresh: () -> Boolean) {
    setOnChildScrollUpCallback { _, target ->
        when {
            !canRefresh.invoke() -> true
            android.os.Build.VERSION.SDK_INT < 14 -> when (target) {
                is AbsListView ->
                    target.childCount > 0 && (target.firstVisiblePosition > 0 || target.getChildAt(0).top < target.paddingTop)
                else -> ViewCompat.canScrollVertically(target, -1) || target!!.scrollY > 0
            }
            else -> ViewCompat.canScrollVertically(target, -1)
        }
    }
}
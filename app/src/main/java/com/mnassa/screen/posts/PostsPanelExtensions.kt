package com.mnassa.screen.posts

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.mnassa.R
import com.mnassa.extensions.firstVisibleItemPosition
import kotlin.math.abs

/**
 * Created by Peter on 7/23/2018.
 */
private const val PANEL_ANIMATION_DURATION = 500L
private const val PANEL_ANIMATION_START_POSITION = 0f
private const val PANEL_ANIMATION_START_ALPHA = 1f
private const val PANEL_ANIMATION_END_POSITION = -100f
private const val PANEL_ANIMATION_END_ALPHA = 0f
private const val VERTICAL_SCROLL_MIN_EPSOLON = 10
private const val VERTICAL_SCROLL_IDLE = 0

fun RecyclerView.attachPanel(hasNewPosts: () -> Boolean) {
    val panel = (parent as View).findViewById<View>(R.id.flNewItemsPanel)

    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        private var isShown = false
        private var isHidden = false

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (dy != VERTICAL_SCROLL_IDLE && abs(dy) < VERTICAL_SCROLL_MIN_EPSOLON) return

            //to hide panel when scrolling to bottom, add (dy > 0 &&)
            if (firstVisibleItemPosition > 1 && hasNewPosts()) {
                if (isShown) return
                showNewItemsPanel(panel)
                isShown = true
                isHidden = false
            } else {
                if (isHidden) return
                hideNewItemsPanel(panel)
                isHidden = true
                isShown = false
            }
        }
    })

    panel.animate().alpha(PANEL_ANIMATION_END_ALPHA).setDuration(0L).start()
    panel.setOnClickListener { scrollToPosition(0) }
}

private fun showNewItemsPanel(panel: View) {
    panel.animate()
            .setDuration(PANEL_ANIMATION_DURATION)
            .translationY(PANEL_ANIMATION_START_POSITION)
            .alpha(PANEL_ANIMATION_START_ALPHA)
            .start()
}

private fun hideNewItemsPanel(panel: View) {
    panel.animate()
            .setDuration(PANEL_ANIMATION_DURATION)
            .translationY(PANEL_ANIMATION_END_POSITION)
            .alpha(PANEL_ANIMATION_END_ALPHA)
            .start()
}


package com.mnassa.screen.posts

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.mnassa.R
import com.mnassa.core.addons.launchUI
import com.mnassa.domain.extensions.toCoroutineScope
import com.mnassa.extensions.firstVisibleItemPosition
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlin.coroutines.coroutineContext
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
private const val VERTICAL_THRESHOLD = 100

suspend fun RecyclerView.attachPanel(
    shouldBeShown: ReceiveChannel<Boolean>,
    onClick: (View) -> Unit
) {
    var y = 0
    var panelIsShownY = 0

    var panelShouldBeShown = false
    var panelIsShown = false
    val panel = (parent as View).findViewById<View>(R.id.flNewItemsPanel)

    fun updatePanelIsShown(shown: Boolean) {
        if (panelIsShown != shown) {
            panelIsShown = shown

            if (shown) {
                showNewItemsPanel(panel)
            } else {
                hideNewItemsPanel(panel)
            }
        }
    }

    // Observe the `Should be changed` emitter and
    // handle the events.
    coroutineContext.toCoroutineScope().launchUI {
        shouldBeShown.consumeEach {
            updatePanelIsShown(it)
            panelShouldBeShown = it
            panelIsShownY = y
        }
    }

    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (dy == VERTICAL_SCROLL_IDLE) {
                return
            }

            y += dy

            if (firstVisibleItemPosition == 0 && panelShouldBeShown) {
                updatePanelIsShown(true)
            } else {
                if (abs(y - panelIsShownY) > VERTICAL_THRESHOLD) {
                    if (y > panelIsShownY) {
                        updatePanelIsShown(false)
                    } else if (panelShouldBeShown) {
                        updatePanelIsShown(true)
                    }

                    panelIsShownY = y
                }
            }
        }
    })

    panel.visibility = View.VISIBLE
    panel.animate().alpha(PANEL_ANIMATION_END_ALPHA).setDuration(0L).start()
    panel.setOnClickListener { onClick(it) }
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


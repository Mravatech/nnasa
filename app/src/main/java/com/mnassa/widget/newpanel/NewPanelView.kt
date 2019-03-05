package com.mnassa.widget.newpanel

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mnassa.R
import com.mnassa.core.addons.launchUI
import com.mnassa.domain.extensions.toCoroutineScope
import com.mnassa.screen.posts.attachPanel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.map
import kotlin.coroutines.coroutineContext

/**
 * @author Artem Chepurnoy
 */
interface NewPanelView {
    suspend fun RecyclerView.setupNewPanel(viewModel: NewPanelViewModel) {
        val panelTextView = (parent as View).findViewById<TextView>(R.id.tvNewItemsAvailable)!!
        val scope = coroutineContext.toCoroutineScope()
        scope.launchUI {
            attachPanel(
                shouldBeShown = viewModel.newItemsCounterChannel.openSubscription().map { it > 0 },
                onClick = {
                    scope.launchUI {
                        viewModel.onNewPanelClick()
                    }
                }
            )
        }
        scope.launchUI {
            viewModel.newItemsCounterChannel.consumeEach {
                if (it > 0) panelTextView.text = formatNewPanelLabel(it)
            }
        }
        scope.launchUI {
            viewModel.scrollToTopChannel.consumeEach {
                scrollToPosition(0)
            }
        }
    }

    fun formatNewPanelLabel(counter: Int): String?
}

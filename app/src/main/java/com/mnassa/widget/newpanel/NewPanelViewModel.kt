package com.mnassa.widget.newpanel

import com.mnassa.core.addons.launchUI
import com.mnassa.domain.extensions.toCoroutineScope
import com.mnassa.exceptions.resolveExceptions
import kotlinx.coroutines.channels.BroadcastChannel
import java.util.*
import kotlin.coroutines.coroutineContext

/**
 * @author Artem Chepurnoy
 */
interface NewPanelViewModel {
    val scrollToTopChannel: BroadcastChannel<Unit>
    val newItemsCounterChannel: BroadcastChannel<Int>

    suspend fun onNewPanelClick() {
        val scope = coroutineContext.toCoroutineScope()
        scope.resolveExceptions {
            launchUI {
                val date = Calendar.getInstance().time
                setNewItemsTimeUpperBound(date)

                // Ask the list to scroll itself to
                // the top.
                scrollToTopChannel.send(Unit)
            }
        }
    }

    fun setNewItemsTimeUpperBound(date: Date)
}

package com.mnassa.screen.notifications

import android.os.Bundle
import android.view.View
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.NotificationModel
import com.mnassa.extensions.isInvisible
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.main.OnPageSelected
import com.mnassa.screen.main.OnScrollToTop
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_notifications.view.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import org.kodein.di.generic.instance

/**
 * Created by Peter on 3/6/2018.
 */
class NotificationsController : MnassaControllerImpl<NotificationsViewModel>(), OnPageSelected, OnScrollToTop {
    override val layoutId: Int = R.layout.controller_notifications
    override val viewModel: NotificationsViewModel by instance()

    private val adapter = NotificationAdapter()

    override fun onCreated(savedInstanceState: Bundle?) {
        super.onCreated(savedInstanceState)
        savedInstanceState?.apply {
            adapter.restoreState(this)
        }
        adapter.isLoadingEnabled = savedInstanceState == null
        adapter.onDataChangedListener = { itemsCount ->
            view?.llEmptyNotifications?.isInvisible = itemsCount > 0 || adapter.isLoadingEnabled
        }
        adapter.onItemClickListener = ::onNotificationClickHandle

        launchCoroutineUI {
            subscribeToUpdates(viewModel.oldNotificationChannel.openSubscription())
        }
        launchCoroutineUI {
            subscribeToUpdates(viewModel.newNotificationChannel.openSubscription())
        }
        launchCoroutineUI {
            viewModel.openController.consumeEach(::open)
        }
    }

    private suspend fun subscribeToUpdates(channel: ReceiveChannel<ListItemEvent<List<NotificationModel>>>) {
        channel.consumeEach {
            when (it) {
                is ListItemEvent.Added -> {
                    adapter.isLoadingEnabled = false
                    adapter.addNotifications(it.item)
                }
                is ListItemEvent.Changed -> adapter.addNotifications(it.item)
                is ListItemEvent.Moved -> adapter.addNotifications(it.item)
                is ListItemEvent.Removed -> {
                    adapter
                        .dataStorage
                        .find { model -> model.key == it.key }
                        ?.let { model -> model as? NotificationAdapter.NotificationItem.ContentItem }
                        ?.let { model -> adapter.removeNotifications(listOf(model.content)) }
                }
                is ListItemEvent.Cleared -> {
                    adapter.isLoadingEnabled = true
                    adapter.dataStorage.clear()
                }
            }
        }
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        with(view) {
            tvEmptyNotifications.text = fromDictionary(R.string.notifications_no_notifications)
            rvNotifications.adapter = adapter
        }
    }

    override fun onDestroyView(view: View) {
        view.rvNotifications.adapter = null
        super.onDestroyView(view)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        adapter.saveState(outState)
    }

    override fun onPageSelected() {
        viewModel.resetCounter()
    }

    override fun scrollToTop() {
        view?.rvNotifications?.scrollToPosition(0)
    }

    private fun onNotificationClickHandle(item: NotificationModel) {
        viewModel.openNotification(item)
    }

    companion object {
        fun newInstance() = NotificationsController()
    }
}

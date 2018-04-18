package com.mnassa.screen.notifications

import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.NotificationModel
import com.mnassa.domain.model.bufferize
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.posts.PostDetailsFactory
import com.mnassa.screen.profile.ProfileController
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_notifications.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import org.kodein.di.generic.instance

/**
 * Created by Peter on 3/6/2018.
 */
class NotificationsController : MnassaControllerImpl<NotificationsViewModel>() {
    override val layoutId: Int = R.layout.controller_notifications
    override val viewModel: NotificationsViewModel by instance()

    private val adapter = NotificationAdapter()
    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        with(view) {
            tvEmptyNotifications.text = fromDictionary(R.string.notifications_no_notifications)
            rvNotifications.layoutManager = LinearLayoutManager(view.context)
            rvNotifications.adapter = adapter
        }
        viewModel.retrieveNotifications()
        adapter.onItemClickListener = { onNotificationClickHandle(it) }
        launchCoroutineUI {
            viewModel.notificationChannel.openSubscription().bufferize(this@NotificationsController).consumeEach {
                when (it) {
                    is ListItemEvent.Added -> {
                        adapter.isLoadingEnabled = false
                        adapter.dataStorage.addAll(it.item)
                    }
                    is ListItemEvent.Changed -> adapter.dataStorage.addAll(it.item)
                    is ListItemEvent.Moved -> adapter.dataStorage.addAll(it.item)
                    is ListItemEvent.Removed -> adapter.dataStorage.removeAll(it.item)
                    is ListItemEvent.Cleared -> {
                        adapter.dataStorage.clear()
                        adapter.isLoadingEnabled = true
                    }
                }
                if (view.llEmptyNotifications.visibility == View.VISIBLE) {
                    view.llEmptyNotifications.visibility = View.GONE
                }
            }
        }
    }

    private fun onNotificationClickHandle(item: NotificationModel) {
        if (!item.isOld) {
            viewModel.notificationView(item.id)
        }

        when (item.type) {
            postComment, generalPostByAdmin, newEventByAdmin, eventCancelling, userWasRecommendedInEvent -> {
                val postDetailsFactory: PostDetailsFactory by instance()
                open(postDetailsFactory.newInstance(requireNotNull(item.extra.post)))
            }
            iWasRecommended, iWasRecommendedInEvent, userWasRecommended, autoSuggestYouCanHelp,
            newUserJoined, postRepost, connectionRequest, connectionsRequestAccepted,
            userWasRecommendedToYou, userWasRecommendedByPost, newEventAttendee, privateChatMessage -> {
                val id = item.extra.author?.id ?: item.extra.recommended?.id
                open(ProfileController.newInstance(requireNotNull(id)))
            }
        }

    }

    companion object {
        fun newInstance() = NotificationsController()
    }
}

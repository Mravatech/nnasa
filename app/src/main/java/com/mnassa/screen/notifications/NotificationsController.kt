package com.mnassa.screen.notifications

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.di.getInstance
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.NotificationModel
import com.mnassa.domain.model.bufferize
import com.mnassa.extensions.isInvisible
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.events.details.EventDetailsController
import com.mnassa.screen.group.list.GroupListController
import com.mnassa.screen.invite.InviteController
import com.mnassa.screen.invite.InviteSource
import com.mnassa.screen.invite.InviteSourceHolder
import com.mnassa.screen.main.OnPageSelected
import com.mnassa.screen.main.OnScrollToTop
import com.mnassa.screen.notifications.viewholder.*
import com.mnassa.screen.posts.PostDetailsFactory
import com.mnassa.screen.profile.ProfileController
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_notifications.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
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
        adapter.isLoadingEnabled = true
//        controllerSubscriptionContainer.launchCoroutineUI {
//            val view = getViewSuspend()
//            viewModel.notificationChannel.openSubscription().bufferize(controllerSubscriptionContainer).consumeEach {
//                when (it) {
//                    is ListItemEvent.Added -> {
//                        adapter.isLoadingEnabled = false
//                        if (it.item.isNotEmpty()) {
//                            adapter.addNotifications(it.item)
//                        }
//                        view.llEmptyNotifications.isInvisible = it.item.isNotEmpty() || !adapter.dataStorage.isEmpty()
//                    }
//                    is ListItemEvent.Changed -> adapter.addNotifications(it.item)
//                    is ListItemEvent.Moved -> adapter.addNotifications(it.item)
//                    is ListItemEvent.Removed -> adapter.removeNotifications(it.item)
//                    is ListItemEvent.Cleared -> {
//                        adapter.dataStorage.clear()
//                        adapter.isLoadingEnabled = true
//                        view.llEmptyNotifications.isInvisible = true
//                    }
//                }
//            }
//        }
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        with(view) {
            tvEmptyNotifications.text = fromDictionary(R.string.notifications_no_notifications)
            rvNotifications.layoutManager = LinearLayoutManager(view.context)
            rvNotifications.adapter = adapter
        }
        adapter.onItemClickListener = { onNotificationClickHandle(it) }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        adapter.saveState(outState)
    }

    override fun onPageSelected() {
        //do nothing here
    }

    override fun scrollToTop() {
        view?.rvNotifications?.scrollToPosition(0)
    }

    private fun onNotificationClickHandle(item: NotificationModel) {
        if (!item.isOld) {
            viewModel.notificationView(item.id)
        }

        when (item.type) {
            POST_COMMENT,
            POST_IS_EXPIRED,
            POST_PROMOTED,
            USER_WAS_RECOMMENDED_BY_POST,
            USER_WAS_RECOMMENDED,
            GENERAL_POST_BY_ADMIN,
            I_WAS_RECOMMENDED,
            AUTO_SUGGEST_YOU_CAN_HELP,
            ONE_DAY_TO_EXPIRATION_OF_POST -> {
                val postDetailsFactory: PostDetailsFactory by instance()
                open(postDetailsFactory.newInstance(requireNotNull(item.extra.post)))
            }
            NEW_USER_JOINED,
            POST_REPOST,
            CONNECTION_REQUEST,
            CONNECTIONS_REQUEST_ACCEPTED,
            USER_WAS_RECOMMENDED_TO_YOU,
            PRIVATE_CHAT_MESSAGE,
            RESPONSE_CHAT_MESSAGE -> {
                val account = item.extra.recommended ?: item.extra.reffered ?: item.extra.author
                open(ProfileController.newInstance(requireNotNull(account)))
            }
            I_WAS_RECOMMENDED_IN_EVENT,
            USER_WAS_RECOMMENDED_IN_EVENT,
            NEW_EVENT_BY_ADMIN,
            NEW_EVENT_ATTENDEE,
            EVENT_CANCELLING -> {
                open(EventDetailsController.newInstance(requireNotNull(item.extra.event)))
            }
            INVITES_NUMBER_CHANGED -> {
                requireNotNull(applicationContext).getInstance<InviteSourceHolder>().source = InviteSource.Notification()
                open(InviteController.newInstance())
            }
            INVITED_TO_GROUP -> {
                open(GroupListController.newInstance())
            }
            else -> {
                when {
                    item.type.contains(POST) -> {
                        val postDetailsFactory: PostDetailsFactory by instance()
                        open(postDetailsFactory.newInstance(requireNotNull(item.extra.post)))
                    }
                    item.type.contains(EVENT) -> open(EventDetailsController.newInstance(requireNotNull(item.extra.event)))
                    else -> {
                        val account = item.extra.recommended ?: item.extra.reffered
                        ?: item.extra.author
                        open(ProfileController.newInstance(requireNotNull(account)))
                    }
                }

            }
        }
    }

    companion object {
        const val POST = "post"
        const val EVENT = "event"
        fun newInstance() = NotificationsController()
    }
}

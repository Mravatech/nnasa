package com.mnassa.screen.group.profile.events

import android.os.Bundle
import com.mnassa.domain.interactor.EventsInteractor
import com.mnassa.domain.interactor.GroupsInteractor
import com.mnassa.domain.model.EventModel
import com.mnassa.domain.model.GroupModel
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.withBuffer
import com.mnassa.exceptions.resolveExceptions
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.*

/**
 * Created by Peter on 09.08.2018.
 */
class GroupEventsViewModelImpl(private val groupId: String,
                               private val eventsInteractor: EventsInteractor,
                               private val groupsInteractor: GroupsInteractor) : MnassaViewModelImpl(), GroupEventsViewModel {
    override val groupChannel: BroadcastChannel<GroupModel> = ConflatedBroadcastChannel()
    override val newsFeedChannel: ReceiveChannel<ListItemEvent<List<EventModel>>>
        get() = produce {
            handleExceptionsSuspend { send(ListItemEvent.Added(eventsInteractor.loadAllByGroupIdImmediately(groupId))) }
            eventsInteractor.loadAllByGroupId(groupId).withBuffer().consumeEach { send(it) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        resolveExceptions {
            groupsInteractor.getGroup(groupId).consumeEach {
                if (it != null) groupChannel.send(it)
            }
        }
    }

    override fun onAttachedToWindow(event: EventModel) {
        GlobalScope.resolveExceptions(showErrorMessage = false) {
            eventsInteractor.onItemViewed(event)
        }
    }
}
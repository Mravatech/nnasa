package com.mnassa.screen.group.profile.events

import com.mnassa.domain.model.EventModel
import com.mnassa.domain.model.GroupModel
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ReceiveChannel

/**
 * Created by Peter on 09.08.2018.
 */
interface GroupEventsViewModel : MnassaViewModel {
    val groupChannel: BroadcastChannel<GroupModel>
    val newsFeedChannel: ReceiveChannel<ListItemEvent<List<EventModel>>>

    fun onAttachedToWindow(event: EventModel)
}
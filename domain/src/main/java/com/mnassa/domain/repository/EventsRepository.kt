package com.mnassa.domain.repository

import com.mnassa.domain.model.EventModel
import com.mnassa.domain.model.ListItemEvent
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by Peter on 4/13/2018.
 */
interface EventsRepository {
    suspend fun getEventsFeedChannel(): ReceiveChannel<ListItemEvent<EventModel>>
    suspend fun getEventsWallChannel(): ReceiveChannel<ListItemEvent<EventModel>>
}
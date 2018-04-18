package com.mnassa.domain.model.impl

import com.mnassa.domain.model.*
import java.util.*

/**
 * Created by Peter on 4/13/2018.
 */
data class EventModelImpl(
        override var id: String,
        override val author: ShortAccountModel,
        override val commentsCount: Int,
        override val viewsCount: Int,
        override val createdAt: Date,
        override val duration: EventDuration,
        override val startAt: Date,
        override val locationType: EventLocationType,
        override val allConnections: Boolean,
        override val itemType: ItemType,
        override val originalId: String,
        override val originalCreatedAt: Date,
        override val pictures: List<String>,
        override val price: Long,
        override val privacyType: PostPrivacyType,
        override val status: EventStatus,
        override val tags: List<String>,
        override val title: String,
        override val text: String,
        override val ticketsPerAccount: Long,
        override val ticketsSold: Long,
        override val ticketsTotal: Long,
        override val type: EventType,
        override val updatedAt: Date,
        override val participants: List<String>

) : EventModel {

}

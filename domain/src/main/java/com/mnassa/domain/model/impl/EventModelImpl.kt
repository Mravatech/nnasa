package com.mnassa.domain.model.impl

import android.net.Uri
import com.mnassa.domain.interactor.PostPrivacyOptions
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
        override val duration: EventDuration?,
        override val startAt: Date,
        override val locationType: EventLocationType,
        override val allConnections: Boolean,
        override val privacyConnections: Set<String>,
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
        override val participants: List<String>,
        override val groups: List<GroupModel>,
        override val contact_via_mnassa: Boolean

) : EventModel

data class EventTicketModelImpl(
        override val eventName: String,
        override val eventOrganizerId: String,
        override val pricePerTicket: Long,
        override val ticketCount: Long,
        override var id: String,
        override val ownerId: String //similar to id
) : EventTicketModel

data class RawEventModel(
        val id: String? = null,
        val title: String,
        val description: String,
        val type: EventType,
        val startDateTime: Date,
        val durationMillis: Long,
        val imagesToUpload: List<Uri>,
        val uploadedImages: MutableSet<String>,
        val privacy: PostPrivacyOptions,
        val ticketsTotal: Int,
        val ticketsPerAccount: Int,
        val price: Long?,
        val locationType: EventLocationType,
        val tagModels: List<TagModel>,
        val tagIds: MutableSet<String> = mutableSetOf(),
        val status: EventStatus,
        val groupIds: Set<String>,
        val needPush: Boolean?,
        val contact_via_mnassa: Boolean

)
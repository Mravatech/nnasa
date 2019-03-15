package com.mnassa.domain.model

import java.io.Serializable
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by Peter on 4/13/2018.
 */
interface EventModel : Model {
    val author: ShortAccountModel
    val commentsCount: Int
    val viewsCount: Int
    val createdAt: Date
    val startAt: Date
    val duration: EventDuration?
    val locationType: EventLocationType
    val allConnections: Boolean
    val privacyConnections: Set<String>
    val itemType: ItemType
    val originalId: String
    val originalCreatedAt: Date
    val pictures: List<String>
    val price: Long
    val privacyType: PostPrivacyType
    val status: EventStatus
    val tags: List<String>
    val title: String
    val text: String
    val ticketsPerAccount: Long
    val ticketsSold: Long
    val ticketsTotal: Long
    val type: EventType
    val updatedAt: Date
    val participants: List<String>
    val groups: List<GroupModel>

    companion object {
        const val DEFAULT_CREATED_AT = 0L
        const val DEFAULT_START_AT = 0L
        const val DEFAULT_ALL_CONNECTIONS = false
        const val DEFAULT_ORIGINAL_CREATED_AT = 0L
        const val DEFAULT_PRICE = 0L
        const val DEFAULT_TICKETS_PER_ACCOUNT = 0L
        const val DEFAULT_TICKETS_SOLD = 0L
        const val DEFAULT_TICKETS_TOTAL = 0L
        const val DEFAULT_UPDATED_AT = 0L
    }
}

interface EventTicketModel : Model {
    val eventName: String
    val eventOrganizerId: String
    val pricePerTicket: Long
    val ticketCount: Long
    val ownerId: String

    companion object {
        const val DEFAULT_EVENT_NAME = ""
        const val DEFAULT_EVENT_ORGANIZER_ID = HasIdMaybe.EMPTY_KEY
        const val DEFAULT_PRICE_PER_TICKET = 0L
        const val DEFAULT_TICKET_COUNT = 0L
    }
}

val EventModel.isActive: Boolean
    get() {
        return status is EventStatus.OPENED
    }

sealed class EventDuration(val value: Long) : Serializable {
    class Minute(value: Long) : EventDuration(value) {
        override fun toMillis(): Long = TimeUnit.MINUTES.toMillis(value)
    }

    class Hour(value: Long) : EventDuration(value) {
        override fun toMillis(): Long = TimeUnit.HOURS.toMillis(value)
    }

    class Day(value: Long) : EventDuration(value) {
        override fun toMillis(): Long = TimeUnit.DAYS.toMillis(value)
    }

    abstract fun toMillis(): Long
}

sealed class EventLocationType : Serializable {
    class Specified(val location: LocationPlaceModel?, val id: String?, val description: String?) : EventLocationType()
    class NotDefined : EventLocationType()
    class Later : EventLocationType()
}

sealed class EventStatus : Serializable {
    class ANNULED : EventStatus()
    class OPENED : EventStatus()
    class CLOSED : EventStatus()
    class SUSPENDED : EventStatus()
}

sealed class EventType(val position: Int) : Serializable {
    class LECTURE : EventType(0)
    class DISCUSSION : EventType(1)
    class WORKSHOP : EventType(2)
    class EXERCISE : EventType(3)
    class ACTIVITY : EventType(4)
}
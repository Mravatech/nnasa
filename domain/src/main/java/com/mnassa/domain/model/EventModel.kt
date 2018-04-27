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
}

interface EventTicketModel : Model {
    val eventName: String
    val eventOrganizerId: String
    val pricePerTicket: Long
    val ticketCount: Long
    val ownerId: String
}

val EventModel.isActive: Boolean
    get() {
        return status == EventStatus.OPENED
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
    class Specified(val location: LocationPlaceModel, val id: String) : EventLocationType()
    object NotDefined : EventLocationType()
    object Later : EventLocationType()
}

sealed class EventStatus : Serializable {
    object ANNULED : EventStatus()
    object OPENED : EventStatus()
    object CLOSED : EventStatus()
    object SUSPENDED : EventStatus()
}

sealed class EventType(val position: Int) : Serializable {
    object LECTURE : EventType(0)
    object DISCUSSION : EventType(1)
    object WORKSHOP : EventType(2)
    object EXERCISE : EventType(3)
    object ACTIVITY : EventType(4)
}
package com.mnassa.data.converter

import com.androidkotlincore.entityconverter.ConvertersContext
import com.androidkotlincore.entityconverter.ConvertersContextRegistrationCallback
import com.androidkotlincore.entityconverter.convert
import com.androidkotlincore.entityconverter.registerConverter
import com.mnassa.data.network.NetworkContract
import com.mnassa.data.network.bean.firebase.EventDbEntity
import com.mnassa.data.network.bean.firebase.EventTicketDbEntity
import com.mnassa.domain.model.*
import com.mnassa.domain.model.impl.EventModelImpl
import com.mnassa.domain.model.impl.EventTicketModelImpl
import java.util.*

/**
 * Created by Peter on 4/13/2018.
 */
class EventsConverter : ConvertersContextRegistrationCallback {

    override fun register(convertersContext: ConvertersContext) {
        convertersContext.registerConverter(this::convertEvent)
        convertersContext.registerConverter(this::convertItemType)
        convertersContext.registerConverter(this::convertStatus)
        convertersContext.registerConverter(this::convertType)
        convertersContext.registerConverter(this::convertTicket)
    }

    private fun convertEvent(input: EventDbEntity, tag: Any?, converter: ConvertersContext): EventModelImpl {
        return EventModelImpl(
                id = input.id,
                author = converter.convert(input.author),
                commentsCount = input.counters.comments,
                viewsCount = input.counters.views,
                createdAt = Date(input.createdAt),
                duration = convertDuration(input),
                startAt = Date(input.eventStartAt),
                locationType = convertLocation(input, converter),
                allConnections = input.allConnections,
                itemType = converter.convert(input.itemType),
                originalId = input.originalId,
                originalCreatedAt = Date(input.originalCreatedAt),
                pictures = input.pictures,
                price = input.price,
                privacyType = input.privacyType?.run { converter.convert(this, PostPrivacyType::class.java) }
                        ?: PostPrivacyType.PUBLIC,
                status = converter.convert(input.status),
                tags = input.tags ?: emptyList(),
                title = input.title,
                text = input.text,
                ticketsPerAccount = input.ticketsPerAccount,
                ticketsSold = input.ticketsSold,
                ticketsTotal = input.ticketsTotal,
                type = converter.convert(input.type),
                updatedAt = Date(input.updatedAt),
                participants = input.participants ?: emptyList())
    }

    private fun convertDuration(input: EventDbEntity?): EventDuration? {
        return when (input?.duration?.type ?: return null) {
            NetworkContract.EventDuration.DAY -> EventDuration.Day(input.duration.value)
            NetworkContract.EventDuration.MINUTE -> EventDuration.Minute(input.duration.value)
            NetworkContract.EventDuration.HOUR -> EventDuration.Hour(input.duration.value)
            else -> throw IllegalArgumentException("Invalid event duration ${input.duration.type}. Event: $input")
        }
    }

    private fun convertLocation(input: EventDbEntity, converter: ConvertersContext): EventLocationType {
        return when (input.locationType) {
            NetworkContract.EventLocationType.SPECIFY -> if (input.locationDbEntity != null) EventLocationType.Specified(
                    location = converter.convert(input.locationDbEntity),
                    id = requireNotNull(input.locationId) { "LocationId not specified, but type: ${input.locationType}. Event: $input" }
            ) else EventLocationType.Later //server side error
            NetworkContract.EventLocationType.LATER -> EventLocationType.Later
            NetworkContract.EventLocationType.NOT_DEFINED -> EventLocationType.NotDefined
            else -> throw IllegalArgumentException("Invalid location type ${input.locationType}. Event: $input")
        }
    }

    private fun convertItemType(input: String): ItemType {
        return when (input) {
            NetworkContract.ItemType.ORIGINAL -> ItemType.ORIGINAL
            NetworkContract.ItemType.REPOST -> ItemType.REPOST
            else -> throw IllegalArgumentException("Invalid item type $input")
        }
    }

    private fun convertStatus(input: String): EventStatus {
        return when (input) {
            NetworkContract.EventStatus.ANNULED -> EventStatus.ANNULED
            NetworkContract.EventStatus.CLOSED -> EventStatus.CLOSED
            NetworkContract.EventStatus.OPENED -> EventStatus.OPENED
            NetworkContract.EventStatus.SUSPENDED -> EventStatus.SUSPENDED
            else -> throw IllegalArgumentException("Invalid event status $input")
        }
    }

    private fun convertType(input: String): EventType {
        return when (input) {
            NetworkContract.EventType.ACTIVITY -> EventType.ACTIVITY
            NetworkContract.EventType.DISCUSSION -> EventType.DISCUSSION
            NetworkContract.EventType.EXERCISE -> EventType.EXERCISE
            NetworkContract.EventType.LECTURE -> EventType.LECTURE
            NetworkContract.EventType.WORKSHOP -> EventType.WORKSHOP
            else -> throw IllegalArgumentException("Invalid event type $input")
        }
    }

    private fun convertTicket(input: EventTicketDbEntity): EventTicketModelImpl {
        return EventTicketModelImpl(
                id = input.id,
                ownerId = input.id,
                eventName = input.eventName,
                eventOrganizerId = input.eventOrganizer,
                pricePerTicket = input.pricePerTicket,
                ticketCount = input.ticketsCount
        )
    }
}
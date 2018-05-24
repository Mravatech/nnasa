package com.mnassa.data.converter

import com.androidkotlincore.entityconverter.ConvertersContext
import com.androidkotlincore.entityconverter.ConvertersContextRegistrationCallback
import com.androidkotlincore.entityconverter.convert
import com.androidkotlincore.entityconverter.registerConverter
import com.mnassa.data.network.NetworkContract
import com.mnassa.data.network.bean.firebase.EventDbEntity
import com.mnassa.data.network.bean.firebase.EventTicketDbEntity
import com.mnassa.data.network.bean.retrofit.request.CreateOrEditEventRequest
import com.mnassa.data.network.stringValue
import com.mnassa.domain.model.*
import com.mnassa.domain.model.impl.EventModelImpl
import com.mnassa.domain.model.impl.EventTicketModelImpl
import com.mnassa.domain.model.impl.RawEventModel
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by Peter on 4/13/2018.
 */
class EventsConverter : ConvertersContextRegistrationCallback {

    override fun register(convertersContext: ConvertersContext) {
        convertersContext.registerConverter(this::convertEvent)
        convertersContext.registerConverter(this::convertItemType)
        convertersContext.registerConverter(this::convertStatus)
        convertersContext.registerConverter(this::convertType)
        convertersContext.registerConverter(this::convertFromType)
        convertersContext.registerConverter(this::convertTicket)
        convertersContext.registerConverter(this::convertCreateEvent)
    }

    private fun convertEvent(input: EventDbEntity, tag: Any?, converter: ConvertersContext): EventModelImpl {
        return EventModelImpl(
                id = input.id,
                author = converter.convert(input.author),
                commentsCount = input.counters.comments ?: 0,
                viewsCount = input.counters.views ?: 0,
                createdAt = Date(input.createdAt),
                duration = convertDuration(input),
                startAt = Date(input.eventStartAt),
                locationType = convertLocation(input, converter),
                allConnections = input.allConnections,
                privacyConnections = input.privacyConnections?.toSet() ?: emptySet(),
                itemType = converter.convert(input.itemType),
                originalId = input.originalId,
                originalCreatedAt = Date(input.originalCreatedAt),
                pictures = input.pictures,
                price = input.price,
                privacyType = input.privacyType?.run { converter.convert(this, PostPrivacyType::class.java) }
                        ?: PostPrivacyType.PUBLIC(),
                status = converter.convert(input.status),
                tags = input.tags ?: emptyList(),
                title = input.title,
                text = input.text,
                ticketsPerAccount = input.ticketsPerAccount,
                ticketsSold = input.ticketsSold,
                ticketsTotal = input.ticketsTotal,
                type = converter.convert(input.type),
                updatedAt = Date(input.updatedAt),
                participants = input.participants ?: emptyList(),
                groupIds = input.privacyCommunitiesIds ?: emptySet())
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
                    id = requireNotNull(input.locationId) { "LocationId not specified, but type: ${input.locationType}. Event: $input" },
                    description = input.locationDescription
            ) else EventLocationType.Later() //server side error
            NetworkContract.EventLocationType.LATER -> EventLocationType.Later()
            NetworkContract.EventLocationType.NOT_DEFINED -> EventLocationType.NotDefined()
            else -> throw IllegalArgumentException("Invalid location type ${input.locationType}. Event: $input")
        }
    }

    private fun convertFromLocation(input: EventLocationType): String {
        return when (input) {
            is EventLocationType.Specified -> NetworkContract.EventLocationType.SPECIFY
            is EventLocationType.NotDefined -> NetworkContract.EventLocationType.NOT_DEFINED
            is EventLocationType.Later -> NetworkContract.EventLocationType.LATER
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
            NetworkContract.EventStatus.ANNULED -> EventStatus.ANNULED()
            NetworkContract.EventStatus.CLOSED -> EventStatus.CLOSED()
            NetworkContract.EventStatus.OPENED -> EventStatus.OPENED()
            NetworkContract.EventStatus.SUSPENDED -> EventStatus.SUSPENDED()
            else -> throw IllegalArgumentException("Invalid event status $input")
        }
    }

    private fun convertType(input: String): EventType {
        return when (input) {
            NetworkContract.EventType.ACTIVITY -> EventType.ACTIVITY()
            NetworkContract.EventType.DISCUSSION -> EventType.DISCUSSION()
            NetworkContract.EventType.EXERCISE -> EventType.EXERCISE()
            NetworkContract.EventType.LECTURE -> EventType.LECTURE()
            NetworkContract.EventType.WORKSHOP -> EventType.WORKSHOP()
            else -> throw IllegalArgumentException("Invalid event type $input")
        }
    }

    private fun convertFromType(input: EventType): String {
        return when (input) {
            is EventType.ACTIVITY -> NetworkContract.EventType.ACTIVITY
            is EventType.DISCUSSION -> NetworkContract.EventType.DISCUSSION
            is EventType.EXERCISE -> NetworkContract.EventType.EXERCISE
            is EventType.LECTURE -> NetworkContract.EventType.LECTURE
            is EventType.WORKSHOP -> NetworkContract.EventType.WORKSHOP
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

    private fun convertCreateEvent(input: RawEventModel): CreateOrEditEventRequest {
        val locationType = input.locationType

        return CreateOrEditEventRequest(
                id = input.id,
                title = input.title,
                text = input.description,
                locationId = (locationType as? EventLocationType.Specified)?.location?.placeId,
                price = input.price ?: 0L,
                ticketsTotal = input.ticketsTotal,
                ticketsPerAccount = input.ticketsPerAccount,
                isPromoted = false,
                eventStartAt = input.startDateTime.time,
                duration = com.mnassa.data.network.bean.retrofit.request.EventDuration("minute", TimeUnit.MILLISECONDS.toMinutes(input.durationMillis)),
                pictures = input.uploadedImages.toList().takeIf { it.isNotEmpty() },
                type = convertFromType(input.type),
                locationType = convertFromLocation(input.locationType),
                privacyType = input.privacy.privacyType.stringValue,
                tags = input.tagIds.takeIf { it.isNotEmpty() }?.toList(),
                status = input.status.stringValue,
                locationDescription = (locationType as? EventLocationType.Specified)?.description,
                allConnections = input.privacy.privacyType is PostPrivacyType.PUBLIC,
                privacyConnections = input.privacy.privacyConnections.takeIf { it.isNotEmpty() }?.toList()
        )
    }
}
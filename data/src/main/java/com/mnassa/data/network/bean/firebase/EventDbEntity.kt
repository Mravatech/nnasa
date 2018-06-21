package com.mnassa.data.network.bean.firebase

import com.google.gson.annotations.SerializedName
import com.mnassa.domain.model.HasId

/**
 * Created by Peter on 4/13/2018.
 */
internal data class EventDbEntity(
        @SerializedName("id") override var id: String,
        @SerializedName("allConnections") val allConnections: Boolean,
        @SerializedName("author") val author: ShortAccountDbEntity,
        @SerializedName("copyOwner") val copyOwner: String,
        @SerializedName("counters") val counters: EventCountersDbEntity,
        @SerializedName("createdAt") val createdAt: Long,
        @SerializedName("duration") val duration: EventDurationDbEntity?,
        @SerializedName("eventStartAt") val eventStartAt: Long,
        @SerializedName("itemType") val itemType: String,
        @SerializedName("location") val locationDbEntity: LocationDbEntity?,
        @SerializedName("locationId") val locationId: String?,
        @SerializedName("locationType") val locationType: String,
        @SerializedName("originalCreatedAt") val originalCreatedAt: Long,
        @SerializedName("originalId", alternate = arrayOf("originalPostId")) val originalId: String,
        @SerializedName("pictures") val pictures: List<String>,
        @SerializedName("price") val price: Long,
        @SerializedName("privacyType") val privacyType: String?,
        @SerializedName("status") val status: String,
        @SerializedName("tags") val tags: List<String>?,
        @SerializedName("text") val text: String,
        @SerializedName("ticketsPerAccount") val ticketsPerAccount: Long,
        @SerializedName("ticketsSold") val ticketsSold: Long,
        @SerializedName("ticketsTotal") val ticketsTotal: Long,
        @SerializedName("title") val title: String,
        @SerializedName("type") val type: String,
        @SerializedName("updatedAt") val updatedAt: Long,
        @SerializedName("participants") val participants: List<String>?,
        @SerializedName("privacyConnections") val privacyConnections: List<String>?,
        @SerializedName("locationDescription") val locationDescription: String?,
        @SerializedName("privacyCommunitiesIds") val privacyCommunitiesIds: Set<String>?
) : HasId {
}

internal data class EventTicketDbEntity(
        @SerializedName("id") override var id: String,
        @SerializedName("eventName") val eventName: String,
        @SerializedName("eventOrganizer") val eventOrganizer: String,
        @SerializedName("pricePerTicket") val pricePerTicket: Long,
        @SerializedName("ticketsCount") val ticketsCount: Long
) : HasId

internal data class EventCountersDbEntity(
        @SerializedName("comments") val comments: Int?,
        @SerializedName("views") val views: Int?
)

internal data class EventDurationDbEntity(
        @SerializedName("type") val type: String,
        @SerializedName("value") val value: Long
)
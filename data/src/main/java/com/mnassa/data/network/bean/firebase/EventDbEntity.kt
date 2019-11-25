package com.mnassa.data.network.bean.firebase

import com.google.gson.annotations.SerializedName
import com.mnassa.domain.model.HasIdMaybe

/**
 * Created by Peter on 4/13/2018.
 */
internal data class EventDbEntity(
        @SerializedName("id") override var idOrNull: String?,
        @SerializedName("allConnections") val allConnections: Boolean?,
        @SerializedName("author") val author: ShortAccountDbEntity?,
        @SerializedName("copyOwner") val copyOwner: String?,
        @SerializedName("counters") val counters: EventCountersDbEntity?,
        @SerializedName("createdAt") val createdAt: Long?,
        @SerializedName("duration") val duration: EventDurationDbEntity?,
        @SerializedName("eventStartAt") val eventStartAt: Long?,
        @SerializedName("itemType") val itemType: String?,
        @SerializedName("location") val locationDbEntity: LocationDbEntity?,
        @SerializedName("locationId") val locationId: String?,
        @SerializedName("locationType") val locationType: String?,
        @SerializedName("originalCreatedAt") val originalCreatedAt: Long?,
        @SerializedName("originalId", alternate = ["originalPostId"]) val originalId: String?,
        @SerializedName("pictures") val pictures: List<String>?,
        @SerializedName("price") val price: Long?,
        @SerializedName("privacyType") val privacyType: String?,
        @SerializedName("status") val status: String?,
        @SerializedName("tags") val tags: List<String>?,
        @SerializedName("text") val text: String?,
        @SerializedName("ticketsPerAccount") val ticketsPerAccount: Long?,
        @SerializedName("ticketsSold") val ticketsSold: Long?,
        @SerializedName("ticketsTotal") val ticketsTotal: Long?,
        @SerializedName("title") val title: String?,
        @SerializedName("type") val type: String?,
        @SerializedName("updatedAt") val updatedAt: Long?,
        @SerializedName("participants") val participants: List<String>?,
        @SerializedName("privacyConnections") val privacyConnections: List<String>?,
        @SerializedName("locationDescription") val locationDescription: String?,
        @SerializedName("privacyCommunitiesIds") val groups: Set<String>?,
        @SerializedName("contact_via_mnassa") val contact_via_mnassa: Boolean

) : HasIdMaybe {

    companion object {
        const val CREATED_AT = "createdAt"
        const val VISIBLE_FOR_USERS = "visibleFor"
        const val VISIBLE_FOR_GROUPS = "visibleForCommunities"
    }
}

internal data class EventTicketDbEntity(
        @SerializedName("id") override var idOrNull: String?,
        @SerializedName("eventName") val eventName: String?,
        @SerializedName("eventOrganizer") val eventOrganizer: String?,
        @SerializedName("pricePerTicket") val pricePerTicket: Long?,
        @SerializedName("ticketsCount") val ticketsCount: Long?
) : HasIdMaybe

internal data class EventCountersDbEntity(
        @SerializedName("comments") val comments: Int?,
        @SerializedName("views") val views: Int?
)

internal data class EventDurationDbEntity(
        @SerializedName("type") val type: String?,
        @SerializedName("value") val value: Long?
)
package com.mnassa.data.network.bean.firebase

import com.google.gson.annotations.SerializedName
import com.mnassa.domain.model.HasId

/**
 * Created by Peter on 4/13/2018.
 */
internal data class EventDbEntity(
        override var id: String,
        @SerializedName("allConnections") val allConnections: Boolean,
        @SerializedName("author") val author: ShortAccountDbEntity,
        @SerializedName("copyOwner") val copyOwner: String,
        @SerializedName("counters") val counters: EventCountersDbEntity,
        @SerializedName("createdAt") val createdAt: Long,
        @SerializedName("duration") val duration: EventDurationDbEntity,
        @SerializedName("eventStartAt") val eventStartAt: Long,
        @SerializedName("itemType") val itemType: Long,
        @SerializedName("location") val locationDbEntity: LocationDbEntity,
        @SerializedName("locationId") val locationId: String,
        @SerializedName("locationType") val locationType: String,
        @SerializedName("originalCreatedAt") val originalCreatedAt: Long,
        @SerializedName("originalId") val originalId: Long,
        @SerializedName("pictures") val pictures: List<String>,
        @SerializedName("price") val price: Long,

        ) : HasId {
}

internal data class EventCountersDbEntity(
        @SerializedName("comments") val comments: Int,
        @SerializedName("views") val views: Int
)

internal data class EventDurationDbEntity(
    @SerializedName("type") val type: String,
    @SerializedName("value") val value: Long
)
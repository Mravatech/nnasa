package com.mnassa.data.network.bean.retrofit.request

import com.google.gson.annotations.SerializedName

/**
 * Created by Peter on 4/26/2018.
 */
data class CreateOrEditEventRequest (
        @SerializedName("id") val id: String? = null,
        @SerializedName("type") val type: String,
        @SerializedName("title") val title: String,
        @SerializedName("text") val text: String,
        @SerializedName("locationId") val locationId: String?,
        @SerializedName("locationType") val locationType: String,
        @SerializedName("locationDescription") val locationDescription: String?,
        @SerializedName("privacyType") val privacyType: String,
        @SerializedName("ticketsPerAccount") val ticketsPerAccount: Int,
        @SerializedName("price") val price: Long,
        @SerializedName("ticketsTotal") val ticketsTotal: Int,
        @SerializedName("eventStartAt") val eventStartAt: Long,
        @SerializedName("isPromoted") val isPromoted: Boolean,
        @SerializedName("duration") val duration: EventDuration,
        @SerializedName("pictures") val pictures: List<String>?,
        @SerializedName("tags") val tags: List<String>?,
        @SerializedName("status") val status: String
)

data class EventDuration(
        @SerializedName("type") val type: String,
        @SerializedName("value") val value: Long
)
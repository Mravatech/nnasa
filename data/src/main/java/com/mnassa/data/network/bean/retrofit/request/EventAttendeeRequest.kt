package com.mnassa.data.network.bean.retrofit.request

import com.google.gson.annotations.SerializedName

/**
 * Created by Peter on 4/20/2018.
 */
data class EventAttendeeRequest(
        @SerializedName("id") val eventId: String,
        @SerializedName("attendees") val attendees: List<EventAttendeeBean>
)

data class EventAttendeeBean(
        @SerializedName("id") val id: String,
        @SerializedName("presence") val presence: Boolean
)
package com.mnassa.data.network.bean.retrofit.request

import com.google.gson.annotations.SerializedName

/**
 * Created by Peter on 4/19/2018.
 */
data class BuyTicketsRequest(
        @SerializedName("eventId") val eventId: String,
        @SerializedName("ticketsCount") val ticketsCount: Long,
        @SerializedName("description") val description: String? = null
)
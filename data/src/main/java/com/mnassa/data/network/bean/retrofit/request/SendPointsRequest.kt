package com.mnassa.data.network.bean.retrofit.request

import com.google.gson.annotations.SerializedName

/**
 * Created by Peter on 4/2/2018.
 */
data class SendPointsRequest(
        @SerializedName("fromAid") val fromAid: String,
        @SerializedName("toAid") val toAid: String,
        @SerializedName("type") val type: String = "user2User",
        @SerializedName("amount") val amount: Long,
        @SerializedName("userDescription") val userDescription: String?
)
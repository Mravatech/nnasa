package com.mnassa.data.network.bean.retrofit.request

import com.google.gson.annotations.SerializedName

/**
 * Created by Peter on 9/25/2018.
 */
data class SendSmsRequest(
        @SerializedName("phone") val phone: String,
        @SerializedName("test") val isTest: Boolean = false
)

data class CheckSmsRequest(
        @SerializedName("phone") val phone: String,
        @SerializedName("id") val id: String,
        @SerializedName("code") val code: String
)
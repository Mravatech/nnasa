package com.mnassa.data.network.bean.retrofit.response

import com.google.gson.annotations.SerializedName

/**
 * Created by Peter on 9/25/2018.
 */
data class SendSmsResponse (
        @SerializedName("data") val data: SendSmsResponseData
): MnassaResponse()

data class SendSmsResponseData(
        @SerializedName("id") val id: String
)

data class CheckSmsResponse (
        @SerializedName("data") val data: CheckSmsResponseData
): MnassaResponse()

data class CheckSmsResponseData(
        @SerializedName("token") val token: String
)
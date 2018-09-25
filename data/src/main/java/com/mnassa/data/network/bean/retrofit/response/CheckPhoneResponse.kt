package com.mnassa.data.network.bean.retrofit.response

import com.google.gson.annotations.SerializedName

/**
 * Created by Peter on 9/25/2018.
 */
data class CheckPhoneResponse(
        @SerializedName("data") val data: CheckPhoneResponseData
) : MnassaResponse()

data class CheckPhoneResponseData(
        @SerializedName("customAuth") val customAuth: Boolean = false
)
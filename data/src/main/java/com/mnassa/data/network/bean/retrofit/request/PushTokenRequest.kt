package com.mnassa.data.network.bean.retrofit.request

import com.google.gson.annotations.SerializedName

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/12/2018
 */

data class PushTokenRequest(
        @SerializedName("token")
        val token: String,
        @SerializedName("deviceInfo")
        val deviceInfo: String
)
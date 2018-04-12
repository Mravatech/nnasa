package com.mnassa.data.network.bean.retrofit.request

import com.google.gson.annotations.SerializedName

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/1/2018
 */
data class ConnectionStatusRequest(
        @SerializedName("action") val action: String,
        @SerializedName("requestAid") val requestAid: List<String>
)
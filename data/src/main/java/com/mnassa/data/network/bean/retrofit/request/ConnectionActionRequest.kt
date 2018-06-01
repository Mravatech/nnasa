package com.mnassa.data.network.bean.retrofit.request

import com.google.gson.annotations.SerializedName

/**
 * Created by Peter on 3/5/2018.
 */
data class ConnectionActionRequest(
        @SerializedName("action") val action: String,
        @SerializedName("requestAid") val accountIds: List<String>
)
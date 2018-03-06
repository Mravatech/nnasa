package com.mnassa.data.network.bean.retrofit.request

import com.google.gson.annotations.SerializedName

/**
 * Created by Peter on 3/5/2018.
 */
data class ConnectionActionRequest(
        @SerializedName("action")
        private val action: String,
        @SerializedName("requestAid")
        private val accountIds: List<String>
)
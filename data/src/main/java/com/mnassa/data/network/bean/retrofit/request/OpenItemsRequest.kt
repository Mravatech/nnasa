package com.mnassa.data.network.bean.retrofit.request

import com.google.gson.annotations.SerializedName

/**
 * Created by Peter on 3/16/2018.
 */
data class OpenItemsRequest(
        @SerializedName("id") val id: String,
        @SerializedName("type") val type: String
)
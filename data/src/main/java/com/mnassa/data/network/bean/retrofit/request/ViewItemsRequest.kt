package com.mnassa.data.network.bean.retrofit.request

import com.google.gson.annotations.SerializedName

/**
 * Created by Peter on 3/16/2018.
 */
data class ViewItemsRequest(
        @SerializedName("ids") val ids: List<String>,
        @SerializedName("type") val type: String
)
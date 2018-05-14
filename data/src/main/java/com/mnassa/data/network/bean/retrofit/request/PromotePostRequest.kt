package com.mnassa.data.network.bean.retrofit.request

import com.google.gson.annotations.SerializedName

/**
 * Created by Peter on 5/8/2018.
 */
data class PromotePostRequest(
        @SerializedName("id") val id: String,
        @SerializedName("type") val type: String
)
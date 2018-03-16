package com.mnassa.data.network.bean.retrofit.request

import com.google.gson.annotations.SerializedName

/**
 * Created by Peter on 3/16/2018.
 */
data class CreatePostRequest(
        @SerializedName("type") val type: String,
        @SerializedName("text") val text: String,
        @SerializedName("location") val location: String? = null,
        @SerializedName("tags") val tags: List<String>? = null,
        @SerializedName("images") val images: List<String>? = null,
        @SerializedName("privacyType") val privacyType: String,
        @SerializedName("accountForRecommendation") val accountForRecommendation: String? = null,
//        @SerializedName("sharingType") val sharingType: String,

        @SerializedName("privacyConnections") val privacyConnections: List<String>? = null,
        @SerializedName("price") val price: Long? = null
)
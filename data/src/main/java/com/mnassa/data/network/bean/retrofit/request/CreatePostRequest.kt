package com.mnassa.data.network.bean.retrofit.request

import com.google.gson.annotations.SerializedName

/**
 * Created by Peter on 3/16/2018.
 */
data class CreatePostRequest(
        @SerializedName("type") val type: String,
        @SerializedName("text") val text: String,
        @SerializedName("locationId") val location: String? = null,
        @SerializedName("tags") val tags: List<String>? = null,
        @SerializedName("images") val images: List<String>? = null,
        @SerializedName("privacyType") val privacyType: String? = null,
        @SerializedName("allConnections") val allConnections: Boolean? = null,
        @SerializedName("postedAccount") val accountForRecommendation: String? = null,
        @SerializedName("privacyConnections") val privacyConnections: List<String>? = null,
        @SerializedName("price") val price: Long? = null,
        @SerializedName("postId") val postId: String? = null, //for post updating

        @SerializedName("title") var title: String? = null, //for offer post
        @SerializedName("category") val category: String? = null, //for offer post
        @SerializedName("subcategory") val subcategory: String? = null //for offer post
)
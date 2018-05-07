package com.mnassa.data.network.bean.retrofit.request

import com.google.gson.annotations.SerializedName

/**
 * Created by Peter on 3/27/2018.
 */
data class RepostCommentRequest(
        @SerializedName("postId") val postId: String,
        @SerializedName("repostText") val text: String? = null,
        @SerializedName("privacyConnections") val privacyConnections: List<String> = emptyList(),
        @SerializedName("allConnections") val allConnections: Boolean
)
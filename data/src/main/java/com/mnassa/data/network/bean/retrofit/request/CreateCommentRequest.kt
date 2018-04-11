package com.mnassa.data.network.bean.retrofit.request

import com.google.gson.annotations.SerializedName

/**
 * Created by Peter on 3/26/2018.
 */
data class CreateCommentRequest(
        @SerializedName("postId") val postId: String,
        @SerializedName("text") val text: String?,
        @SerializedName("entityType") val entityType: String,
        @SerializedName("accountIds") val accountIds: List<String>,
        @SerializedName("commentId") val commentId: String? = null
)
package com.mnassa.data.network.bean.retrofit.request

import com.google.gson.annotations.SerializedName

/**
 * Created by Peter on 3/29/2018.
 */
data class EditCommentRequest(
        @SerializedName("commentId") val commentId: String,
        @SerializedName("text") val text: String?,
        @SerializedName("entityType") val entityType: String,
        @SerializedName("accountIds") val accountIds: List<String>?
)
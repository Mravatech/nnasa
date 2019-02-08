package com.mnassa.data.network.bean.retrofit.request

import com.google.gson.annotations.SerializedName

/**
 * Created by Peter on 4/2/2018.
 */
data class SendPointsRequest(
        @SerializedName("fromAid") val fromAid: String,
        @SerializedName("toAid") val toAid: String,
        @SerializedName("type") val type: String,
        @SerializedName("amount") val amount: Long,
        @SerializedName("userDescription") val userDescription: String?
)

data class RewardForCommentRequest(
        @SerializedName("fromAid") val fromAid: String,
        @SerializedName("toAid") val toAid: String,
        @SerializedName("type") val type: String = "rewardForComment",
        @SerializedName("amount") val amount: Long,
        @SerializedName("userDescription") val userDescription: String?,
        @SerializedName("parentCommentId") val parentCommentId: String?,
        @SerializedName("commentId") var commentId: String
)


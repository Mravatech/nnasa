package com.mnassa.data.network.bean.retrofit.response

import com.google.gson.annotations.SerializedName
import com.mnassa.data.network.bean.firebase.ShortAccountDbEntity

/**
 * Created by Peter on 3/23/2018.
 */
class GetCommentsResponse : MnassaResponse() {
    @SerializedName("data") internal lateinit var data: GetCommentsResponseData

    override fun toString(): String =  data.toString()
}

internal data class GetCommentsResponseData(
        @SerializedName("comments") var data: Map<String, CommentResponseEntity>?,
        @SerializedName("infoRestriction") val infoRestriction: String?
)

internal data class CommentResponseEntity(
        @SerializedName("createdAt") val createdAt: Long,
        @SerializedName("creator") val creator: Map<String, ShortAccountDbEntity>,
        @SerializedName("isPrivate") var isPrivate: Boolean = false,
        @SerializedName("isRewarded") var isRewarded: Boolean = false,
        @SerializedName("replies") var replies: Map<String, CommentResponseEntity>?,
        @SerializedName("text") var text: String?,
        @SerializedName("accounts") var recommendedAccounts: Map<String, ShortAccountDbEntity>?,
        @SerializedName("images") var images: List<String>?
) {
    lateinit var id: String
    var parentItemId: String? = null
}
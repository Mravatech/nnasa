package com.mnassa.data.network.bean.retrofit.response

import com.google.gson.annotations.SerializedName

/**
 * Created by Peter on 3/26/2018.
 */
class CreateCommentResponse : MnassaResponse() {
    @SerializedName("data")
    internal lateinit var data: CreateCommentData
}

internal data class CreateCommentData(
        @SerializedName("comment") val comment: Map<String, CommentResponseEntity>
)
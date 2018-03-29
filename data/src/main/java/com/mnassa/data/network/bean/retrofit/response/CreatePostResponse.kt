package com.mnassa.data.network.bean.retrofit.response

import com.google.gson.annotations.SerializedName
import com.mnassa.data.network.bean.firebase.PostDbEntity

/**
 * Created by Peter on 3/16/2018.
 */
class CreatePostResponse : MnassaResponse() {
    @SerializedName("data")
    internal lateinit var data: PostData
}

internal class PostData {
    @SerializedName("id")
    internal lateinit var id: String
    @SerializedName("post")
    internal lateinit var post: PostDbEntity
}
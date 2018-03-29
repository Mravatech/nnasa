package com.mnassa.data.network.bean.retrofit.request

import com.google.gson.annotations.SerializedName

/**
 * Created by Peter on 3/21/2018.
 */
data class RemovePostRequest(
        @SerializedName("postId") val postId: String
) {
}
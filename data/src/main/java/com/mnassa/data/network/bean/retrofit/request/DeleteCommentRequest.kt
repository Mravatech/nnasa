package com.mnassa.data.network.bean.retrofit.request

import com.google.gson.annotations.SerializedName

/**
 * Created by Peter on 3/27/2018.
 */
data class DeleteCommentRequest(@SerializedName("commentId") val commentId: String)
package com.mnassa.data.network.bean.retrofit.request

import com.google.gson.annotations.SerializedName

/**
 * Created by Peter on 3/23/2018.
 */
data class GetCommentsRequest(@SerializedName("postId") val postId: String? = null,
                              @SerializedName("eventId") val eventId: String? = null,
                              @SerializedName("entityType") val entityType: String)
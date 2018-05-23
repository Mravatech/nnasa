package com.mnassa.data.network.bean.retrofit.request

import com.google.gson.annotations.SerializedName

/**
 * Created by Peter on 5/23/2018.
 */
data class CreateGroupRequest(
        @SerializedName("title") val title: String,
        @SerializedName("description") val description: String?,
        @SerializedName("avatar") val avatar: String?,
        @SerializedName("website") val website: String?,
        @SerializedName("location") val location: String?,
        @SerializedName("communityId") val communityId: String?,
        @SerializedName("tags") val tags: List<String>?
)
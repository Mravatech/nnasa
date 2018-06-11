package com.mnassa.data.network.bean.retrofit.request

import com.google.gson.annotations.SerializedName

/**
 * Created by Peter on 5/22/2018.
 */
data class GroupConnectionRequest(
        @SerializedName("action") val action: String,
        @SerializedName("communityId") val groupId: String,
        @SerializedName("aids") val accounts: List<String>? = null
)
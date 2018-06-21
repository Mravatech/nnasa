package com.mnassa.data.network.bean.retrofit.request

import com.google.gson.annotations.SerializedName

/**
 * Created by Peter on 6/6/2018.
 */
data class DeleteGroupRequest(
        @SerializedName("id") val id: String
)
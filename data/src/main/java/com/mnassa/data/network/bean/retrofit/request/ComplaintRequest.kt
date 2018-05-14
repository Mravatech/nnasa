package com.mnassa.data.network.bean.retrofit.request

import com.google.gson.annotations.SerializedName

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/10/2018
 */
data class ComplaintRequest(
        @SerializedName("id") val id: String,
        @SerializedName("type") val type: String,
        @SerializedName("reason") val reason: String,
        @SerializedName("authorText") val authorText: String?
)
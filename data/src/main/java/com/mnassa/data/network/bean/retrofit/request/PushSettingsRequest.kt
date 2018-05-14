package com.mnassa.data.network.bean.retrofit.request

import com.google.gson.annotations.SerializedName

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/20/2018
 */

data class PushSettingsRequest (
        @SerializedName("isActive") val isActive: Boolean,
        @SerializedName("withSound") val withSound: Boolean
)
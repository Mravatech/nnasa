package com.mnassa.data.network.bean.retrofit.request

import com.google.gson.annotations.SerializedName

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/13/2018
 */
data class NotificationViewRequest(
        @SerializedName("resetCounter")
        val resetCounter: Boolean,
        @SerializedName("all")
        val all: Boolean,
        @SerializedName("ids")
        val ids: List<String>
)
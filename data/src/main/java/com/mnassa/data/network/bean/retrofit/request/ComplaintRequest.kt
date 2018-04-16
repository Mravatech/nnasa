package com.mnassa.data.network.bean.retrofit.request

import com.google.gson.annotations.SerializedName

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/10/2018
 */
data class ComplaintRequest(

        @SerializedName("id")
        private val id: String,
        @SerializedName("type")
        private val type: String,
        @SerializedName("reason")
        private val reason: String
)
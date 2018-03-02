package com.mnassa.data.network.bean.retrofit

import com.google.gson.annotations.SerializedName

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/1/2018
 */
data class RegisterSendAccountInfoRequest(
        @SerializedName("contactPhone")
        val contactPhone: String,
        @SerializedName("userName")
        val userName: String,
        @SerializedName("firstName")
        val firstName: String,
        @SerializedName("lastName")
        val lastName: String,
        @SerializedName("placeId")
        val placeId: String,
        @SerializedName("type")
        val type: String,
        @SerializedName("avatar")
        val avatar: String,
        @SerializedName("language")
        val language: String,
        @SerializedName("id")
        val id: String
)
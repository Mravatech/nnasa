package com.mnassa.data.network.bean.retrofit

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Created by Peter on 2/27/2018.
 */
data class RegisterAccountResponse(
        @SerializedName("state")
        val state: String,
        @SerializedName("account")
        val account: Account
)

data class Account(
        @SerializedName("id")
        val id: String,
        @SerializedName("userID")
        val userId: String,
        @SerializedName("userName")
        val userName: String,
        @SerializedName("firstName")
        val firstName: String?,
        @SerializedName("lastName")
        val lastName: String?,
        @SerializedName("contactPhone")
        val contactPhone: String,
        @SerializedName("language")
        val language: String,
        @SerializedName("organizationName")
        val organizationName: String?,
        @SerializedName("type")
        val type: String
): Serializable
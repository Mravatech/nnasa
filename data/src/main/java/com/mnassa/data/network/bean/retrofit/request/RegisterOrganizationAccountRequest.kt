package com.mnassa.data.network.bean.retrofit.request

import com.google.gson.annotations.SerializedName

/**
 * Created by Peter on 2/27/2018.
 */
data class RegisterOrganizationAccountRequest(
        @SerializedName("userName")
        val userName: String,
        @SerializedName("type")
        val type: String,
        @SerializedName("organizationName")
        val organizationName: String,
        @SerializedName("offers")
        val offers: String?,
        @SerializedName("interests")
        val interests: String?
)
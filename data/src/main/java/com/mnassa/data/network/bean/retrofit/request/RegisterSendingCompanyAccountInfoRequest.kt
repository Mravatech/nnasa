package com.mnassa.data.network.bean.retrofit.request

import com.google.gson.annotations.SerializedName

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/1/2018
 */

data class RegisterSendingCompanyAccountInfoRequest(
        @SerializedName("organizationName")
        val organizationName: String,
        @SerializedName("avatar")
        val avatar: String? = null,
        @SerializedName("showContactEmail")
        val showContactEmail: Boolean? = false,
        @SerializedName("contactEmail")
        val contactEmail: String?,
        @SerializedName("userName")
        val userName: String,
        @SerializedName("language")
        val language: String? = null,
        @SerializedName("type")
        val type: String,
        @SerializedName("founded")
        val founded: Long? = 0,
        @SerializedName("id")
        val id: String,
        @SerializedName("website")
        val website: String?,
        @SerializedName("organizationType")
        val organizationType: String?
)

package com.mnassa.data.network.bean.retrofit.request

import com.google.gson.annotations.SerializedName

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/1/2018
 */

data class RegisterSendingOrganisationInfoRequest(
        @SerializedName("organizationName")
        val organizationName: String? = null,
        @SerializedName("contactPhone")
        val contactPhone: String? = null,
        @SerializedName("avatar")
        val avatar: String? = null,
        @SerializedName("showContactEmail")
        val showContactEmail: Boolean? = false,
        @SerializedName("showContactPhone")
        val showContactPhone: Boolean? = null,
        @SerializedName("birthday")
        val birthday: Double? = 0.0,
        @SerializedName("offers")
        val offers: List<String>? = null,
        @SerializedName("interests")
        val interests: List<String>? = null,
        @SerializedName("language")
        val language: String? = null,
        @SerializedName("id")
        val id: String? = null
)


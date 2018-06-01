package com.mnassa.data.network.bean.retrofit.request

import com.google.gson.annotations.SerializedName

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/20/2018
 */
data class PhoneContactRequest(
        @SerializedName("phone") val phone: String,
        @SerializedName("description") val description: String?,
        @SerializedName("avatar") val avatar: String?
)

data class ContactsRequest(
        @SerializedName("phones") val phones: List<PhoneContactRequest>
)
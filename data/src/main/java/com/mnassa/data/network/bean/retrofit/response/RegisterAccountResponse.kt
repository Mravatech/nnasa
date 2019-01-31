package com.mnassa.data.network.bean.retrofit.response

import com.google.gson.annotations.SerializedName
import com.mnassa.data.network.bean.firebase.ConnectedByDbEntity
import java.io.Serializable

/**
 * Created by Peter on 2/27/2018.
 */
data class RegisterAccountResponse(
        @SerializedName("state") val state: String,
        @SerializedName("data") val data: AccountData
)

data class AccountData(
        @SerializedName("account") val account: AccountResponseBean
)

data class AccountResponseBean(
        @SerializedName("id") val id: String,
        @SerializedName("serialNumber") val serialNumber: Int?,
        @SerializedName("userID") val userId: String,
        @SerializedName("userName") val userName: String,
        @SerializedName("firstName") val firstName: String?,
        @SerializedName("lastName") val lastName: String?,
        @SerializedName("contactPhone") val contactPhone: String,
        @SerializedName("language") val language: String,
        @SerializedName("organizationName") val organizationName: String?,
        @SerializedName("type") val type: String,
        @SerializedName("connectedBy") val connectedBy: ConnectedByDbEntity?
) : Serializable
package com.mnassa.data.network.bean.retrofit.request

import com.google.gson.annotations.SerializedName

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/1/2018
 */
data class RegisterSendingAccountInfoRequest(
        @SerializedName("birthdayDate")
        val birthdayDate: String? = null,
        @SerializedName("lastName")
        val lastName: String,
        @SerializedName("userName")
        val userName: String,
        @SerializedName("showContactEmail")
        val showContactEmail: Boolean? = false,
        @SerializedName("language")
        val language: String? = null,
        @SerializedName("type")
        val type: String,
        @SerializedName("birthday")
        val birthday: Long? = 0,
        @SerializedName("contactPhone")
        val contactPhone: String? = null,
        @SerializedName("abilities")
        val abilities: List<Ability>? = null,
        @SerializedName("id")
        val id: String,
        @SerializedName("avatar")
        val avatar: String? = null,
        @SerializedName("firstName")
        val firstName: String,
        @SerializedName("showContactPhone")
        val showContactPhone: Boolean? = false
)

data class Ability(
        @SerializedName("name")
        val name: String? = null,
        @SerializedName("place")
        val place: String? = null,
        @SerializedName("isMain")
        val isMain: Boolean? = false
)

data class Location(
        @SerializedName("placeId")
        val placeId: String? = null

)
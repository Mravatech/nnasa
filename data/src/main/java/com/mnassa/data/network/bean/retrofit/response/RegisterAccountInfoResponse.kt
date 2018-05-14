package com.mnassa.data.network.bean.retrofit.response

import com.google.gson.annotations.SerializedName

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/1/2018
 */
data class RegisterAccountInfoResponse(
        @SerializedName("status")
        val status: String//,
//        @SerializedName("data")
//        val account: ProcessAccount
)

data class ProcessAccount(
        @SerializedName("avatar")
        val avatar: String,
        @SerializedName("contactPhone")
        val contactPhone: String,
        @SerializedName("createdAt")
        val createdAt: Long,
        @SerializedName("createdAtDate")
        val createdAtDate: String,
        @SerializedName("firstName")
        val firstName: String,
        @SerializedName("invites")
        val invites: Int,
        @SerializedName("language")
        val language: String,
        @SerializedName("lastName")
        val lastName: String,
        @SerializedName("numberOfCommunities")
        val numberOfCommunities: Int,
        @SerializedName("numberOfConnections")
        val numberOfConnections: Int,
        @SerializedName("numberOfDisconnected")
        val numberOfDisconnected: Int,
        @SerializedName("numberOfRecommendations")
        val numberOfRecommendations: Int,
        @SerializedName("numberOfRequested")
        val numberOfRequested: Int,
        @SerializedName("numberOfSent")
        val numberOfSent: Int,
        @SerializedName("numberOfUnreadChats")
        val numberOfUnreadChats: Int,
        @SerializedName("numberOfUnreadNotifications")
        val numberOfUnreadNotifications: Int,
        @SerializedName("numberOfUnreadResponses")
        val numberOfUnreadResponses: Int,
        @SerializedName("placeId")
        val placeId: String,
        @SerializedName("type")
        val type: String,
        @SerializedName("updatedAt")
        val updatedAt: Long,
        @SerializedName("updatedAtDate")
        val updatedAtDate: String
        )
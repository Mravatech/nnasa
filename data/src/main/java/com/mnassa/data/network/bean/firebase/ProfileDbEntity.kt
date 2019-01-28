package com.mnassa.data.network.bean.firebase

import com.google.gson.annotations.SerializedName

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/23/2018
 */

internal class ProfileDbEntity(
        id: String,
        serialNumber: Int?,
        avatar: String?,
        firstName: String?,
        lastName: String?,
        organizationName: String?,
        type: String,
        userName: String,
        abilitiesInternal: List<ShortAccountAbilityDbEntity>,
        createdAt: Long,
        @SerializedName("createdAtDate") var createdAtDate: String?,
        @SerializedName("interests") var interests: List<String>?,
        @SerializedName("offers") var offers: List<String>?,
        @SerializedName("points") var points: Int?,
        @SerializedName("totalIncome") var totalIncome: Int?,
        @SerializedName("totalOutcome") var totalOutcome: Int?,
        @SerializedName("contactEmail") var contactEmail: String?,
        @SerializedName("contactPhone") val contactPhone: String?,
        @SerializedName("showContactPhone") var showContactPhone: Boolean,
        @SerializedName("showContactEmail") var showContactEmail: Boolean,
        @SerializedName("numberOfCommunities") val numberOfCommunities: Int?,
        @SerializedName("numberOfConnections") val numberOfConnections: Int?,
        @SerializedName("numberOfDisconnected") val numberOfDisconnected: Int?,
        @SerializedName("numberOfRecommendations") val numberOfRecommendations: Int?,
        @SerializedName("numberOfRequested") val numberOfRequested: Int?,
        @SerializedName("numberOfSent") val numberOfSent: Int?,
        @SerializedName("numberOfUnreadChats") val numberOfUnreadChats: Int?,
        @SerializedName("numberOfUnreadEvents") val numberOfUnreadEvents: Int?,
        @SerializedName("numberOfUnreadNeeds") val numberOfUnreadNeeds: Int?,
        @SerializedName("numberOfUnreadNotifications") val numberOfUnreadNotifications: Int?,
        @SerializedName("numberOfUnreadResponses") val numberOfUnreadResponses: Int?,
        @SerializedName("visiblePoints") val visiblePoints: Int,
        location: LocationDbEntity?,
        gender: String?,
        @SerializedName("website") val website: String?,
        @SerializedName("organizationType") val organizationType: String?,
        @SerializedName("birthday") val birthday: Long?
) : ShortAccountDbEntity(id, serialNumber, avatar, firstName, lastName, organizationName, type, userName, abilitiesInternal) {

    @SerializedName("createdAt") var createdAt: Long? = createdAt
    @SerializedName("location") val location: LocationDbEntity? = location
    @SerializedName("gender") val gender: String? = gender

}
package com.mnassa.data.network.bean.firebase

import com.google.gson.annotations.SerializedName

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/23/2018
 */

internal class ProfileDbEntity : ShortAccountDbEntity {

    @SerializedName("createdAt")
    var createdAt: Long?
    @SerializedName("createdAtDate")
    var createdAtDate: String?
    @SerializedName("contactEmail")
    var contactEmail: String?
    @SerializedName("showContactEmail")
    var showContactEmail: Boolean
    @SerializedName("showContactPhone")
    var showContactPhone: Boolean
    @SerializedName("firebaseUserId")
    var firebaseUserId: String
    @SerializedName("interests")
    var interests: List<String>?
    @SerializedName("offers")
    var offers: List<String>?
    @SerializedName("points")
    var points: Int?
    @SerializedName("totalIncome")
    var totalIncome: Int?
    @SerializedName("totalOutcome")
    var totalOutcome: Int?
    @SerializedName("numberOfCommunities")
    val numberOfCommunities: Int?
    @SerializedName("numberOfConnections")
    val numberOfConnections: Int?
    @SerializedName("numberOfDisconnected")
    val numberOfDisconnected: Int?
    @SerializedName("numberOfRecommendations")
    val numberOfRecommendations: Int?
    @SerializedName("numberOfRequested")
    val numberOfRequested: Int?
    @SerializedName("numberOfSent")
    val numberOfSent: Int?
    @SerializedName("numberOfUnreadChats")
    val numberOfUnreadChats: Int?
    @SerializedName("numberOfUnreadEvents")
    val numberOfUnreadEvents: Int?
    @SerializedName("numberOfUnreadNeeds")
    val numberOfUnreadNeeds: Int?
    @SerializedName("numberOfUnreadNotifications")
    val numberOfUnreadNotifications: Int?
    @SerializedName("numberOfUnreadResponses")
    val numberOfUnreadResponses: Int?
    @SerializedName("visiblePoints")
    val visiblePoints: Int
    @SerializedName("location")
    val location: LocationDbEntity?
    @SerializedName("gender")
    val gender: String?

    constructor(id: String,
                avatar: String?,
                firstName: String?,
                lastName: String?,
                organizationName: String?,
                type: String,
                userName: String,
                abilitiesInternal: List<ShortAccountAbilityDbEntity>,
                createdAt: Long,
                createdAtDate: String?,
                firebaseUserId: String,
                interests: List<String>?,
                offers: List<String>?,
                points: Int?,
                totalIncome: Int?,
                totalOutcome: Int?,
                contactEmail: String?,
                showContactPhone: Boolean,
                showContactEmail: Boolean,
                numberOfCommunities: Int?,
                numberOfConnections: Int?,
                numberOfDisconnected: Int?,
                numberOfRecommendations: Int?,
                numberOfRequested: Int?,
                numberOfSent: Int?,
                numberOfUnreadChats: Int?,
                numberOfUnreadEvents: Int?,
                numberOfUnreadNeeds: Int?,
                numberOfUnreadNotifications: Int?,
                numberOfUnreadResponses: Int?,
                visiblePoints: Int,
                location: LocationDbEntity,
                gender: String
    ) : super(id, avatar, firstName, lastName, organizationName, type, userName, abilitiesInternal) {
        this.createdAt = createdAt
        this.createdAtDate = createdAtDate
        this.firebaseUserId = firebaseUserId
        this.interests = interests
        this.offers = offers
        this.points = points
        this.totalIncome = totalIncome
        this.totalOutcome = totalOutcome
        this.contactEmail = contactEmail
        this.showContactEmail = showContactEmail
        this.showContactPhone = showContactPhone
        this.numberOfCommunities = numberOfCommunities
        this.numberOfConnections = numberOfConnections
        this.numberOfDisconnected = numberOfDisconnected
        this.numberOfRecommendations = numberOfRecommendations
        this.numberOfRequested = numberOfRequested
        this.numberOfSent = numberOfSent
        this.numberOfUnreadChats = numberOfUnreadChats
        this.numberOfUnreadEvents = numberOfUnreadEvents
        this.numberOfUnreadNeeds = numberOfUnreadNeeds
        this.numberOfUnreadNotifications = numberOfUnreadNotifications
        this.numberOfUnreadResponses = numberOfUnreadResponses
        this.visiblePoints = visiblePoints
        this.location = location
        this.gender = gender
    }

}

internal data class LocationDbEntity(
        @SerializedName("placeId")
        val placeId: String?,
        @SerializedName("en")
        val en: LocationDetailDbEntity?,
        @SerializedName("ar")
        val ar: LocationDetailDbEntity?
)

internal data class LocationDetailDbEntity(
        @SerializedName("city")
        val city: String?,
        @SerializedName("lat")
        val lat: Double?,
        @SerializedName("lng")
        val lng: Double?,
        @SerializedName("placeId")
        val placeId: String?,
        @SerializedName("placeName")
        val placeName: String?
)
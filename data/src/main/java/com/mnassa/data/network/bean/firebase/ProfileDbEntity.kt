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
                totalOutcome: Int?

    ) : super(id, avatar, firstName, lastName, organizationName, type, userName, abilitiesInternal) {
        this.createdAt = createdAt
        this.createdAtDate = createdAtDate
        this.firebaseUserId = firebaseUserId
        this.interests = interests
        this.offers = offers
        this.points = points
        this.totalIncome = totalIncome
        this.totalOutcome = totalOutcome

    }

}
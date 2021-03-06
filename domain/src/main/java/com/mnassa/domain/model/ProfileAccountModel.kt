package com.mnassa.domain.model

import android.os.Parcelable
import java.util.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/23/2018
 */
interface ProfileAccountModel : ShortAccountModel, Parcelable {

    val createdAt: Long?
    val createdAtDate: String?
    val contactEmail: String?
    val showContactEmail: Boolean
    val showContactPhone: Boolean
    val interests: List<String>
    val offers: List<String>
    val points: Int?
    val totalIncome: Int?
    val totalOutcome: Int?
    val numberOfCommunities: Int?
    val numberOfConnections: Int?
    val numberOfDisconnected: Int?
    val numberOfRecommendations: Int?
    val numberOfRequested: Int?
    val numberOfSent: Int?
    val numberOfUnreadChats: Int?
    val numberOfUnreadEvents: Int?
    val numberOfUnreadNeeds: Int?
    val numberOfUnreadNotifications: Int?
    val numberOfUnreadResponses: Int?
    val visiblePoints: Int
    val location: LocationPlaceModel?
    val gender: Gender
    val website: String?
    val organizationType: String?
    val birthday: Date?

    companion object {
        const val DEFAULT_SHOW_CONTACT_EMAIL = true
        const val DEFAULT_SHOW_CONTACT_PHONE = true
        const val DEFAULT_VISIBLE_POINTS = 0
    }
}
package com.mnassa.domain.model.impl

import com.mnassa.domain.model.*
import kotlinx.android.parcel.Parcelize
import java.util.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/23/2018
 */
@Parcelize
data class ProfileAccountModelImpl(
        override val createdAt: Long?,
        override var id: String,
        override var serialNumber: Int?,
        override val createdAtDate: String?,
        override val interests: List<String>,
        override val offers: List<String>,
        override var userName: String,
        override val points: Int?,
        override var accountType: AccountType,
        override val totalIncome: Int?,
        override var avatar: String?,
        override val totalOutcome: Int?,
        override var contactPhone: String?,
        override var language: String?,
        override var personalInfo: PersonalAccountDiffModel?,
        override var organizationInfo: OrganizationAccountDiffModel?,
        override var abilities: List<AccountAbility>,
        override val contactEmail: String?,
        override val showContactEmail: Boolean,
        override val showContactPhone: Boolean,
        override val numberOfCommunities: Int?,
        override val numberOfConnections: Int?,
        override val numberOfDisconnected: Int?,
        override val numberOfRecommendations: Int?,
        override val numberOfRequested: Int?,
        override val numberOfSent: Int?,
        override val numberOfUnreadChats: Int?,
        override val numberOfUnreadEvents: Int?,
        override val numberOfUnreadNeeds: Int?,
        override val numberOfUnreadNotifications: Int?,
        override val numberOfUnreadResponses: Int?,
        override val visiblePoints: Int,
        override val location: LocationPlaceModel?,
        override val gender: Gender,
        override val website: String?,
        override val organizationType: String?,
        override var connectedBy: ConnectedByModel?,
        override val birthday: Date?
) : ProfileAccountModel
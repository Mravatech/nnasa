package com.mnassa.data.converter

import com.mnassa.core.converter.ConvertersContext
import com.mnassa.core.converter.ConvertersContextRegistrationCallback
import com.mnassa.core.converter.registerConverter
import com.mnassa.data.network.NetworkContract
import com.mnassa.data.network.bean.firebase.ConnectedByDbEntity
import com.mnassa.data.network.bean.firebase.LocationDbEntity
import com.mnassa.data.network.bean.firebase.ProfileDbEntity
import com.mnassa.domain.model.*
import com.mnassa.domain.model.impl.*
import com.mnassa.domain.other.LanguageProvider
import java.util.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/23/2018
 */

class ProfileConverter(private val languageProvider: LanguageProvider) : ConvertersContextRegistrationCallback {

    override fun register(convertersContext: ConvertersContext) {
        convertersContext.registerConverter(this::convertAccountFromDb)
        convertersContext.registerConverter(this::convertConnectedBy)
    }

    private fun convertLocationPlace(input: LocationDbEntity): LocationPlaceModelImpl {
        val city: TranslatedWordModel? =
                if (!input.en?.city.isNullOrBlank() || !input.ar?.city.isNullOrBlank()) {
                    TranslatedWordModelImpl(languageProvider, "", "", input.en?.city, input.ar?.city)
                } else null

        val placeName: TranslatedWordModel? =
                if (!input.en?.placeName.isNullOrBlank() || !input.ar?.placeName.isNullOrBlank()) {
                    TranslatedWordModelImpl(languageProvider, "", "", input.en?.placeName, input.ar?.placeName)
                } else null

        return LocationPlaceModelImpl(city = city, lat = input.en?.lat ?: 0.0, lng = input.en?.lng ?: 0.0, placeId = input.placeId, placeName = placeName)
    }

    private fun convertAccountFromDb(input: ProfileDbEntity, token: Any?, convertersContext: ConvertersContext): ProfileAccountModelImpl {

        var personalInfo: PersonalAccountDiffModel? = null
        var organizationInfo: OrganizationAccountDiffModel? = null
        val accountType: AccountType
        when (input.type) {
            NetworkContract.AccountType.ORGANIZATION -> {
                accountType = AccountType.ORGANIZATION
                organizationInfo = OrganizationAccountDiffModelImpl(
                        organizationName = requireNotNull(input.organizationName)
                )
            }
            NetworkContract.AccountType.PERSONAL -> {
                accountType = AccountType.PERSONAL
                personalInfo = PersonalAccountDiffModelImpl(
                        firstName = requireNotNull(input.firstName),
                        lastName = requireNotNull(input.lastName)
                )
            }
            else -> throw IllegalArgumentException("Illegal account type ${input.type}")
        }
        val location: LocationPlaceModel? = input.location?.let { convertLocationPlace(input.location) }
        val gender: Gender = if (input.gender == Gender.MALE.toString().toLowerCase()) Gender.MALE else Gender.FEMALE
        return ProfileAccountModelImpl(
                createdAt = input.createdAt,
                id = input.id,
                serialNumber = input.serialNumber,
                createdAtDate = input.createdAtDate,
                interests = input.interests?: emptyList(),
                offers = input.offers?: emptyList(),
                userName = input.userName ?: CONVERT_ERROR_MESSAGE,
                points = input.points,
                accountType = accountType,
                totalIncome = input.totalIncome,
                totalOutcome = input.totalOutcome,
                avatar = input.avatar,
                contactPhone = input.contactPhone,
                language = null,
                organizationInfo = organizationInfo,
                personalInfo = personalInfo,
                abilities = convertersContext.convertCollection(input.abilitiesInternal
                        ?: emptyList(), AccountAbility::class.java),
                contactEmail = input.contactEmail,
                showContactEmail = input.showContactEmail,
                showContactPhone = input.showContactPhone,
                numberOfCommunities = input.numberOfCommunities,
                numberOfConnections = input.numberOfConnections,
                numberOfDisconnected = input.numberOfDisconnected,
                numberOfRecommendations = input.numberOfRecommendations,
                numberOfRequested = input.numberOfRequested,
                numberOfSent = input.numberOfSent,
                numberOfUnreadChats = input.numberOfUnreadChats,
                numberOfUnreadEvents = input.numberOfUnreadEvents,
                numberOfUnreadNeeds = input.numberOfUnreadNeeds,
                numberOfUnreadNotifications = input.numberOfUnreadNotifications,
                numberOfUnreadResponses = input.numberOfUnreadResponses,
                visiblePoints = input.visiblePoints,
                location = location,
                gender = gender,
                website = input.website,
                organizationType = input.organizationType,
                connectedBy = input.connectedBy?.run { convertersContext.convert(this, ConnectedByModel::class.java) },
                birthday = input.birthday?.let { Date(it) }
        )
    }

    private fun convertConnectedBy(input: ConnectedByDbEntity): ConnectedByModelImpl {
        return ConnectedByModelImpl(
                id = input.id,
                type = input.type,
                value = input.value
        )
    }
}
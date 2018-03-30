package com.mnassa.data.converter

import com.androidkotlincore.entityconverter.ConvertersContext
import com.androidkotlincore.entityconverter.ConvertersContextRegistrationCallback
import com.androidkotlincore.entityconverter.registerConverter
import com.google.firebase.auth.FirebaseAuth
import com.mnassa.data.network.NetworkContract
import com.mnassa.data.network.bean.firebase.LocationDbEntity
import com.mnassa.data.network.bean.firebase.ProfileDbEntity
import com.mnassa.domain.model.*
import com.mnassa.domain.model.impl.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/23/2018
 */

class ProfileConverter : ConvertersContextRegistrationCallback {

    override fun register(convertersContext: ConvertersContext) {
        convertersContext.registerConverter(this::convertAccountFromDb)
        convertersContext.registerConverter(this::convertLocationPlace)
//        convertersContext.registerConverter(this::convertDetailLocation)
    }

//    private fun convertLocation(input: LocationDbEntity, token: Any?, convertersContext: ConvertersContext): LocationModel {
//        val locationDetailAr: LocationDetailModel? = input.ar?.let {
//            convertersContext.convert(it, LocationDetailModel::class.java)
//        }
//        val locationDetailEn: LocationDetailModel? = input.en?.let {
//            convertersContext.convert(it, LocationDetailModel::class.java)
//        }
//        return LocationModelImpl(input.placeId, locationDetailEn, locationDetailAr)
//    }

    private fun convertLocationPlace(input: LocationDbEntity): LocationPlaceModelImpl {
        val city: TranslatedWordModel? =
                if (!input.en?.city.isNullOrBlank() || !input.ar?.city.isNullOrBlank()) {
                    TranslatedWordModelImpl("", "", input.en?.city, input.ar?.city)
                } else null

        val placeName: TranslatedWordModel? =
                if (!input.en?.placeName.isNullOrBlank() || !input.ar?.placeName.isNullOrBlank()) {
                    TranslatedWordModelImpl("", "", input.en?.placeName, input.ar?.placeName)
                } else null

        return LocationPlaceModelImpl(city = city, lat = input.en?.lat ?: 0.0, lng = input.en?.lng ?: 0.0, placeId = input.placeId, placeName = placeName)
    }

//    private fun convertDetailLocation(input: LocationDetailDbEntity?, token: Any?, convertersContext: ConvertersContext): LocationDetailModel {
//        return LocationDetailModelImpl(input?.city, input?.lat, input?.lng, input?.placeId, input?.placeName)
//    }

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
                createdAtDate = input.createdAtDate,
                firebaseUserId = requireNotNull(FirebaseAuth.getInstance().uid),
                interests = input.interests?: emptyList(),
                offers = input.offers?: emptyList(),
                userName = input.userName,
                points = input.points,
                accountType = accountType,
                totalIncome = input.totalIncome,
                totalOutcome = input.totalOutcome,
                avatar = input.avatar,
                contactPhone = null,
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
                organizationType = input.organizationType
        )
    }
}
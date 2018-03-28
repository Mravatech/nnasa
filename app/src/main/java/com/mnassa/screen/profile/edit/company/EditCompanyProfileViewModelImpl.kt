package com.mnassa.screen.profile.edit.company

import android.net.Uri
import com.mnassa.domain.interactor.PlaceFinderInteractor
import com.mnassa.domain.interactor.StorageInteractor
import com.mnassa.domain.interactor.TagInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.*
import com.mnassa.domain.model.impl.ProfileCompanyInfoModelImpl
import com.mnassa.domain.model.impl.StoragePhotoDataImpl
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import timber.log.Timber

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/28/2018
 */
class EditCompanyProfileViewModelImpl(
        private val tagInteractor: TagInteractor,
        private val storageInteractor: StorageInteractor,
        private val placeFinderInteractor: PlaceFinderInteractor,
        private val userProfileInteractor: UserProfileInteractor) : MnassaViewModelImpl(), EditCompanyProfileViewModel {

    override val tagChannel: BroadcastChannel<EditCompanyProfileViewModel.TagCommand> = BroadcastChannel(10)
    override val imageUploadedChannel: BroadcastChannel<String> = BroadcastChannel(10)
    private var path: String? = null
    private var sendPhotoJob: Job? = null
    override fun uploadPhotoToStorage(uri: Uri) {
        sendPhotoJob?.cancel()
        sendPhotoJob = handleException {
            path = storageInteractor.sendAvatar(StoragePhotoDataImpl(uri, FOLDER_AVATARS))
            path?.let {
                imageUploadedChannel.send(it)
            }
            Timber.i(path)
        }
    }

    private var tagJob: Job? = null
    override fun getTagsByIds(ids: List<String>?, isOffers: Boolean) {
        val tagIds = ids ?: return
        tagJob = handleException {
            val tags = tagInteractor.getTagsByIds(tagIds)
            if (isOffers) {
                tagChannel.send(EditCompanyProfileViewModel.TagCommand.TagOffers(tags))
            } else {
                tagChannel.send(EditCompanyProfileViewModel.TagCommand.TagInterests(tags))
            }
        }
    }

    override suspend fun search(search: String): List<TagModel> {
        return tagInteractor.search(search)
    }

    override fun updateCompanyAccount(
            profileAccountModel: ProfileAccountModel,
            userName: String,
            showContactEmail: Boolean,
            contactEmail: String?,
            founded: Long?,
            organizationType: String?,
            website: String?,
            foundedDate: String?,
            locationId: String?,
            interests: List<TagModel>,
            offers: List<TagModel>
    ) {
        handleException {
            withProgressSuspend {
                val offersWithIds = getFilteredTags(offers)
                val interestsWithIds = getFilteredTags(interests)
                val profile = ProfileCompanyInfoModelImpl(
                        id = profileAccountModel.id,
                        firebaseUserId = profileAccountModel.firebaseUserId,
                        userName = userName,
                        accountType = AccountType.ORGANIZATION,
                        avatar = path,
                        contactPhone = profileAccountModel.contactPhone,
                        language = profileAccountModel.language,
                        personalInfo = profileAccountModel.personalInfo,
                        organizationInfo = profileAccountModel.organizationInfo,
                        abilities = profileAccountModel.abilities,
                        showContactEmail = showContactEmail,
                        showContactPhone = profileAccountModel.showContactPhone,
                        contactEmail = contactEmail,
                        founded = founded ?: profileAccountModel.createdAt,
                        organizationType = organizationType,
                        website = website,
                        foundedDate = foundedDate,
                        locationId = locationId,
                        interests = interestsWithIds,
                        offers = offersWithIds)
                userProfileInteractor.updateCompanyAccount(profile)
            }
        }
    }


    private suspend fun getFilteredTags(customTagsAndTagsWithIds: List<TagModel>): List<String> {
        val customTags = customTagsAndTagsWithIds.filter { it.id == null }.map { it.name }
        val existsTags = customTagsAndTagsWithIds.mapNotNull { it.id }
        val tags = arrayListOf<String>()
        if (customTags.isNotEmpty()) {
            val newTags = tagInteractor.createCustomTagIds(customTags)
            tags.addAll(newTags)
        }
        tags.addAll(existsTags)
        return tags
    }

    override fun getAutocomplete(constraint: CharSequence): List<GeoPlaceModel> {
        return placeFinderInteractor.getReqieredPlaces(constraint)
    }
}
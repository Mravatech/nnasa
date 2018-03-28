package com.mnassa.screen.profile.edit.personal

import android.net.Uri
import com.mnassa.domain.interactor.PlaceFinderInteractor
import com.mnassa.domain.interactor.StorageInteractor
import com.mnassa.domain.interactor.TagInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.*
import com.mnassa.domain.model.impl.ProfilePersonalInfoModelImpl
import com.mnassa.domain.model.impl.StoragePhotoDataImpl
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import timber.log.Timber

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/26/2018
 */
class EditPersonalProfileViewModelImpl(
        private val tagInteractor: TagInteractor,
        private val storageInteractor: StorageInteractor,
        private val placeFinderInteractor: PlaceFinderInteractor,
        private val userProfileInteractor: UserProfileInteractor) : MnassaViewModelImpl(), EditPersonalProfileViewModel {

    override val tagChannel: BroadcastChannel<EditPersonalProfileViewModel.TagCommand> = BroadcastChannel(10)
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
    override fun updatePersonalAccount(
            profileAccountModel: ProfileAccountModel,
            firstName: String,
            secondName: String,
            userName: String,
            showContactEmail: Boolean,
            contactEmail: String?,
            showContactPhone: Boolean,
            contactPhone: String?,
            birthday: Long?,
            birthdayDate: String?,
            locationId: String?,
            isMale: Boolean,
            abilities: List<AccountAbility>,
            interests: List<TagModel>,
            offers: List<TagModel>) {
        handleException {
            withProgressSuspend {
                val offersWithIds = getFilteredTags(offers)
                val interestsWithIds = getFilteredTags(interests)
                val profile = ProfilePersonalInfoModelImpl(
                        id = profileAccountModel.id,
                        firebaseUserId = profileAccountModel.firebaseUserId,
                        userName = userName,
                        accountType = AccountType.PERSONAL,
                        avatar = path,
                        contactPhone = contactPhone,
                        language = profileAccountModel.language,
                        personalInfo = profileAccountModel.personalInfo,
                        organizationInfo = profileAccountModel.organizationInfo,
                        abilities = abilities,
                        birthdayDate = birthdayDate,
                        showContactEmail = showContactEmail,
                        birthday = birthday,
                        showContactPhone = showContactPhone,
                        contactEmail = contactEmail,
                        gender = if (isMale) Gender.MALE else Gender.FEMALE,
                        locationId = locationId,
                        interests = interestsWithIds,
                        offers = offersWithIds
                )
                userProfileInteractor.updatePersonalAccount(profile)
            }
        }
    }

    private var tagJob: Job? = null
    override fun getTagsByIds(ids: List<String>?, isOffers: Boolean) {
        val tagIds = ids ?: return
        tagJob = handleException {
            val tags = tagInteractor.getTagsByIds(tagIds)
            if (isOffers) {
                tagChannel.send(EditPersonalProfileViewModel.TagCommand.TagOffers(tags))
            } else {
                tagChannel.send(EditPersonalProfileViewModel.TagCommand.TagInterests(tags))
            }
        }
    }

    override suspend fun search(search: String): List<TagModel> {
        return tagInteractor.search(search)
    }

    override fun getAutocomplete(constraint: CharSequence): List<GeoPlaceModel> {
        return placeFinderInteractor.getReqieredPlaces(constraint)
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
}
package com.mnassa.screen.profile.edit.personal

import android.net.Uri
import com.mnassa.domain.interactor.PlaceFinderInteractor
import com.mnassa.domain.interactor.StorageInteractor
import com.mnassa.domain.interactor.TagInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.*
import com.mnassa.domain.model.impl.PersonalAccountDiffModelImpl
import com.mnassa.domain.model.impl.ProfilePersonalInfoModelImpl
import com.mnassa.domain.model.impl.StoragePhotoDataImpl
import com.mnassa.screen.profile.edit.BaseEditableProfileViewModelImpl
import kotlinx.coroutines.experimental.channels.ArrayBroadcastChannel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/26/2018
 */
class EditPersonalProfileViewModelImpl(
        private val tagInteractor: TagInteractor,
        private val storageInteractor: StorageInteractor,
        private val placeFinderInteractor: PlaceFinderInteractor,
        private val userProfileInteractor: UserProfileInteractor) : BaseEditableProfileViewModelImpl(tagInteractor), EditPersonalProfileViewModel {

    override val openScreenChannel: ArrayBroadcastChannel<EditPersonalProfileViewModel.PersonalScreenCommander> = ArrayBroadcastChannel(10)
    private var avatarSavedPath: String? = null
    private var avatarUri: Uri? = null
    override fun saveLocallyAvatarUri(uri: Uri) {
        this.avatarUri = uri
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
                avatarSavedPath = avatarUri?.let { storageInteractor.sendImage(StoragePhotoDataImpl(it, FOLDER_AVATARS)) }
                val profile = ProfilePersonalInfoModelImpl(
                        id = profileAccountModel.id,
                        userName = userName,
                        accountType = AccountType.PERSONAL,
                        avatar = avatarSavedPath,
                        contactPhone = contactPhone,
                        language = profileAccountModel.language,
                        personalInfo = PersonalAccountDiffModelImpl(firstName, secondName),
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
                        offers = offersWithIds,
                        connectedBy = null
                )
                userProfileInteractor.updatePersonalAccount(profile)
                openScreenChannel.send(EditPersonalProfileViewModel.PersonalScreenCommander.ClosePersonalEditScreen())
            }
        }
    }

    override suspend fun search(search: String): List<TagModel> {
        return tagInteractor.search(search)
    }

    override fun getAutocomplete(constraint: CharSequence): List<GeoPlaceModel> {
        return placeFinderInteractor.getReqieredPlaces(constraint)
    }

}
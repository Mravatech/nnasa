package com.mnassa.screen.profile.edit.company

import android.net.Uri
import com.mnassa.core.addons.launchWorker
import com.mnassa.domain.interactor.PlaceFinderInteractor
import com.mnassa.domain.interactor.StorageInteractor
import com.mnassa.domain.interactor.TagInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.*
import com.mnassa.domain.model.impl.OrganizationAccountDiffModelImpl
import com.mnassa.domain.model.impl.ProfileCompanyInfoModelImpl
import com.mnassa.domain.model.impl.StoragePhotoDataImpl
import com.mnassa.screen.profile.edit.BaseEditableProfileViewModelImpl
import kotlinx.coroutines.channels.BroadcastChannel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/28/2018
 */
class EditCompanyProfileViewModelImpl(
        private val tagInteractor: TagInteractor,
        private val storageInteractor: StorageInteractor,
        private val placeFinderInteractor: PlaceFinderInteractor,
        private val userProfileInteractor: UserProfileInteractor) : BaseEditableProfileViewModelImpl(tagInteractor), EditCompanyProfileViewModel {

    override val openScreenChannel: BroadcastChannel<EditCompanyProfileViewModel.CompanyScreenCommander> = BroadcastChannel(10)
    private var avatarSavedPath: String? = null
    private var avatarUri: Uri? = null
    override fun saveLocallyAvatarUri(uri: Uri) {
        this.avatarUri = uri
    }

    override fun updateCompanyAccount(
            profileAccountModel: ProfileAccountModel,
            userName: String,
            companyName: String,
            showContactEmail: Boolean,
            showContactPhone: Boolean,
            contactEmail: String?,
            contactPhone: String?,
            founded: Long?,
            organizationType: String?,
            website: String?,
            foundedDate: String?,
            locationId: String?,
            interests: List<TagModel>,
            offers: List<TagModel>
    ) {
        launchWorker {
            withProgressSuspend {
                val offersWithIds = getFilteredTags(offers)
                val interestsWithIds = getFilteredTags(interests)
                avatarSavedPath = avatarUri?.let { storageInteractor.sendImage(StoragePhotoDataImpl(it, FOLDER_AVATARS)) }

                val profile = ProfileCompanyInfoModelImpl(
                        id = profileAccountModel.id,
                        serialNumber = profileAccountModel.serialNumber,
                        userName = userName,
                        accountType = AccountType.ORGANIZATION,
                        avatar = avatarSavedPath,
                        contactPhone = contactPhone,
                        language = profileAccountModel.language,
                        personalInfo = profileAccountModel.personalInfo,
                        organizationInfo = OrganizationAccountDiffModelImpl(companyName),
                        abilities = profileAccountModel.abilities,
                        showContactEmail = showContactEmail,
                        showContactPhone = showContactPhone,
                        contactEmail = contactEmail,
                        founded = founded ?: profileAccountModel.birthday?.time,
                        organizationType = organizationType,
                        website = website,
                        foundedDate = foundedDate,
                        locationId = locationId,
                        interests = interestsWithIds,
                        offers = offersWithIds,
                        connectedBy = null)
                userProfileInteractor.updateCompanyAccount(profile)
                openScreenChannel.send(EditCompanyProfileViewModel.CompanyScreenCommander.CloseCompanyEditScreen())
            }
        }
    }

    override fun getAutocomplete(constraint: CharSequence): List<GeoPlaceModel> {
        return placeFinderInteractor.getReqieredPlaces(constraint)
    }
}
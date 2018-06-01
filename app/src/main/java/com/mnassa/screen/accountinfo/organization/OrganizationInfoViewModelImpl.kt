package com.mnassa.screen.accountinfo.organization

import android.net.Uri
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.interactor.StorageInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.FOLDER_AVATARS
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.impl.CompanyInfoModelImpl
import com.mnassa.domain.model.impl.OrganizationAccountDiffModelImpl
import com.mnassa.domain.model.impl.StoragePhotoDataImpl
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.ArrayBroadcastChannel

/**
 * Created by Peter on 2/28/2018.
 */
class OrganizationInfoViewModelImpl(
        private val storageInteractor: StorageInteractor,
        private val userProfileInteractor: UserProfileInteractor) : MnassaViewModelImpl(), OrganizationInfoViewModel {

    override val openScreenChannel: ArrayBroadcastChannel<OrganizationInfoViewModel.OpenScreenCommand> = ArrayBroadcastChannel(10)

    private var avatarSavedPath: String? = null
    private var avatarUri: Uri? = null
    override fun saveLocallyAvatarUri(uri: Uri) {
        this.avatarUri = uri
    }

    override fun skipThisStep() {
        launchCoroutineUI {
            openScreenChannel.send(OrganizationInfoViewModel.OpenScreenCommand.InviteScreen())
        }
    }

    private var processAccountJob: Job? = null
    override fun processAccount(
            accountModel: ShortAccountModel,
            organizationType: String?,
            foundedDate: String?,
            showContactEmail: Boolean?,
            showContactPhone: Boolean?,
            founded: Long?,
            contactEmail: String?,
            contactPhone: String?,
            website: String?) {
        processAccountJob?.cancel()
        processAccountJob = handleException {
            withProgressSuspend {
                avatarSavedPath = avatarUri?.let { storageInteractor.sendImage(StoragePhotoDataImpl(it, FOLDER_AVATARS)) }
                val companyInfo = CompanyInfoModelImpl(
                        id = accountModel.id,
                        userName = accountModel.userName,
                        accountType = accountModel.accountType,
                        avatar = avatarSavedPath,
                        contactPhone = contactPhone,
                        language = accountModel.language,
                        personalInfo = accountModel.personalInfo,
                        organizationInfo = OrganizationAccountDiffModelImpl(requireNotNull(accountModel.organizationInfo).organizationName),
                        abilities = emptyList(),
                        showContactEmail = showContactEmail,
                        showContactPhone = showContactPhone,
                        contactEmail = contactEmail,
                        founded = founded,
                        organizationType = organizationType,
                        website = website,
                        foundedDate = foundedDate,
                        connectedBy = null
                )
                userProfileInteractor.processAccount(companyInfo)
                openScreenChannel.send(OrganizationInfoViewModel.OpenScreenCommand.InviteScreen())
            }
        }

    }
}
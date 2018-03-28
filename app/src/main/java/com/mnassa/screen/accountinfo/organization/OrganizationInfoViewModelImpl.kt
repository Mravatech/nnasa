package com.mnassa.screen.accountinfo.organization

import android.net.Uri
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
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import timber.log.Timber

/**
 * Created by Peter on 2/28/2018.
 */
class OrganizationInfoViewModelImpl(
        private val storageInteractor: StorageInteractor,
        private val userProfileInteractor: UserProfileInteractor) : MnassaViewModelImpl(), OrganizationInfoViewModel {

    override val imageUploadedChannel: BroadcastChannel<String> = BroadcastChannel(10)
    override val openScreenChannel: ArrayBroadcastChannel<OrganizationInfoViewModel.OpenScreenCommand> = ArrayBroadcastChannel(10)
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

    private var processAccountJob: Job? = null
    override fun processAccount(
            accountModel: ShortAccountModel,
            organizationType: String?,
            foundedDate: String?,
            showContactEmail: Boolean?,
            founded: Long?,
            contactEmail: String?,
            organizationName: String,
            website: String?) {
        processAccountJob?.cancel()
        processAccountJob = handleException {
            withProgressSuspend {
                val companyInfo = CompanyInfoModelImpl(
                        id = accountModel.id,
                        firebaseUserId = accountModel.firebaseUserId,
                        userName = accountModel.userName,
                        accountType = accountModel.accountType,
                        avatar = path,
                        contactPhone = accountModel.contactPhone,
                        language = accountModel.language,
                        personalInfo = accountModel.personalInfo,
                        organizationInfo = OrganizationAccountDiffModelImpl(organizationName),
                        abilities = emptyList(),
                        showContactEmail = showContactEmail,
                        showContactPhone = true,//todo handle
                        contactEmail = contactEmail,
                        founded = founded,
                        organizationType = organizationType,
                        website = website,
                        foundedDate = foundedDate
                )
                userProfileInteractor.processAccount(companyInfo)
                openScreenChannel.send(OrganizationInfoViewModel.OpenScreenCommand.InviteScreen())
            }
        }

    }
}
package com.mnassa.screen.accountinfo.personal

import android.net.Uri
import com.mnassa.core.addons.launchWorker
import com.mnassa.domain.interactor.StorageInteractor
import com.mnassa.domain.interactor.TagInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.AccountAbility
import com.mnassa.domain.model.FOLDER_AVATARS
import com.mnassa.domain.model.Gender
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.impl.PersonalInfoModelImpl
import com.mnassa.domain.model.impl.StoragePhotoDataImpl
import com.mnassa.exceptions.resolveExceptions
import com.mnassa.screen.profile.edit.BaseEditableProfileViewModelImpl
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BroadcastChannel

class PersonalInfoViewModelImpl(private val storageInteractor: StorageInteractor,
                                private val userProfileInteractor: UserProfileInteractor,
                                tagInteractor: TagInteractor) : BaseEditableProfileViewModelImpl(tagInteractor), PersonalInfoViewModel {

    override val openScreenChannel: BroadcastChannel<PersonalInfoViewModel.OpenScreenCommand> = BroadcastChannel(10)

    private var avatarSavedPath: String? = null
    private var avatarUri: Uri? = null
    override fun saveLocallyAvatarUri(uri: Uri) {
        this.avatarUri = uri
    }

    override fun skipThisStep() {
        launchWorker {
            openScreenChannel.send(PersonalInfoViewModel.OpenScreenCommand.InviteScreen())
        }
    }

    private var processAccountJob: Job? = null
    override fun processAccount(accountModel: ShortAccountModel,
                                contactPhone: String,
                                abilities: List<AccountAbility>,
                                birthdayDate: String,
                                showContactEmail: Boolean?,
                                birthday: Long?,
                                showContactPhone: Boolean?,
                                contactEmail: String,
                                isMale: Boolean
    ) {
        processAccountJob?.cancel()
        processAccountJob = launchWorker {
            withProgressSuspend {
                avatarSavedPath = avatarUri?.let { storageInteractor.sendImage(StoragePhotoDataImpl(it, FOLDER_AVATARS)) }
                val personalInfo = PersonalInfoModelImpl(
                        id = accountModel.id,
                        serialNumber = accountModel.serialNumber,
                        userName = accountModel.userName,
                        accountType = accountModel.accountType,
                        avatar = avatarSavedPath,
                        contactPhone = contactPhone.takeIf { it.isNotBlank() },
                        language = accountModel.language,
                        personalInfo = accountModel.personalInfo,
                        organizationInfo = accountModel.organizationInfo,
                        abilities = abilities,
                        birthdayDate = birthdayDate.takeIf { it.isNotBlank() },
                        showContactEmail = showContactEmail,
                        birthday = birthday,
                        showContactPhone = showContactPhone,
                        contactEmail = contactEmail.takeIf { it.isNotBlank() },
                        gender = if (isMale) Gender.MALE else Gender.FEMALE,
                        connectedBy = null
                )
                userProfileInteractor.processAccount(personalInfo)
                openScreenChannel.send(PersonalInfoViewModel.OpenScreenCommand.InviteScreen())
            }
        }
    }

    companion object {
        const val EXTRA_PHOTO_PATH = "EXTRA_PHOTO_PATH"
    }
}
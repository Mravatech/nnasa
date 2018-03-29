package com.mnassa.screen.accountinfo.personal

import android.net.Uri
import android.os.Bundle
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.interactor.StorageInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.AccountAbility
import com.mnassa.domain.model.FOLDER_AVATARS
import com.mnassa.domain.model.Gender
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.impl.PersonalInfoModelImpl
import com.mnassa.domain.model.impl.StoragePhotoDataImpl
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.ArrayBroadcastChannel
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import timber.log.Timber

class PersonalInfoViewModelImpl(private val storageInteractor: StorageInteractor,
                                private val userProfileInteractor: UserProfileInteractor) : MnassaViewModelImpl(), PersonalInfoViewModel {

    override val imageUploadedChannel: BroadcastChannel<String> = BroadcastChannel(10)
    override val openScreenChannel: ArrayBroadcastChannel<PersonalInfoViewModel.OpenScreenCommand> = ArrayBroadcastChannel(10)
    private var path: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.apply {
            path = getString(EXTRA_PHOTO_PATH)
        }
    }

    override fun saveInstanceState(outBundle: Bundle) {
        super.saveInstanceState(outBundle)
        outBundle.putString(EXTRA_PHOTO_PATH, path)
    }

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

    override fun skipThisStep() {
        launchCoroutineUI {
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
        processAccountJob = handleException {
            withProgressSuspend {
                val personalInfo = PersonalInfoModelImpl(
                        accountModel.id,
                        accountModel.firebaseUserId,
                        accountModel.userName,
                        accountModel.accountType,
                        path,
                        contactPhone.takeIf { it.isNotBlank() },
                        accountModel.language,
                        accountModel.personalInfo,
                        accountModel.organizationInfo,
                        abilities,
                        birthdayDate.takeIf { it.isNotBlank() },
                        showContactEmail,
                        birthday,
                        showContactPhone,
                        contactEmail.takeIf { it.isNotBlank() },
                        if (isMale) Gender.MALE else Gender.FEMALE
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
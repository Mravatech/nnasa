package com.mnassa.screen.accountinfo.personal

import android.net.Uri
import android.os.Bundle
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.interactor.StorageInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.FOLDER_AVATARS
import com.mnassa.domain.model.MEDIUM_PHOTO_SIZE
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.impl.DownloadingPhotoDataImpl
import com.mnassa.domain.model.impl.UploadingPhotoDataImpl
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.ArrayBroadcastChannel
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import timber.log.Timber

/**
 * Created by Peter on 2/27/2018.
 */
class PersonalInfoViewModelImpl(private val storageInteractor: StorageInteractor,
                                private val storage: FirebaseStorage,
                                private val userProfileInteractor: UserProfileInteractor) : MnassaViewModelImpl(), PersonalInfoViewModel {

    override val imageUploadedChannel: BroadcastChannel<StorageReference> = BroadcastChannel(10)
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

    private var getPhotoJob: Job? = null
    override fun getPhotoFromStorage() {
        getPhotoJob?.cancel()
        getPhotoJob = launchCoroutineUI {
            try {
                val path = storageInteractor.getAvatar(DownloadingPhotoDataImpl(MEDIUM_PHOTO_SIZE, FOLDER_AVATARS))
                imageUploadedChannel.send(storage.getReferenceFromUrl(path))
                Timber.i(path)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    private var sendPhotoJob: Job? = null
    override fun sendPhotoToStorage(uri: Uri) {
        sendPhotoJob?.cancel()
        sendPhotoJob = handleException {
            path = storageInteractor.sendAvatar(UploadingPhotoDataImpl(uri, FOLDER_AVATARS))
            path?.let {
                imageUploadedChannel.send(storage.getReferenceFromUrl(it))
            }
            Timber.i(path)
        }
    }

    override fun processAccount(accountModel: ShortAccountModel) {
        launchCoroutineUI {
            //todo handle error
            try {
                userProfileInteractor.processAccount(accountModel, path)
                openScreenChannel.send(PersonalInfoViewModel.OpenScreenCommand.MainScreen())
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    companion object {
        const val EXTRA_PHOTO_PATH = "EXTRA_PHOTO_PATH"
    }

}
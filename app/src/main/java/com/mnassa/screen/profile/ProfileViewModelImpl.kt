package com.mnassa.screen.profile

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.interactor.StorageInteractor
import com.mnassa.domain.model.FOLDER_AVATARS
import com.mnassa.domain.model.MEDIUM_PHOTO_SIZE
import com.mnassa.domain.model.impl.DownloadingPhotoDataImpl
import com.mnassa.domain.model.impl.UploadingPhotoDataImpl
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import timber.log.Timber

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 2/26/2018
 */
class ProfileViewModelImpl(private val storageInteractor: StorageInteractor
                           , private val storage: FirebaseStorage) : MnassaViewModelImpl(), ProfileViewModel {

    override val imageUploadedChannel: BroadcastChannel<StorageReference> = BroadcastChannel(10)

    private var getPhotoJob: Job? = null
    override fun getPhotoFromStorage() {
        getPhotoJob?.cancel()
        getPhotoJob = launchCoroutineUI {
            try {
                //todo change return type from http to gs
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
        sendPhotoJob = launchCoroutineUI {
            try {
                val path = storageInteractor.sendAvatar(UploadingPhotoDataImpl(uri, FOLDER_AVATARS))
                imageUploadedChannel.send(storage.getReferenceFromUrl(path))
                Timber.i(path)
            } catch (e: Exception) {
                Timber.e(e)
                //todo handle error
            }

        }
    }
}
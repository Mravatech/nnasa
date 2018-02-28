package com.mnassa.screen.profile

import android.net.Uri
import android.os.Bundle
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.interactor.StorageInteractor
import com.mnassa.domain.model.FOLDER_AVATARS
import com.mnassa.domain.model.MEDIUM_PHOTO_SIZE
import com.mnassa.domain.model.impl.DownloadPhotoImpl
import com.mnassa.domain.model.impl.UploadPhotoImpl
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.ArrayBroadcastChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import timber.log.Timber

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 2/26/2018
 */
class ProfileViewModelImpl(private val storageInteractor: StorageInteractor) : MnassaViewModelImpl(), ProfileViewModel {

    override val imageUploadedChannel: ArrayBroadcastChannel<String> = ArrayBroadcastChannel(10)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    private var getPhotoJob: Job? = null
    override fun getPhotoFromStorage() {
        getPhotoJob?.cancel()
        getPhotoJob = launchCoroutineUI {
            storageInteractor.getAvatar(DownloadPhotoImpl(MEDIUM_PHOTO_SIZE, FOLDER_AVATARS)).consumeEach {
                imageUploadedChannel.send(it)
                Timber.i(it)
            }
        }
    }

    private var sendPhotoJob: Job? = null
    override fun sendPhotoToStorage(uri: Uri) {
        sendPhotoJob?.cancel()
        sendPhotoJob = launchCoroutineUI {
            storageInteractor.sendAvatar(UploadPhotoImpl(uri, FOLDER_AVATARS)).consumeEach {
                imageUploadedChannel.send(it)
                Timber.i(it)
            }
        }
    }
}
package com.mnassa.screen.accountinfo.personal

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
 * Created by Peter on 2/27/2018.
 */
class PersonalInfoViewModelImpl(private val storageInteractor: StorageInteractor) : MnassaViewModelImpl(), PersonalInfoViewModel {

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
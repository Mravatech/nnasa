package com.mnassa.screen.profile

import android.net.Uri
import android.os.Bundle
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.interactor.StorageInteractor
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

    private var sendJob: Job? = null
    override fun sendToStorage(uri: Uri) {
        sendJob?.cancel()
        // create interactor for sanding data to storage
        sendJob = launchCoroutineUI {
            storageInteractor.sendAvatar(uri).consumeEach {
                imageUploadedChannel.send(it)
                Timber.i(it)
            }
        }
    }

}
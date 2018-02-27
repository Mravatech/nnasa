package com.mnassa.screen.profile

import android.net.Uri
import android.os.Bundle
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.Job

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 2/26/2018
 */
class ProfileViewModelImpl : MnassaViewModelImpl(), ProfileViewModel {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }
    private var sendJob: Job? = null
    override fun sendToStorage(uri: Uri) {
        sendJob?.cancel()
       // create interactor for sanding data to storage
    }

}
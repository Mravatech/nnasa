package com.mnassa.screen.profile

import android.net.Uri
import android.os.Bundle
import com.mnassa.screen.base.MnassaViewModelImpl

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 2/26/2018
 */
class ProfileViewModelImpl : MnassaViewModelImpl(), ProfileViewModel {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun sendToStorage(uri: Uri) {
       // create interactor for sanding data to storage
    }

}
package com.mnassa.screen.profile

import android.net.Uri
import com.mnassa.screen.base.MnassaViewModel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 2/26/2018
 */
interface ProfileViewModel : MnassaViewModel {

    fun sendToStorage(uri: Uri)

}
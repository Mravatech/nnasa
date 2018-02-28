package com.mnassa.screen.profile

import android.net.Uri
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.ArrayBroadcastChannel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 2/26/2018
 */
interface ProfileViewModel : MnassaViewModel {
    val imageUploadedChannel: ArrayBroadcastChannel<String>
    fun sendPhotoToStorage(uri: Uri)
    fun getPhotoFromStorage()

}
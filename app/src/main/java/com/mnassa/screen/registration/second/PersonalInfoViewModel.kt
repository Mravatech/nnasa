package com.mnassa.screen.registration.second

import android.net.Uri
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.ArrayBroadcastChannel

/**
 * Created by Peter on 2/27/2018.
 */
interface PersonalInfoViewModel : MnassaViewModel {
    val imageUploadedChannel: ArrayBroadcastChannel<String>
    fun sendPhotoToStorage(uri: Uri)
    fun getPhotoFromStorage()

}
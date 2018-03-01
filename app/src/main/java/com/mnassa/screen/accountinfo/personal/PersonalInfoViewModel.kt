package com.mnassa.screen.accountinfo.personal

import android.net.Uri
import com.google.firebase.storage.StorageReference
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by Peter on 2/27/2018.
 */
interface PersonalInfoViewModel : MnassaViewModel {
    val imageUploadedChannel: BroadcastChannel<StorageReference>
    fun sendPhotoToStorage(uri: Uri)
    fun getPhotoFromStorage()

}
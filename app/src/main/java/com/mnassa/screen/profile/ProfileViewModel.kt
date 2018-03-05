package com.mnassa.screen.profile

import android.net.Uri
import com.google.firebase.storage.StorageReference
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 2/26/2018
 */
interface ProfileViewModel : MnassaViewModel {
    val imageUploadedChannel: BroadcastChannel<StorageReference>
    fun sendPhotoToStorage(uri: Uri)

}
package com.mnassa.screen.accountinfo.personal

import android.net.Uri
import com.google.firebase.storage.StorageReference
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.ArrayBroadcastChannel
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by Peter on 2/27/2018.
 */
interface PersonalInfoViewModel : MnassaViewModel {
    val imageUploadedChannel: BroadcastChannel<StorageReference>
    val openScreenChannel: ArrayBroadcastChannel<OpenScreenCommand>

    fun uploadPhotoToStorage(uri: Uri)
    fun processAccount(accountModel: ShortAccountModel)

    sealed class OpenScreenCommand {
        class MainScreen : OpenScreenCommand()
    }
}
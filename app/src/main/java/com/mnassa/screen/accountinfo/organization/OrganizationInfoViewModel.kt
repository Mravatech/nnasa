package com.mnassa.screen.accountinfo.organization

import android.net.Uri
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.ArrayBroadcastChannel
import kotlinx.coroutines.experimental.channels.BroadcastChannel

/**
 * Created by Peter on 2/28/2018.
 */
interface OrganizationInfoViewModel : MnassaViewModel {
    val imageUploadedChannel: BroadcastChannel<String>
    val openScreenChannel: ArrayBroadcastChannel<OrganizationInfoViewModel.OpenScreenCommand>
    fun uploadPhotoToStorage(uri: Uri)
    fun processAccount(accountModel: ShortAccountModel,
                       organizationType: String?,
                       foundedDate: String?,
                       showContactEmail: Boolean?,
                       founded: Long?,
                       contactEmail: String?,
                       website: String?
    )
    sealed class OpenScreenCommand {
        class InviteScreen : OpenScreenCommand()
    }
}
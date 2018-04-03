package com.mnassa.screen.accountinfo.organization

import android.net.Uri
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.experimental.channels.ArrayBroadcastChannel

/**
 * Created by Peter on 2/28/2018.
 */
interface OrganizationInfoViewModel : MnassaViewModel {
    val openScreenChannel: ArrayBroadcastChannel<OrganizationInfoViewModel.OpenScreenCommand>
    fun saveLocallyAvatarUri(uri: Uri)
    fun skipThisStep()
    fun processAccount(accountModel: ShortAccountModel,
                       organizationType: String?,
                       foundedDate: String?,
                       showContactEmail: Boolean?,
                       showContactPhone: Boolean?,
                       founded: Long?,
                       contactEmail: String?,
                       contactPhone: String?,
                       website: String?
    )
    sealed class OpenScreenCommand {
        class InviteScreen : OpenScreenCommand()
    }
}
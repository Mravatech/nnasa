package com.mnassa.screen.accountinfo.organization

import android.net.Uri
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.profile.edit.BaseEditableProfileViewModel
import kotlinx.coroutines.channels.BroadcastChannel

/**
 * Created by Peter on 2/28/2018.
 */
interface OrganizationInfoViewModel : BaseEditableProfileViewModel {
    val openScreenChannel: BroadcastChannel<OpenScreenCommand>
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
package com.mnassa.screen.accountinfo.personal

import android.net.Uri
import com.mnassa.domain.model.AccountAbility
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.profile.edit.BaseEditableProfileViewModel
import kotlinx.coroutines.channels.BroadcastChannel

/**
 * Created by Peter on 2/27/2018.
 */
interface PersonalInfoViewModel : BaseEditableProfileViewModel {
    val openScreenChannel: BroadcastChannel<OpenScreenCommand>
    fun skipThisStep()
    fun saveLocallyAvatarUri(uri: Uri)
    fun processAccount(accountModel: ShortAccountModel,
                       contactPhone: String,
                       abilities: List<AccountAbility>,
                       birthdayDate: String,
                       showContactEmail: Boolean?,
                       birthday: Long?,
                       showContactPhone: Boolean?,
                       contactEmail: String,
                       isMale: Boolean
    )

    sealed class OpenScreenCommand {
        class InviteScreen : OpenScreenCommand()
    }
}
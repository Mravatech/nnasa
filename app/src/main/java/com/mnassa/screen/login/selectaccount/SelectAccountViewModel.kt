package com.mnassa.screen.login.selectaccount

import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.channels.BroadcastChannel

/**
 * Created by Peter on 2/27/2018.
 */
interface SelectAccountViewModel : MnassaViewModel {
    val openScreenChannel: BroadcastChannel<OpenScreenCommand>
    val accountsListChannel: BroadcastChannel<List<ShortAccountModel>>

    fun selectAccount(account: ShortAccountModel)
    sealed class OpenScreenCommand {
        class MainScreen : OpenScreenCommand()
    }
}
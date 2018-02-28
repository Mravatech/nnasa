package com.mnassa.screen.login.selectaccount

import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.ArrayBroadcastChannel

/**
 * Created by Peter on 2/27/2018.
 */
class SelectAccountViewModelIImpl(private val userProfileInteractor: UserProfileInteractor) : MnassaViewModelImpl(), SelectAccountViewModel {

    override val openScreenChannel: ArrayBroadcastChannel<SelectAccountViewModel.OpenScreenCommand> = ArrayBroadcastChannel(10)

    override fun selectAccount(account: ShortAccountModel) {
        handleException {
            userProfileInteractor.setCurrentUserAccount(account)
            openScreenChannel.send(SelectAccountViewModel.OpenScreenCommand.MainScreen())
        }
    }

    override val showMessageChannel: ArrayBroadcastChannel<String> = ArrayBroadcastChannel(10)
}
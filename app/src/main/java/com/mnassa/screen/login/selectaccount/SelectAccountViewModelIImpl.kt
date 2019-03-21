package com.mnassa.screen.login.selectaccount

import com.mnassa.core.addons.launchWorker
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.consumeEach

/**
 * Created by Peter on 2/27/2018.
 */
class SelectAccountViewModelIImpl(private val userProfileInteractor: UserProfileInteractor) : MnassaViewModelImpl(), SelectAccountViewModel {

    override val openScreenChannel: BroadcastChannel<SelectAccountViewModel.OpenScreenCommand> = BroadcastChannel(10)
    override val accountsListChannel: ConflatedBroadcastChannel<List<ShortAccountModel>> = ConflatedBroadcastChannel()

    override fun onSetup(setupScope: CoroutineScope) {
        super.onSetup(setupScope)
        setupScope.launchWorker {
            userProfileInteractor.getAllAccounts().consumeEach { accounts ->
                accountsListChannel.send(accounts)

                if (accounts.size == 1) {
                    selectAccount(accounts.first())
                }
            }
        }
    }

    override fun selectAccount(account: ShortAccountModel) {
        launchWorker {
            withProgressSuspend {
                userProfileInteractor.setCurrentUserAccount(account)
                openScreenChannel.send(SelectAccountViewModel.OpenScreenCommand.MainScreen())
            }
        }
    }
}
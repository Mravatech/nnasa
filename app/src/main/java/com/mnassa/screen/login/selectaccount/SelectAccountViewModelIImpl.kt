package com.mnassa.screen.login.selectaccount

import android.os.Bundle
import com.mnassa.domain.interactor.LoginInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.ArrayBroadcastChannel
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel

/**
 * Created by Peter on 2/27/2018.
 */
class SelectAccountViewModelIImpl(private val userProfileInteractor: UserProfileInteractor, private val loginInteractor: LoginInteractor) : MnassaViewModelImpl(), SelectAccountViewModel {

    override val openScreenChannel: ArrayBroadcastChannel<SelectAccountViewModel.OpenScreenCommand> = ArrayBroadcastChannel(10)
    override val accountsListChannel: ConflatedBroadcastChannel<List<ShortAccountModel>> = ConflatedBroadcastChannel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleException {
            val accounts = loginInteractor.getAccounts()
            accountsListChannel.send(accounts)

            if (accounts.size == 1) {
                selectAccount(accounts.first())
            }
        }
    }

    override fun selectAccount(account: ShortAccountModel) {
        handleException {
            withProgressSuspend {
                userProfileInteractor.setCurrentUserAccount(account)
            }
            openScreenChannel.send(SelectAccountViewModel.OpenScreenCommand.MainScreen())
        }
    }
}
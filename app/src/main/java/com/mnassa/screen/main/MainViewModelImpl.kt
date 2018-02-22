package com.mnassa.screen.main

import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.interactor.LoginInteractor
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.RendezvousChannel

/**
 * Created by Peter on 2/21/2018.
 */
class MainViewModelImpl(private val loginInteractor: LoginInteractor) : MnassaViewModelImpl(), MainViewModel {
    override val openScreenChannel: RendezvousChannel<MainViewModel.ScreenType> = RendezvousChannel()

    override fun logout() {
        launchCoroutineUI {
            loginInteractor.signOut()
            openScreenChannel.send(MainViewModel.ScreenType.LOGIN)
        }
    }
}
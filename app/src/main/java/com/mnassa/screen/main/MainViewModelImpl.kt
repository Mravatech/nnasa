package com.mnassa.screen.main

import android.os.Bundle
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.interactor.LoginInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.RendezvousChannel

/**
 * Created by Peter on 2/21/2018.
 */
class MainViewModelImpl(
        private val loginInteractor: LoginInteractor,
        private val userProfileInteractor: UserProfileInteractor) : MnassaViewModelImpl(), MainViewModel {
    override val openScreenChannel: RendezvousChannel<MainViewModel.ScreenType> = RendezvousChannel()
    override val userName: ConflatedBroadcastChannel<String> = ConflatedBroadcastChannel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        launchCoroutineUI {
            val profile = userProfileInteractor.getProfile()
            userName.send(profile.name)
        }
    }

    override fun logout() {
        launchCoroutineUI {
            try {
                loginInteractor.signOut()
                openScreenChannel.send(MainViewModel.ScreenType.LOGIN)
            } catch (e: Exception) {

            }
        }
    }
}
package com.mnassa.screen.main

import android.os.Bundle
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.interactor.LoginInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.ArrayBroadcastChannel
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import timber.log.Timber

/**
 * Created by Peter on 2/21/2018.
 */
class MainViewModelImpl(
        private val loginInteractor: LoginInteractor,
        private val userProfileInteractor: UserProfileInteractor) : MnassaViewModelImpl(), MainViewModel {
    override val openScreenChannel: ArrayBroadcastChannel<MainViewModel.ScreenType> = ArrayBroadcastChannel(10)
    override val userName: ConflatedBroadcastChannel<String> = ConflatedBroadcastChannel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        launchCoroutineUI {
            try {
                val profile = userProfileInteractor.getProfile()
                userName.send(profile.userName)
            } catch (e: Exception) {
                Timber.e(e)
            }
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
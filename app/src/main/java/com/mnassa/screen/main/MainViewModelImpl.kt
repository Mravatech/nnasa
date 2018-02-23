package com.mnassa.screen.main

import android.os.Bundle
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.interactor.LoginInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.repository.TagRepository
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.*
import kotlinx.coroutines.experimental.delay
import timber.log.Timber

/**
 * Created by Peter on 2/21/2018.
 */
class MainViewModelImpl(
        private val loginInteractor: LoginInteractor,
        private val userProfileInteractor: UserProfileInteractor,
        private val tagRepo: TagRepository) : MnassaViewModelImpl(), MainViewModel {
    override val openScreenChannel: RendezvousChannel<MainViewModel.ScreenType> = RendezvousChannel()
    override val userName: ConflatedBroadcastChannel<String> = ConflatedBroadcastChannel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        launchCoroutineUI {
            val profile = userProfileInteractor.getProfile()
            userName.send(profile.name)
        }

        launchCoroutineUI {
            tagRepo.load().consumeEachIndexed {
                Timber.e("TAGGG = ${it.index} -> ${it.value}")
                if (it.index % 10 == 0 && it.index != 0) delay(3_000)
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
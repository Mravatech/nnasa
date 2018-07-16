package com.mnassa.screen.splash

import android.os.Bundle
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.interactor.LoginInteractor
import com.mnassa.domain.repository.PostsRepository
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.ArrayBroadcastChannel
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.delay

/**
 * Created by Peter on 2/20/2018.
 */
class SplashViewModelImpl(private val loginInteractor: LoginInteractor,
                          private val postsRepository: PostsRepository) : MnassaViewModelImpl(), SplashViewModel {
    override val openNextScreenChannel: BroadcastChannel<SplashViewModel.NextScreen> = ArrayBroadcastChannel(1)
    override val showMessageChannel: ArrayBroadcastChannel<String> = ArrayBroadcastChannel(10)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        launchCoroutineUI {
            if (loginInteractor.isLoggedIn()) {
                postsRepository.preloadAllPosts()
                delay(SHORT_DELAY)
                //todo: await events etc
                openNextScreenChannel.send(SplashViewModel.NextScreen.MAIN)
            } else {
                delay(SHORT_DELAY)
                openNextScreenChannel.send(SplashViewModel.NextScreen.LOGIN)
            }
        }
    }

    companion object {
        private const val LONG_DELAY = 2_000
        private const val SHORT_DELAY = 200
    }
}
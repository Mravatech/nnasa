package com.mnassa.screen.splash

import android.os.Bundle
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.interactor.LoginInteractor
import com.mnassa.domain.interactor.NotificationInteractor
import com.mnassa.domain.interactor.PostsInteractor
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.ArrayBroadcastChannel
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.delay

/**
 * Created by Peter on 2/20/2018.
 */
class SplashViewModelImpl(private val loginInteractor: LoginInteractor,
                          private val postsRepository: PostsInteractor,
                          private val notificatonsRepository: NotificationInteractor) : MnassaViewModelImpl(), SplashViewModel {
    override val openNextScreenChannel: BroadcastChannel<SplashViewModel.NextScreen> = ArrayBroadcastChannel(1)
    override val showMessageChannel: ArrayBroadcastChannel<String> = ArrayBroadcastChannel(10)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        launchCoroutineUI {
            if (loginInteractor.isLoggedIn()) {
                val feed = async { postsRepository.preloadFeed() }
                val notifications = async { notificatonsRepository.preloadOldNotifications() }
                feed.await()
                notifications.await()

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
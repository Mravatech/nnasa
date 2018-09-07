package com.mnassa.screen.splash

import android.os.Bundle
import com.mnassa.core.addons.asyncUI
import com.mnassa.core.addons.asyncWorker
import com.mnassa.domain.interactor.LoginInteractor
import com.mnassa.domain.interactor.NotificationInteractor
import com.mnassa.domain.interactor.PostsInteractor
import com.mnassa.domain.interactor.TagInteractor
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.ArrayBroadcastChannel
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.delay

/**
 * Created by Peter on 2/20/2018.
 */
class SplashViewModelImpl(private val loginInteractor: LoginInteractor,
                          private val postsInteractor: PostsInteractor,
                          private val notificationsInteractor: NotificationInteractor,
                          private val tagInteractor: TagInteractor) : MnassaViewModelImpl(), SplashViewModel {
    override val openNextScreenChannel: ConflatedBroadcastChannel<SplashViewModel.NextScreen> = ConflatedBroadcastChannel()
    override val showMessageChannel: ArrayBroadcastChannel<String> = ArrayBroadcastChannel(10)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleException {
            if (loginInteractor.isLoggedIn()) {
                try {
                    val feed = asyncWorker { postsInteractor.preloadFeed() }
                    val notifications = asyncWorker { notificationsInteractor.preloadOldNotifications() }
                    val tags = asyncWorker { tagInteractor.getAll() }
                    asyncUI {
                        delay(MAX_DELAY)
                        openNextScreenChannel.send(SplashViewModel.NextScreen.MAIN)
                    }
                    feed.await()
                    notifications.await()
                    tags.await()
                } finally {
                    openNextScreenChannel.send(SplashViewModel.NextScreen.MAIN)
                }
            } else {
                delay(SHORT_DELAY)
                openNextScreenChannel.send(SplashViewModel.NextScreen.LOGIN)
            }
        }
    }

    companion object {
        private const val MAX_DELAY = 6_000
        private const val SHORT_DELAY = 200
    }
}
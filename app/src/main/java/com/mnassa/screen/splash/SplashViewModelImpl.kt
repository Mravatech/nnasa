package com.mnassa.screen.splash

import android.os.Bundle
import com.mnassa.core.addons.asyncUI
import com.mnassa.core.addons.asyncWorker
import com.mnassa.domain.interactor.LoginInteractor
import com.mnassa.domain.interactor.NotificationInteractor
import com.mnassa.domain.interactor.PostsInteractor
import com.mnassa.domain.interactor.TagInteractor
import com.mnassa.exceptions.resolveExceptions
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.delay
import timber.log.Timber

/**
 * Created by Peter on 2/20/2018.
 */
class SplashViewModelImpl(private val loginInteractor: LoginInteractor,
                          private val postsInteractor: PostsInteractor,
                          private val notificationsInteractor: NotificationInteractor,
                          private val tagInteractor: TagInteractor) : MnassaViewModelImpl(), SplashViewModel {
    override val openNextScreenChannel: ConflatedBroadcastChannel<SplashViewModel.NextScreen> = ConflatedBroadcastChannel()
    override val showMessageChannel: BroadcastChannel<String> = BroadcastChannel(10)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        GlobalScope.asyncWorker {
            try {
                postsInteractor.getDefaultExpirationDays()
            } catch (e: Exception) {
                Timber.e("Failed to get default expiration days", e)
            }
        }

        resolveExceptions {
            if (loginInteractor.isLoggedIn()) {
                try {
                    val notifications = GlobalScope.asyncWorker { notificationsInteractor.preloadOldNotifications() }
                    val tags = GlobalScope.asyncWorker { tagInteractor.getAll() }
                    asyncUI {
                        delay(MAX_DELAY)
                        openNextScreenChannel.send(SplashViewModel.NextScreen.MAIN)
                    }
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
        private const val MAX_DELAY = 6_000L
        private const val SHORT_DELAY = 200L
    }
}
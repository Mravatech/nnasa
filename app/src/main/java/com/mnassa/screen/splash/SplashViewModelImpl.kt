package com.mnassa.screen.splash

import android.os.Bundle
import com.mnassa.core.addons.launchWorker
import com.mnassa.core.addons.launchWorkerNoExceptions
import com.mnassa.domain.interactor.LoginInteractor
import com.mnassa.domain.interactor.NotificationInteractor
import com.mnassa.domain.interactor.PostsInteractor
import com.mnassa.domain.interactor.TagInteractor
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.delay

/**
 * Created by Peter on 2/20/2018.
 */
class SplashViewModelImpl(private val loginInteractor: LoginInteractor,
                          private val postsInteractor: PostsInteractor,
                          private val notificationsInteractor: NotificationInteractor,
                          private val tagInteractor: TagInteractor) : MnassaViewModelImpl(), SplashViewModel {
    override val openNextScreenChannel: ConflatedBroadcastChannel<SplashViewModel.NextScreen> = ConflatedBroadcastChannel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        GlobalScope.launchWorkerNoExceptions {
            postsInteractor.getDefaultExpirationDays()
        }

        GlobalScope.launchWorkerNoExceptions {
            notificationsInteractor.preloadOldNotifications()
        }

        GlobalScope.launchWorkerNoExceptions {
            tagInteractor.getAll()
        }

        launchWorker {
            val screen = if (loginInteractor.isLoggedIn()) {
                delay(MAX_DELAY)
                SplashViewModel.NextScreen.MAIN
            } else {
                delay(MIN_DELAY)
                SplashViewModel.NextScreen.LOGIN
            }

            openNextScreenChannel.send(screen)
        }
    }

    companion object {
        private const val MAX_DELAY = 1_000L
        private const val MIN_DELAY = 200L
    }
}
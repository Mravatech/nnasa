package com.mnassa.screen.splash

import android.os.Bundle
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.interactor.LoginInteractor
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.*
import kotlinx.coroutines.experimental.delay
import timber.log.Timber

/**
 * Created by Peter on 2/20/2018.
 */
class SplashViewModelImpl(private val loginInteractor: LoginInteractor) : MnassaViewModelImpl(), SplashViewModel {
    override val progressChannel: ConflatedBroadcastChannel<Int> = ConflatedBroadcastChannel()
    override val showMessageChannel: RendezvousChannel<String> = RendezvousChannel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.e("TEST VM onCreated")

        val from = savedInstanceState?.getInt(EXTRA_NUMBER, 100) ?: 100

        launchCoroutineUI {
            (from downTo 0).forEach {
                Timber.e("TEST VM countdown: $it")

                progressChannel.send(it)
                delay(50L)
            }
        }

        launchCoroutineUI {
            (0 .. 100).forEach {
                showMessageChannel.send(it.toString())
            }
        }
    }

    override suspend fun isLoggedIn(): Boolean = loginInteractor.isLoggedIn()

    override fun saveInstanceState(outBundle: Bundle) {
        super.saveInstanceState(outBundle)
        progressChannel.valueOrNull?.let { outBundle.putInt(EXTRA_NUMBER, it) }
    }

    override fun onCleared() {
        super.onCleared()
        Timber.e("TEST VM onCleared()")
    }

    init {
        Timber.e("TEST VM Constructor")
    }

    companion object {
        private const val EXTRA_NUMBER = "EXTRA_NUMBER"
    }
}
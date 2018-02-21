package com.mnassa.screen.splash

import android.os.Bundle
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.delay
import timber.log.Timber

/**
 * Created by Peter on 2/20/2018.
 */
class SplashViewModelImpl : MnassaViewModelImpl, SplashViewModel {
    override val countDown: ConflatedBroadcastChannel<Int> = ConflatedBroadcastChannel()

    constructor() {
        Timber.e("TEST VM Constructor")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.e("TEST VM onCreated")

        val from = savedInstanceState?.getInt(EXTRA_NUMBER, 100) ?: 100

        launchCoroutineUI {
            (from downTo 0).forEach {
                Timber.e("TEST VM countdown: $it")

                countDown.send(it)
                delay(5000L)
            }
        }
    }

    override fun saveInstanceState(outBundle: Bundle) {
        super.saveInstanceState(outBundle)
        countDown.valueOrNull?.let { outBundle.putInt(EXTRA_NUMBER, it) }
    }

    override fun onCleared() {
        super.onCleared()
        Timber.e("TEST VM onCleared()")
    }

    companion object {
        private const val EXTRA_NUMBER = "EXTRA_NUMBER"
    }
}
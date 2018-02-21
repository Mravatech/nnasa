package com.mnassa.screen.splash

import android.view.View
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.screen.base.MnassaControllerImpl
import kotlinx.android.synthetic.main.controller_splash.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import timber.log.Timber

/**
 * Created by Peter on 2/20/2018.
 */
class SplashController : MnassaControllerImpl<SplashViewModel>() {
    override val layoutId: Int = R.layout.controller_splash
    override val viewModel: SplashViewModel by instance()

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        Timber.e("TEST Controller onViewCreated")

        val job = launchCoroutineUI {
            viewModel.countDown.consumeEach {
                view.tvTimer.text = it.toString()
            }
        }
        job.invokeOnCompletion {
            Timber.e("TEST Controller invokeOnCompletion")
        }
    }

    companion object {
        fun newInstance() = SplashController()
    }

    init {
        Timber.e("TEST Controller constructor")
    }
}
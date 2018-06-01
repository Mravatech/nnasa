package com.mnassa.screen.splash

import android.view.View
import org.kodein.di.generic.instance
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.login.enterphone.EnterPhoneController
import com.mnassa.screen.main.MainController
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by Peter on 2/20/2018.
 */
class SplashController : MnassaControllerImpl<SplashViewModel>() {
    override val layoutId: Int = R.layout.controller_splash
    override val viewModel: SplashViewModel by instance()

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        launchCoroutineUI {
            viewModel.progressChannel.consumeEach {
                if (it == 0) {
                    openNextScreen()
                }
            }
        }
    }

    private suspend fun openNextScreen() {
        val nextScreen = when {
            viewModel.isLoggedIn() -> MainController.newInstance()
            else -> EnterPhoneController.newInstance()
        }
        open(nextScreen)
    }

    companion object {
        fun newInstance() = SplashController()
    }
}
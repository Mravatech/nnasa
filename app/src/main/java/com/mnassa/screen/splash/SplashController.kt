package com.mnassa.screen.splash

import android.view.View
import com.bluelinelabs.conductor.RouterTransaction
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.data.service.FirebaseLoginServiceImpl
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.invite.InviteController
import com.mnassa.screen.login.entercode.EnterCodeController
import com.mnassa.screen.login.enterphone.EnterPhoneController
import com.mnassa.screen.main.MainController
import com.mnassa.screen.registration.RegistrationController
import kotlinx.android.synthetic.main.controller_splash.view.*
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
                view.tvTimer.text = it.toString()
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
package com.mnassa.screen.login

import android.view.View
import com.bluelinelabs.conductor.RouterTransaction
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.main.MainController
import kotlinx.android.synthetic.main.controller_login.view.*
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by Peter on 2/21/2018.
 */
class LoginController : MnassaControllerImpl<LoginViewModel>() {
    override val layoutId: Int = R.layout.controller_login
    override val viewModel: LoginViewModel by instance()

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        view.btnRequestCode.setOnClickListener {
            viewModel.requestVerificationCode(view.etPhoneNumber.text.toString())
        }

        view.btnLogin.setOnClickListener {
            viewModel.login(view.etSmsCode.text.toString())
        }

        launchCoroutineUI {
            viewModel.openScreenChannel.consumeEach {
                when (it) {
                    LoginViewModel.ScreenType.MAIN -> {
                        router.replaceTopController(RouterTransaction.with(MainController.newInstance()))
                    }
                }
            }
        }
    }

    companion object {
        fun newInstance() = LoginController()
    }
}
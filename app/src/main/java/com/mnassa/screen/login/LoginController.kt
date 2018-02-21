package com.mnassa.screen.login

import android.view.View
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.screen.base.MnassaControllerImpl

/**
 * Created by Peter on 2/21/2018.
 */
class LoginController : MnassaControllerImpl<LoginViewModel>() {
    override val layoutId: Int = R.layout.controller_login
    override val viewModel: LoginViewModel by instance()

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
    }

    companion object {
        fun newInstance() = LoginController()
    }
}
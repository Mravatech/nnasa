package com.mnassa.screen.login

import android.view.View
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.screen.base.MnassaControllerImpl
import kotlinx.android.synthetic.main.controller_login.view.*

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
    }

    companion object {
        fun newInstance() = LoginController()
    }
}
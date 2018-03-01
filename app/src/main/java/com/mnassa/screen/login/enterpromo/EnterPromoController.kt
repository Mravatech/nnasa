package com.mnassa.screen.login.enterpromo

import android.view.View
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.other.fromDictionary
import com.mnassa.screen.login.enterphone.EnterPhoneController
import kotlinx.android.synthetic.main.code_input.view.*
import kotlinx.android.synthetic.main.controller_enter_phone.view.*

/**
 * Created by Peter on 3/1/2018.
 */
class EnterPromoController : EnterPhoneController() {
    override val viewModel: EnterPromoViewModel by instance()

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        with(view) {
            orLayout.visibility = View.GONE
            btnEnterPromo.visibility = View.GONE
            coneInput.visibility = View.VISIBLE

            etValidationCode.hint = fromDictionary(R.string.login_your_code)

            btnVerifyMe.setOnClickListener {
                viewModel.requestVerificationCode(phoneNumber, etValidationCode.text.toString())
            }

            registrationProgress.progress = 45
        }
    }

    companion object {
        fun newInstance() = EnterPromoController()
    }
}
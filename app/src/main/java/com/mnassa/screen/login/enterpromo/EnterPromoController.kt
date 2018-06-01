package com.mnassa.screen.login.enterpromo

import android.os.Bundle
import android.view.View
import org.kodein.di.generic.instance
import com.mnassa.R
import com.mnassa.extensions.SimpleTextWatcher
import com.mnassa.screen.login.RegistrationFlowProgress
import com.mnassa.screen.login.enterphone.EnterPhoneController
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.promo_code_input.view.*
import kotlinx.android.synthetic.main.controller_enter_phone.view.*
import kotlinx.android.synthetic.main.phone_input.view.*
import kotlinx.android.synthetic.main.header_login.view.*

/**
 * Created by Peter on 3/1/2018.
 */
class EnterPromoController(args: Bundle) : EnterPhoneController(args) {
    override val viewModel: EnterPromoViewModel by instance()

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        with(view) {
            pbRegistration.progress = RegistrationFlowProgress.ENTER_PROMO_CODE
            pbRegistration.visibility = View.VISIBLE

            orLayout.visibility = View.GONE
            btnEnterPromo.visibility = View.GONE
            coneInput.visibility = View.VISIBLE

            etPromoCode.hint = fromDictionary(R.string.login_your_code)
            etPromoCode.addTextChangedListener(SimpleTextWatcher { onInputChanged() })

            btnVerifyMe.setOnClickListener {
                viewModel.requestVerificationCode(phoneNumber, etPromoCode.text.toString())
            }

            if (args.containsKey(EXTRA_COUNTRY_INDEX)) {
                spinnerPhoneCode.setSelection(args.getInt(EXTRA_COUNTRY_INDEX), false)
                args.remove(EXTRA_COUNTRY_INDEX)
            }

            if (args.containsKey(EXTRA_PHONE_TAIL)) {
                etPhoneNumberTail.setText(args.getString(EXTRA_PHONE_TAIL, null))
                etPhoneNumberTail.setSelection(etPhoneNumberTail.text.length)
                args.remove(EXTRA_PHONE_TAIL)
            }
        }
    }

    override fun validateInput(): Boolean {
        val promoCode = requireNotNull(view).etPromoCode.text.toString()
        val promoCodeLength = requireNotNull(resources).getInteger(R.integer.promo_code_length)

        return super.validateInput() && promoCode.length == promoCodeLength
    }

    companion object {
        private const val EXTRA_COUNTRY_INDEX = "EXTRA_COUNTRY_INDEX"
        private const val EXTRA_PHONE_TAIL = "EXTRA_PHONE_TAIL"

        fun newInstance(selectedCountryIndex: Int, phoneTail: String): EnterPromoController {
            val args = Bundle()
            args.putInt(EXTRA_COUNTRY_INDEX, selectedCountryIndex)
            args.putString(EXTRA_PHONE_TAIL, phoneTail)
            return EnterPromoController(args)
        }
    }
}
package com.mnassa.screen.login.enterphone

import android.support.design.widget.Snackbar
import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.Spannable
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.view.inputmethod.EditorInfo
import com.bluelinelabs.conductor.RouterTransaction
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.other.SimpleTextWatcher
import com.mnassa.other.fromDictionary
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.login.entercode.EnterCodeController
import com.mnassa.screen.login.selectaccount.SelectAccountController
import com.mnassa.screen.main.MainController
import com.mnassa.screen.registration.first.RegistrationController
import kotlinx.android.synthetic.main.controller_enter_phone.view.*
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by Peter on 2/21/2018.
 */
class EnterPhoneController : MnassaControllerImpl<EnterPhoneViewModel>() {
    override val layoutId: Int = R.layout.controller_enter_phone
    override val viewModel: EnterPhoneViewModel by instance()

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        with(view) {
            tvEnterPhoneNumber.text = fromDictionary(R.string.login_enter_phone_title)
            btnVerifyMe.text = fromDictionary(R.string.login_verify_me)
            ilPhoneNumber.hint = fromDictionary(R.string.login_your_phone)
            tvTermsAndConditions.text = fromDictionary(R.string.login_terms_part_1)

            val termsAndCond = fromDictionary(R.string.login_terms_part_2)
            val termsAndCondSpan = Spannable.Factory.getInstance().newSpannable(termsAndCond)
            termsAndCondSpan.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    Snackbar.make(view, "Terms and conditions!", Snackbar.LENGTH_SHORT).show()
                }
            }, 0, termsAndCond.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)


            tvTermsAndConditions.append(" ")
            tvTermsAndConditions.append(termsAndCondSpan)
            tvTermsAndConditions.movementMethod = LinkMovementMethod.getInstance()

            btnVerifyMe.setOnClickListener {
                viewModel.requestVerificationCode(etPhoneNumber.text.toString())
            }

            etPhoneNumber.addTextChangedListener(PhoneNumberFormattingTextWatcher())
            etPhoneNumber.addTextChangedListener(SimpleTextWatcher {
                btnVerifyMe.isEnabled = isPhoneValid(it)
                ilPhoneNumber.error = null
            })
            etPhoneNumber.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    btnVerifyMe.performClick()
                    true
                } else false
            }
            btnVerifyMe.isEnabled = isPhoneValid(etPhoneNumber.text.toString())
        }

        launchCoroutineUI {
            viewModel.openScreenChannel.consumeEach {
                when (it) {
                    is EnterPhoneViewModel.OpenScreenCommand.MainScreen -> {
                        router.replaceTopController(RouterTransaction.with(MainController.newInstance()))
                    }
                    is EnterPhoneViewModel.OpenScreenCommand.EnterVerificationCode -> {
                        router.pushController(RouterTransaction.with(EnterCodeController.newInstance(it.param)))
                    }
                    is EnterPhoneViewModel.OpenScreenCommand.Registration -> {
                        router.pushController(RouterTransaction.with(RegistrationController.newInstance()))
                    }
                    is EnterPhoneViewModel.OpenScreenCommand.SelectAccount -> {
                        router.pushController(RouterTransaction.with(SelectAccountController.newInstance(it.accounts)))
                    }
                }
            }
        }

        launchCoroutineUI {
            viewModel.errorMessageChannel.consumeEach {
                view.ilPhoneNumber.error = it
            }
        }
    }

    private fun isPhoneValid(phoneNumber: String): Boolean {
        return android.util.Patterns.PHONE.matcher(phoneNumber).matches()
    }

    companion object {
        fun newInstance() = EnterPhoneController()
    }
}
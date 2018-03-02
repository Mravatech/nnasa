package com.mnassa.screen.login.entercode

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.text.Spannable
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import com.bluelinelabs.conductor.RouterTransaction
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.PhoneVerificationModel
import com.mnassa.other.SimpleTextWatcher
import com.mnassa.other.fromDictionary
import com.mnassa.other.validators.onImeActionDone
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.login.selectaccount.SelectAccountController
import com.mnassa.screen.main.MainController
import com.mnassa.screen.registration.RegistrationController
import kotlinx.android.synthetic.main.code_input.view.*
import kotlinx.android.synthetic.main.controller_enter_code.view.*
import kotlinx.android.synthetic.main.header_login.view.*
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.delay
import java.util.concurrent.TimeUnit

/**
 * Created by Peter on 23.02.2018.
 */
class EnterCodeController(params: Bundle) : MnassaControllerImpl<EnterCodeViewModel>(params) {
    override val layoutId: Int = R.layout.controller_enter_code
    override val viewModel: EnterCodeViewModel by instance()
    private var resendCodeSecondCounter = RESEND_SMS_DELAY
    private val verificationResponse by lazy { args.getParcelable<PhoneVerificationModel>(EXTRA_VERIFICATION_CODE_RESPONSE)}

    override fun onCreated(savedInstanceState: Bundle?) {
        super.onCreated(savedInstanceState)
        if (savedInstanceState == null) {
            viewModel.verificationResponse = verificationResponse
        }
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        with(view) {
            tvScreenHeader.text = fromDictionary(R.string.login_validation_code_header)
            tvEnterValidationCode.text = fromDictionary(R.string.login_enter_code_title)
            etValidationCode.hint = fromDictionary(R.string.login_validation_code_hint)
            startResendCodeTimer(resendCodeSecondCounter)

            etValidationCode.addTextChangedListener(SimpleTextWatcher {
                onCodeChanged()
            })
            etValidationCode.onImeActionDone { onCodeChanged() }
        }

        launchCoroutineUI {
            viewModel.openScreenChannel.consumeEach {
                when (it) {
                    is EnterCodeViewModel.OpenScreenCommand.MainScreen -> {
                        router.popToRoot()
                        router.replaceTopController(RouterTransaction.with(MainController.newInstance()))
                    }
                    is EnterCodeViewModel.OpenScreenCommand.RegistrationScreen -> {
                        router.pushController(RouterTransaction.with(RegistrationController.newInstance()))
                    }
                    is EnterCodeViewModel.OpenScreenCommand.SelectAccount -> {
                        router.pushController(RouterTransaction.with(SelectAccountController.newInstance(it.accounts)))
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(EXTRA_RESEND_CODE_DELAY, resendCodeSecondCounter)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        resendCodeSecondCounter = savedInstanceState.getInt(EXTRA_RESEND_CODE_DELAY)
    }

    private fun onCodeChanged() {
        val code = view?.etValidationCode?.text?.toString() ?: return
        val validationCodeLength = resources!!.getInteger(R.integer.validation_code_length)
        if (code.length != validationCodeLength) return
        viewModel.verifyCode(code)
    }

    private var resendCodeTimerJob: Job? = null
    private fun startResendCodeTimer(secondsCounter: Int) {
        resendCodeSecondCounter = secondsCounter
        if (secondsCounter <= 0) {
            enableResendCodeButton()
        } else {
            resendCodeTimerJob?.cancel()
            resendCodeTimerJob = launchCoroutineUI {
                val v = view ?: return@launchCoroutineUI
                val text = fromDictionary(R.string.login_enter_code_resend_after).format(secondsCounter)
                v.tvResendCodeAfter.text = text
                delay(1, TimeUnit.SECONDS)
                startResendCodeTimer(secondsCounter - 1)
            }
        }
    }

    private fun enableResendCodeButton() {
        val v = view ?: return

        val span = Spannable.Factory.getInstance().newSpannable(fromDictionary(R.string.login_enter_code_resend_code))
        span.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                viewModel.resendCode()
                startResendCodeTimer(RESEND_SMS_DELAY)
            }
        }, 0, span.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        v.tvResendCodeAfter.text = span
        v.tvResendCodeAfter.movementMethod = LinkMovementMethod.getInstance()
    }

    companion object {
        private const val EXTRA_VERIFICATION_CODE_RESPONSE = "EXTRA_VERIFICATION_CODE_RESPONSE"
        private const val EXTRA_RESEND_CODE_DELAY = "EXTRA_RESEND_CODE_DELAY"
        private const val RESEND_SMS_DELAY = 60

        fun newInstance(param: PhoneVerificationModel): EnterCodeController {
            val bundle = Bundle()
            bundle.putParcelable(EXTRA_VERIFICATION_CODE_RESPONSE, param)
            return EnterCodeController(bundle)
        }
    }
}
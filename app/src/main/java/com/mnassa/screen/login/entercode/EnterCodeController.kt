package com.mnassa.screen.login.entercode

import android.os.Bundle
import android.view.View
import org.kodein.di.generic.instance
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.PhoneVerificationModel
import com.mnassa.translation.fromDictionary
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.login.RegistrationFlowProgress
import com.mnassa.screen.login.selectaccount.SelectAccountController
import com.mnassa.screen.main.MainController
import com.mnassa.screen.registration.RegistrationController
import kotlinx.android.synthetic.main.controller_enter_code.view.*
import kotlinx.android.synthetic.main.header_login.view.*
import kotlinx.android.synthetic.main.sms_code_input.view.*
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.consumeEach
import kotlinx.coroutines.experimental.delay
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Created by Peter on 23.02.2018.
 */
class EnterCodeController(params: Bundle) : MnassaControllerImpl<EnterCodeViewModel>(params) {
    override val layoutId: Int = R.layout.controller_enter_code
    override val viewModel: EnterCodeViewModel by instance()
    private val resendSmsCodeDelay by lazy { resources!!.getInteger(R.integer.validation_code_resend_delay_seconds) }
    private var resendCodeSecondCounter: Int = -1
    private val verificationResponse by lazy { args.getParcelable<PhoneVerificationModel>(EXTRA_VERIFICATION_CODE_RESPONSE) }

    override fun onCreated(savedInstanceState: Bundle?) {
        super.onCreated(savedInstanceState)
        if (savedInstanceState == null) {
            viewModel.verificationResponse = verificationResponse
        }
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        if (resendCodeSecondCounter == -1) {
            resendCodeSecondCounter = resendSmsCodeDelay
        }

        with(view) {
            pbRegistration.progress = RegistrationFlowProgress.ENTER_CODE
            pbRegistration.visibility = View.VISIBLE

            pinView.pinEnteringProgressListener = { progress ->
                pbPin.progress = (pbPin.max * progress).toInt()
            }
            pinView.onPinEnteredListener = { viewModel.verifyCode(it) }

            tvScreenHeader.text = fromDictionary(R.string.login_validation_code_header)
            tvEnterValidationCode.text = fromDictionary(R.string.login_enter_code_title)
            startResendCodeTimer(resendCodeSecondCounter)
        }

        launchCoroutineUI {
            viewModel.openScreenChannel.consumeEach {
                Timber.d("MNSA_LOGIN EnterCodeController->onViewCreated->openScreenChannel-> open $it")
                when (it) {
                    is EnterCodeViewModel.OpenScreenCommand.MainScreen -> open(MainController.newInstance())
                    is EnterCodeViewModel.OpenScreenCommand.RegistrationScreen -> open(RegistrationController.newInstance())
                    is EnterCodeViewModel.OpenScreenCommand.SelectAccount -> open(SelectAccountController.newInstance())
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
        val viewLocal = view ?: return
        viewLocal.tvResendCodeAfter.isEnabled = true
        viewLocal.tvResendCodeAfter.text = fromDictionary(R.string.login_enter_code_resend_code)
        viewLocal.tvResendCodeAfter.setOnClickListener {
            viewModel.resendCode()
            startResendCodeTimer(resendSmsCodeDelay)
            viewLocal.tvResendCodeAfter.isEnabled = false
            viewLocal.tvResendCodeAfter.setOnClickListener(null)
        }
    }

    companion object {
        private const val EXTRA_VERIFICATION_CODE_RESPONSE = "EXTRA_VERIFICATION_CODE_RESPONSE"
        private const val EXTRA_RESEND_CODE_DELAY = "EXTRA_RESEND_CODE_DELAY"

        fun newInstance(param: PhoneVerificationModel): EnterCodeController {
            val bundle = Bundle()
            bundle.putParcelable(EXTRA_VERIFICATION_CODE_RESPONSE, param)
            return EnterCodeController(bundle)
        }
    }
}
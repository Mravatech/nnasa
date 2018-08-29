package com.mnassa.screen.login.enterphone

import android.os.Bundle
import android.text.InputType
import android.text.Spannable
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.AdapterView
import com.mnassa.BuildConfig
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.extensions.SimpleTextWatcher
import com.mnassa.extensions.onImeActionDone
import com.mnassa.extensions.showKeyboard
import com.mnassa.helper.CountryHelper
import com.mnassa.helper.DialogHelper
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.login.RegistrationFlowProgress
import com.mnassa.screen.login.entercode.EnterCodeController
import com.mnassa.screen.login.enterpromo.EnterPromoController
import com.mnassa.screen.login.selectaccount.SelectAccountController
import com.mnassa.screen.main.MainController
import com.mnassa.screen.registration.RegistrationController
import com.mnassa.screen.termsandconditions.TermsAndConditionsController
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_enter_phone.view.*
import kotlinx.android.synthetic.main.header_login.view.*
import kotlinx.android.synthetic.main.or_layout.view.*
import kotlinx.android.synthetic.main.phone_input.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import org.kodein.di.generic.instance

/**
 * Created by Peter on 2/21/2018.
 */
open class EnterPhoneController(args: Bundle = Bundle()) : MnassaControllerImpl<EnterPhoneViewModel>(args) {
    override val layoutId: Int = R.layout.controller_enter_phone
    override val viewModel: EnterPhoneViewModel by instance()
    private val dialogHelper: DialogHelper by instance()
    private val countryHelper: CountryHelper by instance()

    protected val phoneNumber: String?
        get() {
            val view = view ?: return null
            val countryCode = view.spinnerPhoneCode.selectedItem as? CountryCode ?: return null
            return countryCode.withTail(view.etPhoneNumberTail.text.toString()).normalize()
        }

    override fun onCreated(savedInstanceState: Bundle?) {
        super.onCreated(savedInstanceState)

        //open next screen even if current controller in the back stack
        controllerSubscriptionContainer.launchCoroutineUI {
            viewModel.openScreenChannel.consumeEach {
                hideProgress()
                when (it) {
                    is EnterPhoneViewModel.OpenScreenCommand.MainScreen -> open(MainController.newInstance())
                    is EnterPhoneViewModel.OpenScreenCommand.EnterVerificationCode -> open(EnterCodeController.newInstance(it.param))
                    is EnterPhoneViewModel.OpenScreenCommand.Registration -> open(RegistrationController.newInstance())
                    is EnterPhoneViewModel.OpenScreenCommand.SelectAccount -> open(SelectAccountController.newInstance())
                }
            }
        }
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        with(view) {
            pbRegistration.progress = RegistrationFlowProgress.ENTER_PHONE
            pbRegistration.visibility = View.VISIBLE

            tvScreenHeader.text = fromDictionary(R.string.login_header_welcome)
            tvEnterPhoneNumber.text = fromDictionary(R.string.login_enter_phone_title)
            btnVerifyMe.text = fromDictionary(R.string.login_verify_me)
            etPhoneNumberTail.hint = fromDictionary(R.string.login_your_phone)
            tvTermsAndConditions.text = fromDictionary(R.string.login_terms_part_1)
            tvOr.text = fromDictionary(R.string.login_or)
            btnEnterPromo.text = fromDictionary(R.string.login_enter_promo)

            val termsAndCond = fromDictionary(R.string.login_terms_part_2)
            val termsAndCondSpan = Spannable.Factory.getInstance().newSpannable(termsAndCond)
            termsAndCondSpan.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    open(TermsAndConditionsController.newInstance())
                }
            }, 0, termsAndCond.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            tvTermsAndConditions.append(" ")
            tvTermsAndConditions.append(termsAndCondSpan)
            tvTermsAndConditions.movementMethod = LinkMovementMethod.getInstance()

            spinnerPhoneCode.adapter = CountryCodeAdapter(spinnerPhoneCode.context, countryHelper.countries)
            spinnerPhoneCode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) = onInputChanged()
                override fun onItemSelected(a: AdapterView<*>?, v: View?, i: Int, l: Long) {
                    onInputChanged()
                    updateKeyboardType(view)
                }
            }

            btnVerifyMe.setOnClickListener {
                viewModel.requestVerificationCode(phoneNumber ?: return@setOnClickListener)
            }

            btnEnterPromo.setOnClickListener {
                open(EnterPromoController.newInstance(
                        spinnerPhoneCode.selectedItemPosition,
                        etPhoneNumberTail.text.toString()
                ))
            }

            etPhoneNumberTail.addTextChangedListener(SimpleTextWatcher { onInputChanged() })
            etPhoneNumberTail.onImeActionDone { btnVerifyMe.performClick() }
            etPhoneNumberTail.filters = arrayOf(PHONE_INPUT_FILTER)
            btnVerifyMe.isEnabled = validateInput()

            showKeyboard(etPhoneNumberTail)
        }
        updateKeyboardType(view)

        addSignInViaEmailAbility()
    }

    override fun subscribeToServerMaintenanceStatus() = closeServerMaintenanceDialog()

    private fun addSignInViaEmailAbility() {
        if (!BuildConfig.DEBUG) return

        val view = view!!
        view.btnScreenHeaderAction.text = "EMAIL"
        view.btnScreenHeaderAction.visibility = View.VISIBLE
        view.btnScreenHeaderAction.setOnClickListener {
            dialogHelper.showLoginByEmailDebugDialog(it.context) { email, password ->
                viewModel.signInByEmail(email, password)
            }
        }
    }

    protected fun onInputChanged() {
        val view = view ?: return
        view.btnVerifyMe.isEnabled = validateInput()
        view.etPhoneNumberTail.error = null
    }

    protected open fun validateInput(): Boolean {
        return PhoneNumber.isValid(phoneNumber)
    }

    private fun updateKeyboardType(view: View) {
        val countryCode = (view.spinnerPhoneCode.selectedItem as? CountryCode)?.phonePrefix ?: return
        view.etPhoneNumberTail.inputType = if (countryCode is PhonePrefix.SaudiArabia) {
             InputType.TYPE_CLASS_TEXT
        } else {
            InputType.TYPE_CLASS_PHONE
        }
    }

    companion object {
        fun newInstance() = EnterPhoneController()
    }
}
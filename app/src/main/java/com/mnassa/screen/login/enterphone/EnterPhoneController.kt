package com.mnassa.screen.login.enterphone

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.text.Spannable
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.impl.TranslatedWordModelImpl
import com.mnassa.domain.other.AppInfoProvider
import com.mnassa.extensions.PATTERN_PHONE_TAIL
import com.mnassa.extensions.SimpleTextWatcher
import com.mnassa.translation.fromDictionary
import com.mnassa.extensions.onImeActionDone
import com.mnassa.extensions.showKeyboard
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.login.RegistrationFlowProgress
import com.mnassa.screen.login.entercode.EnterCodeController
import com.mnassa.screen.login.enterpromo.EnterPromoController
import com.mnassa.screen.login.selectaccount.SelectAccountController
import com.mnassa.screen.main.MainController
import com.mnassa.screen.registration.RegistrationController
import kotlinx.android.synthetic.main.controller_enter_phone.view.*
import kotlinx.android.synthetic.main.header_login.view.*
import kotlinx.android.synthetic.main.or_layout.view.*
import kotlinx.android.synthetic.main.phone_input.view.*
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by Peter on 2/21/2018.
 */
open class EnterPhoneController(args: Bundle = Bundle()) : MnassaControllerImpl<EnterPhoneViewModel>(args) {
    override val layoutId: Int = R.layout.controller_enter_phone
    override val viewModel: EnterPhoneViewModel by instance()
    private val countryCodes = mutableListOf(
            CountryCode(
                    flagRes = R.drawable.ic_flag_of_saudi_arabia,
                    name = TranslatedWordModelImpl(fromDictionary(R.string.country_saudi_arabia)),
                    phonePrefix = "+966"),
            CountryCode(
                    flagRes = R.drawable.ic_flag_of_ukraine,
                    name = TranslatedWordModelImpl(fromDictionary(R.string.country_ukraine)),
                    phonePrefix = "+380"),
            CountryCode(
                    flagRes = R.drawable.ic_flag_of_the_united_states,
                    name = TranslatedWordModelImpl(fromDictionary(R.string.country_united_states)),
                    phonePrefix = "+1"),
            CountryCode(
                    flagRes = R.drawable.ic_flag_of_canada,
                    name = TranslatedWordModelImpl(fromDictionary(R.string.country_canada)),
                    phonePrefix = "+1")
    )
    protected val phoneNumber: String
        get() {
            val v = view ?: return ""
            val countryCode = v.spinnerPhoneCode.selectedItem as? CountryCode ?: return ""
            return countryCode.phonePrefix
                    .replace("+", "") +
                    v.etPhoneNumberTail.text.toString()
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
                    Snackbar.make(view, "Terms and conditions!", Snackbar.LENGTH_SHORT).show()
                }
            }, 0, termsAndCond.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)


            tvTermsAndConditions.append(" ")
            tvTermsAndConditions.append(termsAndCondSpan)
            tvTermsAndConditions.movementMethod = LinkMovementMethod.getInstance()


            spinnerPhoneCode.adapter = CountryCodeAdapter(spinnerPhoneCode.context, countryCodes)
            spinnerPhoneCode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) = onInputChanged()
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) = onInputChanged()
            }

            btnVerifyMe.setOnClickListener {
                viewModel.requestVerificationCode(phoneNumber)
            }

            btnEnterPromo.setOnClickListener {
                open(EnterPromoController.newInstance(
                        spinnerPhoneCode.selectedItemPosition,
                        etPhoneNumberTail.text.toString()
                ))
            }

            etPhoneNumberTail.addTextChangedListener(SimpleTextWatcher { onInputChanged() })
            etPhoneNumberTail.onImeActionDone { btnVerifyMe.performClick() }
            btnVerifyMe.isEnabled = validateInput()

            showKeyboard(etPhoneNumberTail)
        }

        if (instance<AppInfoProvider>().value.isDebug) addSignInViaEmailAbility()
    }

    private fun addSignInViaEmailAbility() {
        //!!!DEBUG ONLY!!!
        val view = view!!
        view.btnScreenHeaderAction.text = "EMAIL"
        view.btnScreenHeaderAction.visibility = View.VISIBLE
        view.btnScreenHeaderAction.setOnClickListener { requestEmailAndPassword() }
    }

    private fun requestEmailAndPassword() {
        //!!!DEBUG ONLY!!!
        val context = view!!.context
        val container = LinearLayout(context)
        container.orientation = LinearLayout.VERTICAL

        val email = EditText(context)
        email.hint = "Email"
        container.addView(email)

        val password = EditText(context)
        password.hint = "Password"
        container.addView(password)

        lateinit var dialog: AlertDialog

        var btnHardcodedEmailAndPassword = Button(context)
        btnHardcodedEmailAndPassword.text = "p3@nxt.ru"
        btnHardcodedEmailAndPassword.setOnClickListener {
            viewModel.signInByEmail("p3@nxt.ru", "123123")
            dialog.dismiss()
        }
        container.addView(btnHardcodedEmailAndPassword)
        //
        btnHardcodedEmailAndPassword = Button(context)
        btnHardcodedEmailAndPassword.text = "chas@ukr.net"
        btnHardcodedEmailAndPassword.setOnClickListener {
            viewModel.signInByEmail("chas@ukr.net", "123123")
            dialog.dismiss()
        }
        container.addView(btnHardcodedEmailAndPassword)
        //
        btnHardcodedEmailAndPassword = Button(context)
        btnHardcodedEmailAndPassword.text = "serg@u.net"
        btnHardcodedEmailAndPassword.setOnClickListener {
            viewModel.signInByEmail("serg@u.net", "123123")
            dialog.dismiss()
        }
        container.addView(btnHardcodedEmailAndPassword)

        val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT)
        container.layoutParams = layoutParams

        dialog = AlertDialog.Builder(context)
                .setView(container)
                .setPositiveButton("Login", { _, _ ->
                    viewModel.signInByEmail(
                            email.text.toString(),
                            password.text.toString())
                })
                .show()
    }

    protected fun onInputChanged() {
        val view = view ?: return
        view.btnVerifyMe.isEnabled = validateInput()
        view.etPhoneNumberTail.error = null
    }

    protected open fun validateInput(): Boolean {
        return PATTERN_PHONE_TAIL.matcher(phoneNumber).matches()
    }

    companion object {
        fun newInstance() = EnterPhoneController()
    }
}
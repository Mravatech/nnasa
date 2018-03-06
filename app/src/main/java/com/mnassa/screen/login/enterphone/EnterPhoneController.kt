package com.mnassa.screen.login.enterphone

import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.text.Spannable
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.LinearLayout
import com.bluelinelabs.conductor.RouterTransaction
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.impl.TranslatedWordModelImpl
import com.mnassa.domain.other.AppInfoProvider
import com.mnassa.extensions.PATTERN_PHONE_TAIL
import com.mnassa.extensions.SimpleTextWatcher
import com.mnassa.translation.fromDictionary
import com.mnassa.extensions.onImeActionDone
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
open class EnterPhoneController : MnassaControllerImpl<EnterPhoneViewModel>() {
    override val layoutId: Int = R.layout.controller_enter_phone
    override val viewModel: EnterPhoneViewModel by instance()
    protected val phoneNumber: String
        get() {
            val v = view ?: return ""
            val countryCode = v.spinnerPhoneCode.selectedItem as? CountryCode ?: return ""
            return countryCode.phonePrefix
                    .replace("+", "") +
                    v.etPhoneNumberTail.text.toString()
        }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        with(view) {
            pbRegistration.progress = RegistrationFlowProgress.ENTER_PHONE

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

            val onPhoneChanged = {
                btnVerifyMe.isEnabled = isPhoneValid(phoneNumber)
                etPhoneNumberTail.error = null
            }

            val countries = mutableListOf(
                    //TODO: use iOS app countries and add more icons
                    CountryCode(R.mipmap.ic_launcher, TranslatedWordModelImpl("1", "Ukraine1", null, null), "+38"),
                    CountryCode(R.mipmap.ic_launcher, TranslatedWordModelImpl("2", "Ukraine2", null, null), "+38"),
                    CountryCode(R.mipmap.ic_launcher, TranslatedWordModelImpl("3", "Ukraine3", null, null), "+38"),
                    CountryCode(R.mipmap.ic_launcher, TranslatedWordModelImpl("4", "Saudi Arabia", null, null), "+966")
            )
            spinnerPhoneCode.adapter = CountryCodeAdapter(spinnerPhoneCode.context, countries)
            spinnerPhoneCode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) = onPhoneChanged()
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) = onPhoneChanged()
            }

            btnVerifyMe.setOnClickListener {
                viewModel.requestVerificationCode(phoneNumber)
            }

            btnEnterPromo.setOnClickListener {
                router.pushController(RouterTransaction.with(EnterPromoController.newInstance()))
            }

            etPhoneNumberTail.addTextChangedListener(SimpleTextWatcher { onPhoneChanged() })
            etPhoneNumberTail.onImeActionDone { btnVerifyMe.performClick() }
            btnVerifyMe.isEnabled = isPhoneValid(phoneNumber)
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
        val password = EditText(context)
        password.hint = "Password"

        container.addView(email)
        container.addView(password)

        val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT)
        container.layoutParams = layoutParams

        AlertDialog.Builder(context)
                .setView(container)
                .setPositiveButton("Login", { _, _ ->
                    viewModel.signInByEmail(
                            email.text.toString(),
                            password.text.toString())
                })
                .show()
    }



    private fun isPhoneValid(phoneNumber: String): Boolean {
        return PATTERN_PHONE_TAIL.matcher(phoneNumber).matches()
    }

    companion object {
        fun newInstance() = EnterPhoneController()
    }
}
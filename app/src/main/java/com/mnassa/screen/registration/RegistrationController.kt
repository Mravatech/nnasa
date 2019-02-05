package com.mnassa.screen.registration

import com.google.android.material.snackbar.Snackbar
import androidx.core.content.ContextCompat
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.AutoCompleteTextView
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.extensions.SimpleTextWatcher
import com.mnassa.extensions.disable
import com.mnassa.extensions.enable
import com.mnassa.extensions.isGone
import com.mnassa.helper.PlayServiceHelper
import com.mnassa.screen.accountinfo.organization.OrganizationInfoController
import com.mnassa.screen.accountinfo.personal.PersonalInfoController
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.login.RegistrationFlowProgress
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.chip_layout.view.*
import kotlinx.android.synthetic.main.controller_registration.view.*
import kotlinx.android.synthetic.main.header_login.view.*
import kotlinx.android.synthetic.main.sub_reg_company.view.*
import kotlinx.android.synthetic.main.sub_reg_personal.view.*
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.consume
import kotlinx.coroutines.experimental.channels.consumeEach
import org.kodein.di.generic.instance

/**
 * Created by Peter on 2/26/2018.
 */

class RegistrationController : MnassaControllerImpl<RegistrationViewModel>() {
    override val layoutId: Int = R.layout.controller_registration
    override val viewModel: RegistrationViewModel by instance()
    private val playServiceHelper: PlayServiceHelper by instance()
    private var personSelectedPlaceId: String? = null
        set(value) {
            field = value
            onPersonChanged()
        }
    private var companySelectedPlaceId: String? = null
        set(value) {
            field = value
            onOrganizationChanged()
        }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        playServiceHelper.googleApiClient.connect()
        with(view) {
            pbRegistration.progress = RegistrationFlowProgress.SELECT_ACCOUNT_TYPE
            pbRegistration.visibility = View.VISIBLE

            tvScreenHeader.text = fromDictionary(R.string.reg_title)

            updateAccountTypeSwitch()
            llAccountTypePersonal.setOnClickListener {
                llPersonal.isGone = false
                llOrganization.isGone = true
                updateAccountTypeSwitch()
                onPersonChanged()
            }
            llAccountTypeOrganization.setOnClickListener {
                llPersonal.isGone = true
                llOrganization.isGone = false
                updateAccountTypeSwitch()
                onOrganizationChanged()
            }
            btnScreenHeaderAction.text = fromDictionary(R.string.reg_next)
            btnScreenHeaderAction.visibility = View.VISIBLE
            btnScreenHeaderAction.setOnClickListener { processRegisterClick() }
            tvOr.text = fromDictionary(R.string.login_or)
            tvAccountTypePersonal.text = fromDictionary(R.string.reg_account_type_personal)
            tvAccountTypeOrganization.text = fromDictionary(R.string.reg_account_type_organization)
            bindPersonalPage(this)
            bindOrganizationPage(this)
        }
        launchCoroutineUI {
            viewModel.openScreenChannel.consumeEach {
                val controller = when (it) {
                    is RegistrationViewModel.OpenScreenCommand.PersonalInfoScreen -> {
                        PersonalInfoController.newInstance(it.shortAccountModel)
                    }
                    is RegistrationViewModel.OpenScreenCommand.OrganizationInfoScreen -> {
                        OrganizationInfoController.newInstance(it.shortAccountModel)
                    }
                }
                open(controller)
            }
        }
        launchCoroutineUI {
            viewModel.errorMessageChannel.consumeEach {
                Snackbar.make(view, it, Snackbar.LENGTH_SHORT).show()
            }
        }
        launchCoroutineUI {
            val hasPersonalAccount = viewModel.hasPersonalAccountChannel()
            view.llAccountTypePersonal.isEnabled = !hasPersonalAccount
            if (hasPersonalAccount) view.llAccountTypeOrganization.performClick()
            else view.llAccountTypePersonal.performClick()
        }
    }

    private fun updateAccountTypeSwitch() {
        val view = view ?: return

        val grayColor = ContextCompat.getColor(view.context, R.color.gray_cool)
        val blackColor = ContextCompat.getColor(view.context, R.color.black)

        when {
            view.llPersonal.visibility == View.VISIBLE -> {
                view.ivAccountTypeOrganization.disable()
                view.ivAccountTypePersonal.enable()
                view.tvAccountTypePersonal.setTextColor(blackColor)
                view.tvAccountTypeOrganization.setTextColor(grayColor)
            }
            view.llOrganization.visibility == View.VISIBLE -> {
                view.ivAccountTypeOrganization.enable()
                view.ivAccountTypePersonal.disable()
                view.tvAccountTypePersonal.setTextColor(grayColor)
                view.tvAccountTypeOrganization.setTextColor(blackColor)
            }
        }
    }

    override fun onDestroy() {
        if (playServiceHelper.googleApiClient.isConnected) {
            playServiceHelper.googleApiClient.disconnect()
        }
        super.onDestroy()
    }

    private suspend fun canCreatePersonInfo(): Boolean {
        with(view ?: return false) {
            if (etPersonFirstName.text.isNullOrBlank()) return false
            if (etPersonSecondName.text.isNullOrBlank()) return false
            if (etPersonUserName.text.isNullOrBlank()) return false
            if (personSelectedPlaceId == null) return false
            if (viewModel.isOffersMandatory() && chipPersonOffers.getTags().isEmpty()) return false
            if (viewModel.isInterestsMandatory() && chipPersonInterests.getTags().isEmpty()) return false
        }
        return true
    }

    private suspend fun canCreateOrganizationInfo(): Boolean {
        with(view ?: return false) {
            if (etCompanyName.text.isNullOrBlank()) return false
            if (etCompanyUserName.text.isNullOrBlank()) return false
            if (companySelectedPlaceId == null) return false
            if (viewModel.isOffersMandatory() && chipCompanyOffers.getTags().isEmpty()) return false
            if (viewModel.isInterestsMandatory() && chipCompanyInterests.getTags().isEmpty()) return false
        }
        return true
    }

    private var onInfoChangedJob: Job? = null
    private fun onPersonChanged() {
        onInfoChangedJob?.cancel()
        onInfoChangedJob = launchCoroutineUI {
            getViewSuspend().btnScreenHeaderAction.isEnabled = canCreatePersonInfo()
        }
    }

    private fun onOrganizationChanged() {
        onInfoChangedJob?.cancel()
        onInfoChangedJob = launchCoroutineUI {
            getViewSuspend().btnScreenHeaderAction.isEnabled = canCreateOrganizationInfo()
        }
    }

    private fun processRegisterClick() {
        view?.btnScreenHeaderAction?.isEnabled = false
        launchCoroutineUI {
            with(getViewSuspend()) {
                when {
                    llPersonal.visibility == View.VISIBLE -> viewModel.registerPerson(
                            firstName = etPersonFirstName.text.toString(),
                            secondName = etPersonSecondName.text.toString(),
                            userName = etPersonUserName.text.toString(),
                            city = personSelectedPlaceId ?: "",
                            offers = chipPersonOffers.getTags(),
                            interests = chipPersonInterests.getTags())
                    llOrganization.visibility == View.VISIBLE -> viewModel.registerOrganization(
                            companyName = etCompanyName.text.toString(),
                            userName = etCompanyUserName.text.toString(),
                            city = companySelectedPlaceId ?: "",
                            offers = chipCompanyOffers.getTags(),
                            interests = chipCompanyInterests.getTags()
                    )
                    else -> throw IllegalArgumentException("Invalid page position!")
                }
            }
        }.invokeOnCompletion {
            val view = view ?: return@invokeOnCompletion
            when {
                view.llPersonal.visibility == View.VISIBLE -> onPersonChanged()
                view.llOrganization.visibility == View.VISIBLE -> onOrganizationChanged()
            }
        }
    }

    private fun bindPersonalPage(view: View) {
        with(view) {
            tilPersonFirstName.hint = fromDictionary(R.string.reg_personal_first_name)
            tilPersonSecondName.hint = fromDictionary(R.string.reg_personal_last_name)
            tilPersonUserName.hint = fromDictionary(R.string.reg_personal_user_name)
            tilPersonCity.hint = fromDictionary(R.string.reg_personal_city)
            chipPersonOffers.etChipInput.hint = fromDictionary(R.string.reg_person_type_here)
            chipPersonInterests.etChipInput.hint = fromDictionary(R.string.reg_person_type_here)
            launchCoroutineUI {
                chipPersonOffers.tvChipHeader.text = formatTagLabel(fromDictionary(R.string.reg_account_can_help_with))
                chipPersonInterests.tvChipHeader.text = formatTagLabel(fromDictionary(R.string.reg_account_interested_in))
            }
            chipPersonOffers.setTags(emptyList())
            chipPersonInterests.setTags(emptyList())
            etPersonFirstName.addTextChangedListener(SimpleTextWatcher { onPersonChanged() })
            etPersonSecondName.addTextChangedListener(SimpleTextWatcher { onPersonChanged() })
            etPersonUserName.addTextChangedListener(SimpleTextWatcher { onPersonChanged() })
            chipPersonOffers.onChipsChangeListener = { onPersonChanged() }
            chipPersonInterests.onChipsChangeListener = { onPersonChanged() }
        }
        onPersonChanged()
        setupAutoComplete(view.actvPersonCity, true)
    }

    private fun bindOrganizationPage(view: View) {
        with(view) {
            view.tilCompanyName.hint = fromDictionary(R.string.reg_account_company_name)
            view.tilCompanyUserName.hint = fromDictionary(R.string.reg_personal_user_name)
            view.tilCompanyCity.hint = fromDictionary(R.string.reg_personal_city)
            chipCompanyOffers.etChipInput.hint = fromDictionary(R.string.reg_person_type_here)
            chipCompanyInterests.etChipInput.hint = fromDictionary(R.string.reg_person_type_here)
            launchCoroutineUI {
                chipCompanyOffers.tvChipHeader.text = formatTagLabel(fromDictionary(R.string.reg_account_can_help_with))
                chipCompanyInterests.tvChipHeader.text = formatTagLabel(fromDictionary(R.string.reg_account_interested_in))
            }
            etCompanyName.addTextChangedListener(SimpleTextWatcher { onOrganizationChanged() })
            etCompanyUserName.addTextChangedListener(SimpleTextWatcher { onOrganizationChanged() })
            chipCompanyOffers.onChipsChangeListener = { onOrganizationChanged() }
            chipCompanyInterests.onChipsChangeListener = { onOrganizationChanged() }
        }
        onOrganizationChanged()
        setupAutoComplete(view.actvCompanyCity, false)
    }

    private fun setupAutoComplete(city: AutoCompleteTextView, isPerson: Boolean) {
        val placeAutocompleteAdapter = PlaceAutocompleteAdapter(city.context, viewModel)
        city.setAdapter(placeAutocompleteAdapter)
        city.setOnItemClickListener { _, _, i, _ ->
            val item = placeAutocompleteAdapter.getItem(i) ?: return@setOnItemClickListener
            if (isPerson) {
                val personSelectedPlaceName = "${item.primaryText} ${item.secondaryText}"
                city.setText(personSelectedPlaceName)
                personSelectedPlaceId = item.placeId
            } else {
                val companySelectedPlaceName = "${item.primaryText} ${item.secondaryText}"
                city.setText(companySelectedPlaceName)
                companySelectedPlaceId = item.placeId
            }
        }
        city.addTextChangedListener(SimpleTextWatcher {
            personSelectedPlaceId = null
            companySelectedPlaceId = null
        })
    }

    companion object {
        fun newInstance() = RegistrationController()
    }

    private suspend fun formatTagLabel(prefix: String): CharSequence {
        val reward = viewModel.addTagRewardChannel.consume { receive() } ?: return prefix
        val result = SpannableString(fromDictionary(R.string.add_tags_reward_suffix).format(prefix, reward))
        val accentColor = ContextCompat.getColor(getViewSuspend().context, R.color.accent)
        result.setSpan(ForegroundColorSpan(accentColor), prefix.length, result.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return result
    }
}
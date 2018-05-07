package com.mnassa.screen.registration

import android.content.Context
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.extensions.SimpleTextWatcher
import com.mnassa.extensions.disable
import com.mnassa.extensions.enable
import com.mnassa.extensions.hideKeyboard
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
import kotlinx.coroutines.experimental.channels.consumeEach
import org.kodein.di.generic.instance

/**
 * Created by Peter on 2/26/2018.
 */

class RegistrationController : MnassaControllerImpl<RegistrationViewModel>() {
    override val layoutId: Int = R.layout.controller_registration
    override val viewModel: RegistrationViewModel by instance()
    private val playServiceHelper: PlayServiceHelper by instance()

    private lateinit var registrationAdapter: RegistrationAdapter

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        playServiceHelper.googleApiClient.connect()
        registrationAdapter = RegistrationAdapter(view.context)
        with(view) {
            pbRegistration.progress = RegistrationFlowProgress.SELECT_ACCOUNT_TYPE
            pbRegistration.visibility = View.VISIBLE

            tvScreenHeader.text = fromDictionary(R.string.reg_title)
            vpRegistration.adapter = registrationAdapter
            vpRegistration.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
                override fun onPageSelected(position: Int) {
                    updateAccountTypeSwitch()
                    hideKeyboard()
                    when (position) {
                        PAGE_PERSON_INFO -> onPersonChanged()
                        PAGE_ORGANIZATION_INFO -> onOrganizationChanged()
                    }
                }
            })
            updateAccountTypeSwitch()
            llAccountTypePersonal.setOnClickListener {
                vpRegistration.currentItem = PAGE_PERSON_INFO
            }
            llAccountTypeOrganization.setOnClickListener {
                vpRegistration.currentItem = PAGE_ORGANIZATION_INFO
            }
            btnScreenHeaderAction.text = fromDictionary(R.string.reg_next)
            btnScreenHeaderAction.visibility = View.VISIBLE
            btnScreenHeaderAction.setOnClickListener { processRegisterClick() }
            tvOr.text = fromDictionary(R.string.login_or)
            tvAccountTypePersonal.text = fromDictionary(R.string.reg_account_type_personal)
            tvAccountTypeOrganization.text = fromDictionary(R.string.reg_account_type_organization)
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
    }

    private fun updateAccountTypeSwitch() {
        val view = view ?: return

        val grayColor = ContextCompat.getColor(view.context, R.color.gray_cool)
        val blackColor = ContextCompat.getColor(view.context, R.color.black)

        when (view.vpRegistration.currentItem) {
            PAGE_PERSON_INFO -> {
                view.ivAccountTypeOrganization.disable()
                view.ivAccountTypePersonal.enable()
                view.tvAccountTypePersonal.setTextColor(blackColor)
                view.tvAccountTypeOrganization.setTextColor(grayColor)
            }
            PAGE_ORGANIZATION_INFO -> {
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

    private fun canCreatePersonInfo(): Boolean {
        with(view ?: return false) {
            if (vpRegistration.etPersonFirstName.text.isBlank()) return false
            if (vpRegistration.etPersonSecondName.text.isBlank()) return false
            if (vpRegistration.etPersonUserName.text.isBlank()) return false
            if (registrationAdapter.personSelectedPlaceId == null) return false
            if (vpRegistration.chipPersonOffers.getTags().isEmpty()) return false
            if (vpRegistration.chipPersonInterests.getTags().isEmpty()) return false
        }
        return true
    }

    private fun canCreateOrganizationInfo(): Boolean {
        with(view ?: return false) {
            if (vpRegistration.etCompanyName.text.isBlank()) return false
            if (vpRegistration.etCompanyUserName.text.isBlank()) return false
            if (registrationAdapter.companySelectedPlaceId == null) return false
            if (vpRegistration.chipCompanyOffers.getTags().isEmpty()) return false
            if (vpRegistration.chipCompanyInterests.getTags().isEmpty()) return false
        }
        return true
    }

    private fun onPersonChanged() {
        val view = view ?: return
        view.btnScreenHeaderAction.isEnabled = canCreatePersonInfo()
    }

    private fun onOrganizationChanged() {
        val view = view ?: return
        view.btnScreenHeaderAction.isEnabled = canCreateOrganizationInfo()
    }

    private fun processRegisterClick() {
        with(requireNotNull(view)) {
            when (vpRegistration.currentItem) {
                PAGE_PERSON_INFO -> viewModel.registerPerson(
                        firstName = etPersonFirstName.text.toString(),
                        secondName = etPersonSecondName.text.toString(),
                        userName = etPersonUserName.text.toString(),
                        city = registrationAdapter.personSelectedPlaceId ?: "",
                        offers = chipPersonOffers.getTags(),
                        interests = chipPersonInterests.getTags())
                PAGE_ORGANIZATION_INFO -> viewModel.registerOrganization(
                        companyName = etCompanyName.text.toString(),
                        userName = etCompanyUserName.text.toString(),
                        city = registrationAdapter.companySelectedPlaceId ?: "",
                        offers = chipCompanyOffers.getTags(),
                        interests = chipCompanyInterests.getTags()
                )
                else -> throw IllegalArgumentException("Invalid page position $vpRegistration.currentItem")
            }
        }
    }

    companion object {
        private const val PAGE_PERSON_INFO = 0
        private const val PAGE_ORGANIZATION_INFO = 1

        fun newInstance() = RegistrationController()
    }

    inner class RegistrationAdapter(
            private val context: Context
    ) : PagerAdapter() {

        private var companySelectedPlaceName: String? = null
        private var personSelectedPlaceName: String? = null
        var companySelectedPlaceId: String? = null
            set(value) {
                field = value
                onOrganizationChanged()
            }
        var personSelectedPlaceId: String? = null
            set(value) {
                field = value
                onPersonChanged()
            }

        override fun isViewFromObject(view: View, obj: Any): Boolean = view == obj

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val layoutId = when (position) {
                PAGE_PERSON_INFO -> R.layout.controller_registration_personal
                PAGE_ORGANIZATION_INFO -> R.layout.controller_registration_organization
                else -> throw IllegalArgumentException("Invalid page position $position")
            }
            val view = LayoutInflater.from(container.context).inflate(layoutId, container, false)
            container.addView(view)
            when (position) {
                PAGE_PERSON_INFO -> onPersonalPageCreated(view)
                PAGE_ORGANIZATION_INFO -> onOrganizationPageCreated(view)
                else -> throw IllegalArgumentException("Invalid page position $position")
            }
            return view
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return when (position) {
                PAGE_PERSON_INFO -> fromDictionary(R.string.reg_personal_info_title)
                PAGE_ORGANIZATION_INFO -> "Organization" //TODO: add translate
                else -> throw IllegalArgumentException("Invalid page position $position")
            }
        }

        override fun getCount(): Int = 2

        private fun onPersonalPageCreated(view: View) {
            with(view) {
                tilPersonFirstName.hint = fromDictionary(R.string.reg_personal_first_name)
                tilPersonSecondName.hint = fromDictionary(R.string.reg_personal_last_name)
                tilPersonUserName.hint = fromDictionary(R.string.reg_personal_user_name)
                tilPersonCity.hint = fromDictionary(R.string.reg_personal_city)
                chipPersonOffers.etChipInput.hint = fromDictionary(R.string.reg_person_type_here)
                chipPersonOffers.tvChipHeader.text = fromDictionary(R.string.reg_account_can_help_with)
                chipPersonInterests.etChipInput.hint = fromDictionary(R.string.reg_person_type_here)
                chipPersonInterests.tvChipHeader.text = fromDictionary(R.string.reg_account_interested_in)
                chipPersonOffers.chipSearch = viewModel
                chipPersonOffers.setTags(emptyList())
                chipPersonInterests.chipSearch = viewModel
                chipPersonInterests.setTags(emptyList())
                etPersonFirstName.addTextChangedListener(SimpleTextWatcher { onPersonChanged() })
                etPersonSecondName.addTextChangedListener(SimpleTextWatcher { onPersonChanged() })
                etPersonUserName.addTextChangedListener(SimpleTextWatcher { onPersonChanged() })
                chipPersonOffers.chipsChangeListener = { onPersonChanged() }
                chipPersonInterests.chipsChangeListener = { onPersonChanged() }
            }
            onPersonChanged()
            setAdapter(view.actvPersonCity, true)
        }

        private fun onOrganizationPageCreated(view: View) {
            with(view) {
                view.tilCompanyName.hint = fromDictionary(R.string.reg_account_company_name)
                view.tilCompanyUserName.hint = fromDictionary(R.string.reg_personal_user_name)
                view.tilCompanyCity.hint = fromDictionary(R.string.reg_personal_city)
                chipCompanyOffers.etChipInput.hint = fromDictionary(R.string.reg_person_type_here)
                chipCompanyOffers.tvChipHeader.text = fromDictionary(R.string.reg_account_can_help_with)
                chipCompanyInterests.etChipInput.hint = fromDictionary(R.string.reg_person_type_here)
                chipCompanyInterests.tvChipHeader.text = fromDictionary(R.string.reg_account_interested_in)
                chipCompanyOffers.chipSearch = viewModel
                chipCompanyInterests.chipSearch = viewModel
                etCompanyName.addTextChangedListener(SimpleTextWatcher { onOrganizationChanged() })
                etCompanyUserName.addTextChangedListener(SimpleTextWatcher { onOrganizationChanged() })
                chipCompanyOffers.chipsChangeListener = { onOrganizationChanged() }
                chipCompanyInterests.chipsChangeListener = { onOrganizationChanged() }
            }
            setAdapter(view.actvCompanyCity, false)
        }

        private fun setAdapter(city: AutoCompleteTextView, isPerson: Boolean) {
            val placeAutocompleteAdapter = PlaceAutocompleteAdapter(context, viewModel)
            city.setAdapter(placeAutocompleteAdapter)
            city.setOnItemClickListener({ _, _, i, _ ->
                val item = placeAutocompleteAdapter.getItem(i) ?: return@setOnItemClickListener
                if (isPerson) {
                    personSelectedPlaceId = item.placeId
                    personSelectedPlaceName = "${item.primaryText} ${item.secondaryText}"
                    city.setText(personSelectedPlaceName ?: "")
                } else {
                    companySelectedPlaceId = item.placeId
                    companySelectedPlaceName = "${item.primaryText} ${item.secondaryText}"
                    city.setText(companySelectedPlaceName ?: "")
                }
            })
        }
    }
}
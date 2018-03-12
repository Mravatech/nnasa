package com.mnassa.screen.registration

import android.support.v4.content.ContextCompat
import android.content.Context
import android.support.design.widget.Snackbar
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import com.bluelinelabs.conductor.RouterTransaction
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.extensions.disable
import com.mnassa.extensions.enable
import com.mnassa.extensions.hideKeyboard
import com.mnassa.google.PlayServiceHelper
import com.mnassa.screen.accountinfo.organization.OrganizationInfoController
import com.mnassa.screen.accountinfo.personal.PersonalInfoController
import com.mnassa.translation.fromDictionary
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.login.RegistrationFlowProgress
import kotlinx.android.synthetic.main.controller_registration.view.*
import kotlinx.android.synthetic.main.controller_registration_organization.view.*
import kotlinx.android.synthetic.main.controller_registration_personal.view.*
import kotlinx.android.synthetic.main.header_login.view.*
import kotlinx.coroutines.experimental.channels.consumeEach

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
            vpRegistration.addOnPageChangeListener(object: ViewPager.SimpleOnPageChangeListener() {
                override fun onPageSelected(position: Int) {
                    updateAccountTypeSwitch()
                    hideKeyboard()
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
                        OrganizationInfoController.newInstance()
                    }
                }
                router.popToRoot()
                router.replaceTopController(RouterTransaction.with(controller))
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

        val grayColor = ContextCompat.getColor(view.context, R.color.coolGray)
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

    private fun validatePersonInfo(): Boolean {
        val v = view
        return v != null
    }

    private fun validateOrganizationInfo(): Boolean {
        val v = view
        return v != null
    }

    private fun processRegisterClick() {
        with(requireNotNull(view)) {
            when (vpRegistration.currentItem) {
                PAGE_PERSON_INFO -> if (validatePersonInfo()) viewModel.registerPerson(
                        firstName = etPersonFirstName.text.toString(),
                        secondName = etPersonSecondName.text.toString(),
                        userName = etPersonUserName.text.toString(),
                        city = registrationAdapter.personSelectedPlaceId ?: "",
                        offers = listOf(etPersonOffers.text.toString()),
                        interests = listOf(etPersonInterests.text.toString()))
                PAGE_ORGANIZATION_INFO -> if (validateOrganizationInfo()) viewModel.registerOrganization(
                        companyName = etCompanyName.text.toString(),
                        userName = etCompanyUserName.text.toString(),
                        city = registrationAdapter.companySelectedPlaceId ?: "",
                        offers = listOf(etCompanyOffers.text.toString()),
                        interests = listOf(etCompanyInterests.text.toString())
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
            private val context: Context)
        : PagerAdapter() {

        private var companySelectedPlaceName: String? = null
        private var personSelectedPlaceName: String? = null
        var companySelectedPlaceId: String? = null
        var personSelectedPlaceId: String? = null

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
                tilPersonOffers.hint = "Offers"
                tilPersonInterests.hint = "Interests"
            }
            setAdapter(view.actvPersonCity, true)
        }

        private fun onOrganizationPageCreated(view: View) {
            with(view) {
                tilCompanyName.hint = "Company name" //TODO: translation
                tilCompanyUserName.hint = "User name"
                tilCompanyCity.hint = "City"
                tilCompanyOffers.hint = "Offers"
                tilCompanyInterests.hint = "Interests"
            }
            setAdapter(view.actvCompanyCity, false)
        }

        private fun setAdapter(city: AutoCompleteTextView, isPerson: Boolean) {
            val placeAutocompleteAdapter = PlaceAutocompleteAdapter(context, viewModel)
            city.setAdapter(placeAutocompleteAdapter)
            city.setOnItemClickListener({ _, _, i, _ ->
                placeAutocompleteAdapter.getItem(i) ?: return@setOnItemClickListener
                if (isPerson) {
                    personSelectedPlaceId = placeAutocompleteAdapter.getItem(i)?.placeId
                    personSelectedPlaceName = "${placeAutocompleteAdapter.getItem(i)?.primaryText} ${placeAutocompleteAdapter.getItem(i)?.secondaryText}"
                    city.setText(personSelectedPlaceName ?: "")
                } else {
                    companySelectedPlaceId = placeAutocompleteAdapter.getItem(i)?.placeId
                    companySelectedPlaceName = "${placeAutocompleteAdapter.getItem(i)?.primaryText} ${placeAutocompleteAdapter.getItem(i)?.secondaryText}"
                    city.setText(companySelectedPlaceName ?: "")
                }
            })
        }

    }
}
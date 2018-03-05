package com.mnassa.screen.registration

import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.RouterTransaction
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.extensions.disable
import com.mnassa.extensions.enable
import com.mnassa.translation.fromDictionary
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.invite.InviteController
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

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        with(view) {
            tvScreenHeader.text = fromDictionary(R.string.reg_title)
            vpRegistration.adapter = RegistrationAdapter()
            vpRegistration.addOnPageChangeListener(object: ViewPager.SimpleOnPageChangeListener() {
                override fun onPageSelected(position: Int) = updateAccountTypeSwitch()
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
                    //TODO: uncomment when profile will be completed
//                    is RegistrationViewModel.OpenScreenCommand.PersonalInfoScreen -> {
//                        PersonalInfoController.newInstance()
//                    }
//                    is RegistrationViewModel.OpenScreenCommand.OrganizationInfoScreen -> {
//                        OrganizationInfoController.newInstance()
//                    }
                    else -> InviteController.newInstance()
                }
                router.popToRoot()
                router.replaceTopController(RouterTransaction.with(controller))
            }
        }
    }

    private fun updateAccountTypeSwitch() {
        val v = view ?: return
        when (v.vpRegistration.currentItem) {
            PAGE_PERSON_INFO -> {
                v.ivAccountTypeOrganization.disable()
                v.ivAccountTypePersonal.enable()
            }
            PAGE_ORGANIZATION_INFO -> {
                v.ivAccountTypeOrganization.enable()
                v.ivAccountTypePersonal.disable()
            }
        }
    }

    private fun validatePersonInfo(): Boolean {
        val v = view
        return v != null /*&& with(v) {
            tilPersonFirstName.validateAsFirstName() &&
                    tilPersonSecondName.validateAsLastName() &&
                    tilPersonUserName.validateAsUserName() &&
                    tilPersonCity.validateAsCity() &&
                    tilPersonOffers.validateAsOffers() &&
                    tilPersonInterests.validateAsInterests()
        }*/
    }

    private fun validateOrganizationInfo(): Boolean {
        val v = view
        return v != null /*&& with(v) {
            tilCompanyName.validateAsCompanyName() &&
                    tilCompanyUserName.validateAsUserName() &&
                    tilCompanyCity.validateAsCity() &&
                    tilCompanyOffers.validateAsOffers() &&
                    tilCompanyInterests.validateAsInterests()
        }*/
    }

    private fun processRegisterClick() {
        with(requireNotNull(view)) {
            when (vpRegistration.currentItem) {
                PAGE_PERSON_INFO -> if (validatePersonInfo()) viewModel.registerPerson(
                        firstName = etPersonFirstName.text.toString(),
                        secondName = etPersonSecondName.text.toString(),
                        userName = etPersonUserName.text.toString(),
                        city = etPersonCity.text.toString(),
                        offers = listOf(etPersonOffers.text.toString()),
                        interests = listOf(etPersonInterests.text.toString()))
                PAGE_ORGANIZATION_INFO -> if (validateOrganizationInfo()) viewModel.registerOrganization(
                        companyName = etCompanyName.text.toString(),
                        userName = etCompanyUserName.text.toString(),
                        city = etCompanyCity.text.toString(),
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

    class RegistrationAdapter : PagerAdapter() {
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
        }

        private fun onOrganizationPageCreated(view: View) {
            with(view) {
                tilCompanyName.hint = "Company name" //TODO: translation
                tilCompanyUserName.hint = "User name"
                tilCompanyCity.hint = "City"
                tilCompanyOffers.hint = "Offers"
                tilCompanyInterests.hint = "Interests"
            }
        }
    }
}
package com.mnassa.screen.profile.edit.company

import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.View
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.ProfileAccountModel
import com.mnassa.domain.model.TagModel
import com.mnassa.extensions.avatarSquare
import com.mnassa.extensions.formatted
import com.mnassa.google.PlayServiceHelper
import com.mnassa.screen.profile.edit.BaseEditableProfileController
import com.mnassa.screen.profile.model.ProfileModel
import com.mnassa.screen.registration.PlaceAutocompleteAdapter
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.chip_layout.view.*
import kotlinx.android.synthetic.main.controller_edit_company_profile.view.*
import kotlinx.android.synthetic.main.sub_company_info.view.*
import kotlinx.android.synthetic.main.sub_profile_avatar.view.*
import kotlinx.android.synthetic.main.sub_reg_company.view.*
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/28/2018
 */

class EditCompanyProfileController(data: Bundle) : BaseEditableProfileController<EditCompanyProfileViewModel>(data) {
    override val layoutId = R.layout.controller_edit_company_profile
    override val viewModel: EditCompanyProfileViewModel by instance()
    private val accountModel: ProfileAccountModel by lazy { args.getParcelable(EXTRA_PROFILE) as ProfileAccountModel }
    private val interests: java.util.ArrayList<TagModel> by lazy { args.getParcelableArrayList<TagModel>(EXTRA_TAGS_INTERESTS) as java.util.ArrayList<TagModel> }
    private val offers: java.util.ArrayList<TagModel> by lazy { args.getParcelableArrayList<TagModel>(EXTRA_TAGS_OFFERS) as java.util.ArrayList<TagModel> }
    private val playServiceHelper: PlayServiceHelper by instance()

    private var companySelectedPlaceName: String? = null
    private var companySelectedPlaceId: String? = null

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        playServiceHelper.googleApiClient.connect()
        companySelectedPlaceId = accountModel.location?.placeId
        setupView(view)
        view.etFoundation.isLongClickable = false
        view.etFoundation.isFocusableInTouchMode = false
        view.etFoundation.setText(getDateByTimeMillis(accountModel.createdAt))
        timeMillis = accountModel.createdAt
        setCalendarEditText(view.etFoundation)
        view.etCompanyEmail.setHideMode(accountModel.showContactEmail)
        view.etCompanyEmail.setText(accountModel.contactEmail)
        view.etCompanyName.setText(accountModel.organizationInfo?.organizationName)
        view.etCompanyUserName.setText(accountModel.userName)
        view.etCompanyPhone.setHideMode(accountModel.showContactPhone)
        view.etCompanyPhone.setText(accountModel.contactPhone)
        view.etWebSite.setText(accountModel.website)
        view.vCompanyStatus.setOrganization(accountModel.organizationType)
        view.etCompanyNameNotEditable.setText(accountModel.organizationInfo?.organizationName)
        view.chipCompanyOffers.chipSearch = viewModel
        view.chipCompanyOffers.setTags(offers)
        view.chipCompanyInterests.chipSearch = viewModel
        view.chipCompanyInterests.setTags(interests)
        addPhoto(view.fabInfoAddPhoto)
        val placeAutocompleteAdapter = PlaceAutocompleteAdapter(view.context, viewModel)
        view.actvCompanyCity.setText(accountModel.location?.formatted())
        view.actvCompanyCity.setAdapter(placeAutocompleteAdapter)
        view.actvCompanyCity.setOnItemClickListener({ _, _, i, _ ->
            placeAutocompleteAdapter.getItem(i) ?: return@setOnItemClickListener
            companySelectedPlaceId = placeAutocompleteAdapter.getItem(i)?.placeId
            companySelectedPlaceName = "${placeAutocompleteAdapter.getItem(i)?.primaryText} ${placeAutocompleteAdapter.getItem(i)?.secondaryText}"
            view.actvCompanyCity.setText(companySelectedPlaceName ?: "")
        })
        setToolbar(view.toolbarEditProfile, view)
        view.ivUserAvatar.avatarSquare(accountModel.avatar)
        launchCoroutineUI {
            viewModel.openScreenChannel.consumeEach {
                close()
            }
        }
    }

    override fun onViewDestroyed(view: View) {
        super.onViewDestroyed(view)
        if (playServiceHelper.googleApiClient.isConnected) {
            playServiceHelper.googleApiClient.disconnect()
        }
        super.onDestroy()
    }

    override fun photoResult(uri: Uri, view: View) {
        view.ivUserAvatar?.avatarSquare(uri)
        viewModel.saveLocallyAvatarUri(uri)
    }

    override fun proccesProfile(view: View) {
        val email = view.etCompanyEmail.text.toString()
        val phone = view.etCompanyPhone.text.toString()
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() && email.isNotBlank()) {
            view.etCompanyEmail.error = fromDictionary(R.string.email_is_not_valid)
            return
        }
        if (!Patterns.PHONE.matcher(phone).matches() && phone.isNotEmpty()) {
            view.etCompanyPhone.error = fromDictionary(R.string.phone_is_not_valid)
            return
        }
        if (view.etCompanyUserName.text.isBlank()) {
            view.etCompanyUserName.error = fromDictionary(R.string.user_name_is_not_valid)
            return
        }
        if (view.etCompanyName.text.isBlank()) {
            view.etCompanyName.error = fromDictionary(R.string.company_name_is_not_valid)
            return
        }
        viewModel.updateCompanyAccount(
                profileAccountModel = accountModel,
                userName = view.etCompanyUserName.text.toString(),
                companyName = view.etCompanyName.text.toString(),
                showContactEmail = view.etCompanyEmail.isChosen,
                showContactPhone = view.etCompanyPhone.isChosen,
                contactEmail = view.etCompanyEmail.text.toString(),
                contactPhone = view.etCompanyPhone.text.toString(),
                founded = timeMillis,
                organizationType = view.vCompanyStatus.getOrganizationType(),
                website = view.etWebSite.text.toString(),
                foundedDate = view.etFoundation.text.toString(),
                locationId = companySelectedPlaceId,
                interests = view.chipCompanyInterests.getTags(),
                offers = view.chipCompanyOffers.getTags()
        )
    }

    private fun setupView(view: View) {
        view.toolbarEditProfile.title = fromDictionary(R.string.edit_profile_title)
        view.tilCompanyNameNotEditable.hint = fromDictionary(R.string.reg_company_name)
        view.tilCompanyName.hint = fromDictionary(R.string.reg_account_company_name)
        view.tilCompanyUserName.hint = fromDictionary(R.string.reg_personal_user_name)
        view.tilCompanyCity.hint = fromDictionary(R.string.reg_personal_city)
        view.chipCompanyOffers.etChipInput.hint = fromDictionary(R.string.reg_person_type_here)
        view.chipCompanyOffers.tvChipHeader.text = fromDictionary(R.string.reg_account_can_help_with)
        view.chipCompanyInterests.etChipInput.hint = fromDictionary(R.string.reg_person_type_here)
        view.chipCompanyInterests.tvChipHeader.text = fromDictionary(R.string.reg_account_interested_in)
        view.tilCompanyCity.hint = fromDictionary(R.string.reg_personal_city)
        view.tilWebSite.hint = fromDictionary(R.string.reg_company_website)
        view.tilCompanyEmail.hint = fromDictionary(R.string.reg_info_email)
        view.tilCompanyPhone.hint = fromDictionary(R.string.reg_info_phone_number)
        view.tilFoundation.hint = fromDictionary(R.string.reg_company_founded)
    }

    companion object {
        private const val EXTRA_PROFILE = "EXTRA_PROFILE"
        private const val EXTRA_TAGS_INTERESTS = "EXTRA_TAGS_INTERESTS"
        private const val EXTRA_TAGS_OFFERS = "EXTRA_TAGS_OFFERS"

        fun newInstance(profileModel: ProfileModel): EditCompanyProfileController {
            val params = Bundle()
            params.putParcelable(EXTRA_PROFILE, profileModel.profile)
            params.putParcelableArrayList(EXTRA_TAGS_INTERESTS, profileModel.interests as ArrayList<out TagModel>)
            params.putParcelableArrayList(EXTRA_TAGS_OFFERS, profileModel.offers as ArrayList<out TagModel>)
            return EditCompanyProfileController(params)
        }
    }

}
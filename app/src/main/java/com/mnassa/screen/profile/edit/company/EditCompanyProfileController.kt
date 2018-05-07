package com.mnassa.screen.profile.edit.company

import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.View
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.ProfileAccountModel
import com.mnassa.domain.model.TagModel
import com.mnassa.extensions.PATTERN_PHONE_TAIL
import com.mnassa.extensions.SimpleTextWatcher
import com.mnassa.extensions.avatarSquare
import com.mnassa.extensions.formatted
import com.mnassa.helper.PlayServiceHelper
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
import org.kodein.di.generic.instance

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
        set(value) {
            field = value
            onOrganizationChanged()
        }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        playServiceHelper.googleApiClient.connect()
        companySelectedPlaceId = accountModel.location?.placeId
        setupView(view)
        launchCoroutineUI {
            viewModel.openScreenChannel.consumeEach {
                close()
            }
        }
        with(view) {
            etFoundation.isLongClickable = false
            etFoundation.isFocusableInTouchMode = false
            etFoundation.setText(getDateByTimeMillis(accountModel.createdAt))
            birthday = accountModel.createdAt
            setCalendarEditText(etFoundation)
            etCompanyEmail.setHideMode(accountModel.showContactEmail)
            etCompanyEmail.setText(accountModel.contactEmail)
            etCompanyName.setText(accountModel.organizationInfo?.organizationName)
            etCompanyUserName.setText(accountModel.userName)
            etCompanyPhone.setHideMode(accountModel.showContactPhone)
            etCompanyPhone.setText(accountModel.contactPhone?.replace("+", ""))
            etWebSite.setText(accountModel.website)
            vCompanyStatus.setOrganization(accountModel.organizationType)
            etCompanyNameNotEditable.setText(accountModel.organizationInfo?.organizationName)
            chipCompanyOffers.chipSearch = viewModel
            chipCompanyOffers.setTags(offers)
            chipCompanyInterests.chipSearch = viewModel
            chipCompanyInterests.setTags(interests)
            addPhoto(fabInfoAddPhoto)
            val placeAutocompleteAdapter = PlaceAutocompleteAdapter(view.context, viewModel)
            actvCompanyCity.setText(accountModel.location?.formatted())
            actvCompanyCity.setAdapter(placeAutocompleteAdapter)
            actvCompanyCity.setOnItemClickListener { _, _, i, _ ->
                val item = placeAutocompleteAdapter.getItem(i) ?: return@setOnItemClickListener
                companySelectedPlaceId = item.placeId
                companySelectedPlaceName = "${item.primaryText} ${item.secondaryText}"
                actvCompanyCity.setText(companySelectedPlaceName ?: "")
            }
            setToolbar(toolbarEditProfile, this)
            ivUserAvatar.avatarSquare(accountModel.avatar)
            etCompanyName.addTextChangedListener(SimpleTextWatcher { onOrganizationChanged() })
            etCompanyUserName.addTextChangedListener(SimpleTextWatcher { onOrganizationChanged() })
            chipCompanyInterests.chipsChangeListener = { onOrganizationChanged() }
            chipCompanyOffers.chipsChangeListener = { onOrganizationChanged() }
        }
    }

    private fun onOrganizationChanged() {
        val view = view ?: return
        view.toolbarEditProfile.actionButtonEnabled = canCreateOrganizationInfo()
    }

    private fun canCreateOrganizationInfo(): Boolean {
        with(view ?: return false) {
            if (etCompanyName.text.isBlank()) return false
            if (etCompanyUserName.text.isBlank()) return false
            if (companySelectedPlaceId == null) return false
            if (chipCompanyOffers.getTags().isEmpty()) return false
            if (chipCompanyInterests.getTags().isEmpty()) return false
        }
        return true
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
        if (!PATTERN_PHONE_TAIL.matcher(phone).matches() && phone.isNotEmpty()) {
            view.etCompanyPhone.error = fromDictionary(R.string.phone_is_not_valid)
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
                founded = birthday,
                organizationType = view.vCompanyStatus.getOrganizationType(),
                website = view.etWebSite.text.toString(),
                foundedDate = view.etFoundation.text.toString(),
                locationId = companySelectedPlaceId,
                interests = view.chipCompanyInterests.getTags(),
                offers = view.chipCompanyOffers.getTags()
        )
    }

    private fun setupView(view: View) {
        with(view) {
            toolbarEditProfile.title = fromDictionary(R.string.edit_profile_title)
            tilCompanyNameNotEditable.hint = fromDictionary(R.string.reg_company_name)
            tilCompanyName.hint = fromDictionary(R.string.reg_account_company_name)
            tilCompanyUserName.hint = fromDictionary(R.string.reg_personal_user_name)
            tilCompanyCity.hint = fromDictionary(R.string.reg_personal_city)
            chipCompanyOffers.etChipInput.hint = fromDictionary(R.string.reg_person_type_here)
            chipCompanyOffers.tvChipHeader.text = fromDictionary(R.string.reg_account_can_help_with)
            chipCompanyInterests.etChipInput.hint = fromDictionary(R.string.reg_person_type_here)
            chipCompanyInterests.tvChipHeader.text = fromDictionary(R.string.reg_account_interested_in)
            tilCompanyCity.hint = fromDictionary(R.string.reg_personal_city)
            tilWebSite.hint = fromDictionary(R.string.reg_company_website)
            tilCompanyEmail.hint = fromDictionary(R.string.reg_info_email)
            tilCompanyPhone.hint = fromDictionary(R.string.reg_info_phone_number)
            tilFoundation.hint = fromDictionary(R.string.reg_company_founded)
        }
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
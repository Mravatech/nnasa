package com.mnassa.screen.profile.edit.personal

import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.View
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.Gender
import com.mnassa.domain.model.ProfileAccountModel
import com.mnassa.domain.model.TagModel
import com.mnassa.extensions.avatarSquare
import com.mnassa.extensions.formatted
import com.mnassa.helper.PlayServiceHelper
import com.mnassa.screen.profile.edit.BaseEditableProfileController
import com.mnassa.screen.profile.model.ProfileModel
import com.mnassa.screen.registration.PlaceAutocompleteAdapter
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.chip_layout.view.*
import kotlinx.android.synthetic.main.controller_edit_personal_profile.view.*
import kotlinx.android.synthetic.main.sub_personal_info.view.*
import kotlinx.android.synthetic.main.sub_profile_avatar.view.*
import kotlinx.android.synthetic.main.sub_reg_personal.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import java.util.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/26/2018
 */

class EditPersonalProfileController(data: Bundle) : BaseEditableProfileController<EditPersonalProfileViewModel>(data) {

    override val layoutId = R.layout.controller_edit_personal_profile
    override val viewModel: EditPersonalProfileViewModel by instance()
    private val accountModel: ProfileAccountModel by lazy { args.getParcelable(EXTRA_PROFILE) as ProfileAccountModel }
    private val interests: ArrayList<TagModel> by lazy { args.getParcelableArrayList<TagModel>(EXTRA_TAGS_INTERESTS) as ArrayList<TagModel> }
    private val offers: ArrayList<TagModel> by lazy { args.getParcelableArrayList<TagModel>(EXTRA_TAGS_OFFERS) as ArrayList<TagModel> }
    private val playServiceHelper: PlayServiceHelper by instance()
    private var personSelectedPlaceName: String? = null
    private var personSelectedPlaceId: String? = null

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        setupViews(view)
        playServiceHelper.googleApiClient.connect()
        personSelectedPlaceId = accountModel.location?.placeId
        view.rInfoBtnFemale.isChecked = accountModel.gender == Gender.MALE
        view.rInfoBtnMale.isChecked = accountModel.gender == Gender.MALE
        val placeAutocompleteAdapter = PlaceAutocompleteAdapter(view.context, viewModel)
        view.actvPersonCity.setText(accountModel.location?.formatted())
        view.actvPersonCity.setAdapter(placeAutocompleteAdapter)
        view.actvPersonCity.setOnItemClickListener({ _, _, i, _ ->
            placeAutocompleteAdapter.getItem(i) ?: return@setOnItemClickListener
            personSelectedPlaceId = placeAutocompleteAdapter.getItem(i)?.placeId
            personSelectedPlaceName = "${placeAutocompleteAdapter.getItem(i)?.primaryText} ${placeAutocompleteAdapter.getItem(i)?.secondaryText}"
            view.actvPersonCity.setText(personSelectedPlaceName ?: "")
        })
        view.containerSelectOccupation.setAbilities(accountModel.abilities)
        view.etPhoneNumber.setText(accountModel.contactPhone)
        view.etPhoneNumber.setHideMode(accountModel.showContactEmail)
        view.etYourEmail.setText(accountModel.contactEmail)
        view.etYourEmail.setHideMode(accountModel.showContactPhone)
        addPhoto(view.fabInfoAddPhoto)
        view.etDateOfBirthday.setText(getDateByTimeMillis(accountModel.createdAt))
        view.chipPersonInterests.chipSearch = viewModel
        view.chipPersonInterests.setTags(interests)
        view.chipPersonOffers.chipSearch = viewModel
        view.chipPersonOffers.setTags(offers)
        view.etPersonFirstName.setText(accountModel.personalInfo?.firstName)
        view.etPersonSecondName.setText(accountModel.personalInfo?.lastName)
        view.etPersonUserName.setText(accountModel.userName)
        setToolbar(view.toolbarEditProfile, view)
        timeMillis = accountModel.createdAt
        setCalendarEditText(view.etDateOfBirthday)
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
        val email = view.etYourEmail.text.toString()
        val phone = view.etPhoneNumber.text.toString()
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() && email.isNotBlank()) {
            view.etYourEmail.error = fromDictionary(R.string.email_is_not_valid)
            return
        }
        if (!Patterns.PHONE.matcher(phone).matches() && phone.isNotEmpty()) {
            view.etPhoneNumber.error = fromDictionary(R.string.phone_is_not_valid)
            return
        }
        if (view.etPersonFirstName.text.isBlank()) {
            view.etPersonFirstName.error = fromDictionary(R.string.person_name_is_not_valid)
            return
        }
        if (view.etPersonSecondName.text.isBlank()) {
            view.etPersonSecondName.error = fromDictionary(R.string.person_last_name_is_not_valid)
            return
        }
        if (view.etPersonUserName.text.isBlank()) {
            view.etPersonUserName.error = fromDictionary(R.string.user_name_is_not_valid)
            return
        }
        viewModel.updatePersonalAccount(
                profileAccountModel = accountModel,
                firstName = view.etPersonFirstName.text.toString(),
                secondName = view.etPersonSecondName.text.toString(),
                userName = view.etPersonUserName.text.toString(),
                showContactEmail = view.etYourEmail.isChosen,
                contactEmail = view.etYourEmail.text.toString(),
                showContactPhone = view.etPhoneNumber.isChosen,
                contactPhone = view.etPhoneNumber.text.toString(),
                birthday = timeMillis,
                birthdayDate = view.etDateOfBirthday.text.toString(),
                locationId = personSelectedPlaceId,
                isMale = view.rInfoBtnMale.isChecked,
                abilities = view.containerSelectOccupation.getAllAbilities(),
                interests = view.chipPersonInterests.getTags(),
                offers = view.chipPersonOffers.getTags()
        )
    }

    private fun setupViews(view: View) {
        view.toolbarEditProfile.title = fromDictionary(R.string.edit_profile_title)
        view.tvEditProfileMoreInfo.text = fromDictionary(R.string.edit_profile_main_info)
        view.chipPersonOffers.tvChipHeader.text = fromDictionary(R.string.reg_account_can_help_with)
        view.chipPersonOffers.etChipInput.hint = fromDictionary(R.string.reg_person_type_here)
        view.chipPersonInterests.tvChipHeader.text = fromDictionary(R.string.reg_account_interested_in)
        view.chipPersonInterests.etChipInput.hint = fromDictionary(R.string.reg_person_type_here)
        view.tilPersonFirstName.hint = fromDictionary(R.string.reg_personal_first_name)
        view.tilPersonSecondName.hint = fromDictionary(R.string.reg_personal_last_name)
        view.tilPersonUserName.hint = fromDictionary(R.string.reg_personal_user_name)
        view.tilPersonCity.hint = fromDictionary(R.string.edit_profile_city)
        view.tvInfoGender.hint = fromDictionary(R.string.reg_person_info_gender)
        view.tvProfilePersonalInfo.text = fromDictionary(R.string.reg_person_info_title)
        view.rInfoBtnFemale.text = fromDictionary(R.string.reg_person_info_female_gender)
        view.rInfoBtnMale.text = fromDictionary(R.string.reg_person_info_male_gender)
        view.tilDateOfBirthday.hint = fromDictionary(R.string.reg_person_info_birthday)
        view.tilPhoneNumber.hint = fromDictionary(R.string.reg_info_phone_number)
        view.tvInfoGender.text = fromDictionary(R.string.reg_person_info_gender)
        view.rInfoBtnMale.text = fromDictionary(R.string.reg_person_info_male_gender)
        view.rInfoBtnFemale.text = fromDictionary(R.string.reg_person_info_female_gender)
        view.tilYourEmail.hint = fromDictionary(R.string.reg_info_email)
    }

    companion object {
        private const val EXTRA_PROFILE = "EXTRA_PROFILE"
        private const val EXTRA_TAGS_INTERESTS = "EXTRA_TAGS_INTERESTS"
        private const val EXTRA_TAGS_OFFERS = "EXTRA_TAGS_OFFERS"

        fun newInstance(profileModel: ProfileModel): EditPersonalProfileController {
            val params = Bundle()
            params.putParcelable(EXTRA_PROFILE, profileModel.profile)
            params.putParcelableArrayList(EXTRA_TAGS_INTERESTS, profileModel.interests as ArrayList<out TagModel>)
            params.putParcelableArrayList(EXTRA_TAGS_OFFERS, profileModel.offers as ArrayList<out TagModel>)
            return EditPersonalProfileController(params)
        }
    }

}


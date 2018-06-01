package com.mnassa.screen.profile.edit.personal

import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.View
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.Gender
import com.mnassa.domain.model.ProfileAccountModel
import com.mnassa.domain.model.TagModel
import com.mnassa.extensions.*
import com.mnassa.helper.PlayServiceHelper
import com.mnassa.screen.profile.edit.BaseEditableProfileController
import com.mnassa.screen.registration.PlaceAutocompleteAdapter
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.chip_layout.view.*
import kotlinx.android.synthetic.main.controller_edit_personal_profile.view.*
import kotlinx.android.synthetic.main.sub_personal_info.view.*
import kotlinx.android.synthetic.main.sub_profile_avatar.view.*
import kotlinx.android.synthetic.main.sub_reg_personal.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import org.kodein.di.generic.instance
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
        set(value) {
            field = value
            onPersonChanged()
        }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        setupViews(view)
        playServiceHelper.googleApiClient.connect()
        personSelectedPlaceId = accountModel.location?.placeId

        launchCoroutineUI {
            viewModel.openScreenChannel.consumeEach {
                close()
            }
        }
        with(view) {
            rInfoBtnFemale.isChecked = accountModel.gender == Gender.FEMALE
            rInfoBtnMale.isChecked = accountModel.gender == Gender.MALE
            val placeAutocompleteAdapter = PlaceAutocompleteAdapter(view.context, viewModel)
            actvPersonCity.setText(accountModel.location?.formatted())
            actvPersonCity.setAdapter(placeAutocompleteAdapter)
            actvPersonCity.setOnItemClickListener { _, _, i, _ ->
                val item = placeAutocompleteAdapter.getItem(i) ?: return@setOnItemClickListener
                personSelectedPlaceId = item.placeId
                personSelectedPlaceName = "${item.primaryText} ${item.secondaryText}"
                actvPersonCity.setText(personSelectedPlaceName ?: "")
            }
            containerSelectOccupation.setAbilities(accountModel.abilities)
            etPhoneNumber.setText(accountModel.contactPhone)
            etPhoneNumber.setHideMode(accountModel.showContactPhone)
            etYourEmail.setText(accountModel.contactEmail)
            etYourEmail.setHideMode(accountModel.showContactEmail)
            addPhoto(fabInfoAddPhoto)
            etDateOfBirthday.setText(getDateByTimeMillis(accountModel.createdAt))
            chipPersonInterests.chipSearch = viewModel
            chipPersonInterests.setTags(interests)
            chipPersonOffers.chipSearch = viewModel
            chipPersonOffers.setTags(offers)
            etPersonFirstName.setText(accountModel.personalInfo?.firstName)
            etPersonSecondName.setText(accountModel.personalInfo?.lastName)
            etPersonUserName.setText(accountModel.userName)
            setToolbar(toolbarEditProfile, this)
            birthday = accountModel.createdAt
            setCalendarEditText(etDateOfBirthday)
            ivUserAvatar.avatarSquare(accountModel.avatar)
            chipPersonOffers.onChipsChangeListener = { onPersonChanged() }
            chipPersonInterests.onChipsChangeListener = { onPersonChanged() }
            etPersonFirstName.addTextChangedListener(SimpleTextWatcher { onPersonChanged() })
            etPersonSecondName.addTextChangedListener(SimpleTextWatcher { onPersonChanged() })
            etPersonUserName.addTextChangedListener(SimpleTextWatcher { onPersonChanged() })
            onPersonChanged()
        }
    }

    private fun onPersonChanged() {
        val view = view ?: return
        view.toolbarEditProfile.actionButtonEnabled = canCreatePersonInfo()
    }

    private fun canCreatePersonInfo(): Boolean {
        with(view ?: return false) {
            if (etPersonFirstName.text.isBlank()) return false
            if (etPersonSecondName.text.isBlank()) return false
            if (etPersonUserName.text.isBlank()) return false
            if (personSelectedPlaceId == null) return false
            if (chipPersonOffers.getTags().isEmpty()) return false
            if (chipPersonInterests.getTags().isEmpty()) return false
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
        val email = view.etYourEmail.text.toString()
        val phone = view.etPhoneNumber.text.toString()
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() && email.isNotBlank()) {
            view.etYourEmail.error = fromDictionary(R.string.email_is_not_valid)
            return
        }
        if (!PATTERN_PHONE.matcher(phone).matches() && phone.isNotEmpty()) {
            view.etPhoneNumber.error = fromDictionary(R.string.phone_is_not_valid)
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
                birthday = birthday,
                birthdayDate = view.etDateOfBirthday.text.toString(),
                locationId = personSelectedPlaceId,
                isMale = view.rInfoBtnMale.isChecked,
                abilities = view.containerSelectOccupation.getAllAbilities(),
                interests = view.chipPersonInterests.getTags(),
                offers = view.chipPersonOffers.getTags()
        )
    }

    private fun setupViews(view: View) {
        with(view) {
            toolbarEditProfile.title = fromDictionary(R.string.edit_profile_title)
            tvEditProfileMoreInfo.text = fromDictionary(R.string.edit_profile_main_info)
            chipPersonOffers.tvChipHeader.text = fromDictionary(R.string.reg_account_can_help_with)
            chipPersonOffers.etChipInput.hint = fromDictionary(R.string.reg_person_type_here)
            chipPersonInterests.tvChipHeader.text = fromDictionary(R.string.reg_account_interested_in)
            chipPersonInterests.etChipInput.hint = fromDictionary(R.string.reg_person_type_here)
            tilPersonFirstName.hint = fromDictionary(R.string.reg_personal_first_name)
            tilPersonSecondName.hint = fromDictionary(R.string.reg_personal_last_name)
            tilPersonUserName.hint = fromDictionary(R.string.reg_personal_user_name)
            tilPersonCity.hint = fromDictionary(R.string.edit_profile_city)
            tvInfoGender.hint = fromDictionary(R.string.reg_person_info_gender)
            tvProfilePersonalInfo.text = fromDictionary(R.string.reg_person_info_title)
            rInfoBtnFemale.text = fromDictionary(R.string.reg_person_info_female_gender)
            rInfoBtnMale.text = fromDictionary(R.string.reg_person_info_male_gender)
            tilDateOfBirthday.hint = fromDictionary(R.string.reg_person_info_birthday)
            tilPhoneNumber.hint = fromDictionary(R.string.reg_info_phone_number)
            tvInfoGender.text = fromDictionary(R.string.reg_person_info_gender)
            rInfoBtnMale.text = fromDictionary(R.string.reg_person_info_male_gender)
            rInfoBtnFemale.text = fromDictionary(R.string.reg_person_info_female_gender)
            tilYourEmail.hint = fromDictionary(R.string.reg_info_email)
        }
    }

    companion object {
        private const val EXTRA_PROFILE = "EXTRA_PROFILE"
        private const val EXTRA_TAGS_INTERESTS = "EXTRA_TAGS_INTERESTS"
        private const val EXTRA_TAGS_OFFERS = "EXTRA_TAGS_OFFERS"

        fun newInstance(profile: ProfileAccountModel, offers: List<TagModel>, interests: List<TagModel>): EditPersonalProfileController {
            val params = Bundle()
            params.putParcelable(EXTRA_PROFILE, profile)
            params.putParcelableArrayList(EXTRA_TAGS_INTERESTS, interests as ArrayList<out TagModel>)
            params.putParcelableArrayList(EXTRA_TAGS_OFFERS, offers as ArrayList<out TagModel>)
            return EditPersonalProfileController(params)
        }
    }

}


package com.mnassa.screen.profile.edit.personal

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.mnassa.App
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
import kotlinx.android.synthetic.main.sub_reg_personal.*
import kotlinx.android.synthetic.main.sub_reg_personal.view.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.consumeEach
import org.kodein.di.generic.instance
import timber.log.Timber
import java.util.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/26/2018
 */

class EditPersonalProfileController(data: Bundle) : BaseEditableProfileController<EditPersonalProfileViewModel>(data) {




    override val layoutId = R.layout.controller_edit_personal_profile
    override val viewModel: EditPersonalProfileViewModel by instance()
    private val playServiceHelper: PlayServiceHelper by instance()
    private var personSelectedPlaceName: String? = null
    private var personSelectedPlaceId: String? = null
        set(value) {
            field = value
            onPersonChanged()
        }

    private lateinit var firstName: String
    private lateinit var secondName: String

    private val actvPersonCityError by lazy { fromDictionary(R.string.reg_person_address_error) }

    private var actvPersonCityUserChanged = false

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        setupViews(view)



        playServiceHelper.googleApiClient.connect()
        actvPersonCityUserChanged = false
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
            actvPersonCity.addTextChangedListener {
                actvPersonCityUserChanged = true
                personSelectedPlaceId = null
            }
            actvPersonCity.setOnItemClickListener { _, _, i, _ ->
                val item = placeAutocompleteAdapter.getItem(i) ?: return@setOnItemClickListener
                personSelectedPlaceName = "${item.primaryText} ${item.secondaryText}"
                    .also {
                        actvPersonCity.setText(it)
                    }
                personSelectedPlaceId = item.placeId
            }
            containerSelectOccupation.setAbilities(accountModel.abilities)
            etPhoneNumber.setText(accountModel.contactPhone)
            etPhoneNumber.setHideMode(accountModel.showContactPhone)
            etYourEmail.setText(accountModel.contactEmail)
            etYourEmail.setHideMode(accountModel.showContactEmail)
            addPhoto(fabInfoAddPhoto)
            etDateOfBirthday.setText(getDateByTimeMillis(accountModel.birthday?.time ?: 0L))
            chipPersonInterests.setTags(interests)
            chipPersonOffers.setTags(offers)

            etPersonFirstName.setText(accountModel.personalInfo?.firstName)
            etPersonSecondName.setText(accountModel.personalInfo?.lastName)

            etPersonUserName.setText(accountModel.userName)
            etPersonUserName.isEnabled = false
            setToolbar(toolbarEditProfile, this)
            birthday = accountModel.birthday?.time ?: 0L
            setCalendarEditText(etDateOfBirthday)
            ivUserAvatar.avatarSquare(accountModel.avatar)
            chipPersonOffers.onChipsChangeListener = { onPersonChanged() }
            chipPersonInterests.onChipsChangeListener = { onPersonChanged() }
            etPersonFirstName.addTextChangedListener(SimpleTextWatcher {
                onPersonChanged()
                firstName = etPersonFirstName.text.toString()
                secondName = etPersonSecondName.text.toString()

                getUserDetails()})
            etPersonSecondName.addTextChangedListener(SimpleTextWatcher { onPersonChanged()
                secondName = etPersonSecondName.text.toString()
                firstName = etPersonFirstName.text.toString()
                getUserDetails()
            })
            etPersonUserName.addTextChangedListener(SimpleTextWatcher { onPersonChanged() })
            onPersonChanged()
        }
    }

    private fun getUserDetails(){
        val prefs = App.context.getSharedPreferences("name-shared-pref", Context.MODE_PRIVATE).edit()
        prefs.putString("fname", firstName)
        prefs.putString("sname", secondName)
        prefs.apply()

        Log.d("assert", "$firstName $secondName")
    }

    private var onPersonChangedJob: Job? = null
    private fun onPersonChanged() {

        onPersonChangedJob?.cancel()
        onPersonChangedJob = launchCoroutineUI {
            getViewSuspend().toolbarEditProfile.actionButtonClickable = canCreatePersonInfo()
        }
    }

    private suspend fun canCreatePersonInfo(): Boolean {
        var isValid = true
        with(view ?: return false) {
            if (personSelectedPlaceId == null) {
                tilPersonCity.error = actvPersonCityError.takeIf { actvPersonCityUserChanged }
                isValid = false
            } else {
                tilPersonCity.error = null
            }

            if (etPersonFirstName.text.isNullOrBlank()) return false
            if (etPersonSecondName.text.isNullOrBlank()) return false
            if (etPersonUserName.text.isNullOrBlank()) return false
            if (viewModel.isOffersMandatory() && chipPersonOffers.getTags().isEmpty()) return false
            if (viewModel.isInterestsMandatory() && chipPersonInterests.getTags().isEmpty()) return false


        }
        return isValid
    }

    override fun onViewDestroyed(view: View) {
        super.onViewDestroyed(view)
        if (playServiceHelper.googleApiClient.isConnected) {
            playServiceHelper.googleApiClient.disconnect()
        }
    }

    override fun photoResult(uri: Uri, view: View) {
        view.ivUserAvatar?.avatarSquare(uri)
        viewModel.saveLocallyAvatarUri(uri)
    }

    override suspend fun processProfile(view: View) {
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
                interests = getEnteredInterests(),
                offers = getEnteredOffers()
        )

    }

    override suspend fun getEnteredInterests(): List<TagModel> = getViewSuspend().chipPersonInterests.getTags()

    override suspend fun getEnteredOffers(): List<TagModel> = getViewSuspend().chipPersonOffers.getTags()

    private fun setupViews(view: View) {

        with(view) {
            toolbarEditProfile.title = fromDictionary(R.string.edit_profile_title)
            tvEditProfileMoreInfo.text = fromDictionary(R.string.edit_profile_main_info)

            launchCoroutineUI {
                chipPersonOffers.tvChipHeader.text = formatTagLabel(fromDictionary(R.string.reg_account_can_help_with))
                chipPersonInterests.tvChipHeader.text = formatTagLabel(fromDictionary(R.string.reg_account_interested_in))
            }

            chipPersonOffers.etChipInput.hint = fromDictionary(R.string.reg_person_type_here)
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
    }//

    companion object {

        fun newInstance(profile: ProfileAccountModel, offers: List<TagModel>, interests: List<TagModel>): EditPersonalProfileController {
            val params = Bundle()
            params.putSerializable(EXTRA_PROFILE, profile)
            params.putParcelableArrayList(EXTRA_TAGS_INTERESTS, interests as ArrayList<out TagModel>)
            params.putParcelableArrayList(EXTRA_TAGS_OFFERS, offers as ArrayList<out TagModel>)
            return EditPersonalProfileController(params)
        }
    }

}


package com.mnassa.screen.profile.edit.personal

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.util.Patterns
import android.view.View
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.activity.CropActivity
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.dialog.DialogHelper
import com.mnassa.domain.model.ProfileAccountModel
import com.mnassa.domain.model.TagModel
import com.mnassa.extensions.avatarSquare
import com.mnassa.google.PlayServiceHelper
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.profile.model.ProfileModel
import com.mnassa.screen.registration.PlaceAutocompleteAdapter
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.chip_layout.view.*
import kotlinx.android.synthetic.main.controller_edit_personal_profile.view.*
import kotlinx.android.synthetic.main.header_main.view.*
import kotlinx.android.synthetic.main.sub_personal_info.view.*
import kotlinx.android.synthetic.main.sub_profile_avatar.view.*
import kotlinx.android.synthetic.main.sub_reg_personal.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import timber.log.Timber
import java.text.DateFormatSymbols
import java.util.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/26/2018
 */

class EditPersonalProfileController(data: Bundle) : MnassaControllerImpl<EditPersonalProfileViewModel>(data) {

    override val layoutId = R.layout.controller_edit_personal_profile
    override val viewModel: EditPersonalProfileViewModel by instance()
    private val accountModel: ProfileAccountModel by lazy { args.getParcelable(EXTRA_PROFILE) as ProfileAccountModel }
    private val interests: ArrayList<TagModel> by lazy { args.getParcelableArrayList<TagModel>(EXTRA_TAGS_INTERESTS) as ArrayList<TagModel> }
    private val offers: ArrayList<TagModel> by lazy { args.getParcelableArrayList<TagModel>(EXTRA_TAGS_OFFERS) as ArrayList<TagModel> }
    private val playServiceHelper: PlayServiceHelper by instance()
    private val dialog: DialogHelper by instance()
    private var personSelectedPlaceName: String? = null
    private var personSelectedPlaceId: String? = null
    private var timeMillis: Long? = null

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        playServiceHelper.googleApiClient.connect()
        personSelectedPlaceId = accountModel.location?.placeId
        onActivityResult.subscribe {
            when (it.requestCode) {
                REQUEST_CODE_CROP -> {
                    when (it.resultCode) {
                        Activity.RESULT_OK -> {
                            val uri: Uri? = it.data?.getParcelableExtra(CropActivity.URI_PHOTO_RESULT)
                            uri?.let {
                                viewModel.uploadPhotoToStorage(it)
                            } ?: run {
                                Timber.i("uri is null")
                            }
                        }
                        CropActivity.GET_PHOTO_ERROR -> {
                            Timber.i("CropActivity.GET_PHOTO_ERROR")
                        }
                    }
                }
            }
        }
        setupViews(view)
        view.fabInfoAddPhoto.setOnClickListener {
            dialog.showSelectImageSourceDialog(it.context) { imageSource ->
                launchCoroutineUI {
                    activity?.let {
                        if (CropActivity.ImageSource.CAMERA == imageSource) {
                            val permissionsResult = permissions.requestPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            if (!permissionsResult.isAllGranted) {
                                return@launchCoroutineUI
                            }
                        }
                        val intent = CropActivity.start(imageSource, it)
                        startActivityForResult(intent, REQUEST_CODE_CROP)
                    }
                }
            }
        }
        view.etDateOfBirthday.isLongClickable = false
        view.etDateOfBirthday.isFocusableInTouchMode = false
        timeMillis = accountModel.createdAt
        view.etDateOfBirthday.setOnClickListener {
            dialog.calendarDialog(view.context, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                view.etDateOfBirthday.setText("${DateFormatSymbols().months[month]} $dayOfMonth, $year")
                val cal = Calendar.getInstance()
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                cal.set(Calendar.MONTH, month)
                cal.set(Calendar.YEAR, year)
                timeMillis = cal.timeInMillis
            })
        }
        launchCoroutineUI {
            viewModel.imageUploadedChannel.consumeEach {
                view.ivUserAvatar.avatarSquare(it)
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

    private fun updateProfile(view: View) {
        val email = view.etYourEmail.text.toString()
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() && email.isNotBlank()) {
            view.etYourEmail.error = fromDictionary(R.string.email_is_not_valid)
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
        view.toolbarEditProfile.onMoreClickListener = { updateProfile(view) }
        view.toolbarEditProfile.backButtonEnabled = true
        view.toolbarEditProfile.ivToolbarMore.setImageResource(R.drawable.ic_check)
        view.toolbarEditProfile.ivToolbarMore.setColorFilter(ContextCompat.getColor(view.context, R.color.turquoiseBlue), android.graphics.PorterDuff.Mode.SRC_IN)
        view.toolbarEditProfile.ivToolbarMore.visibility = View.VISIBLE
        view.tvEditProfileMoreInfo.text = fromDictionary(R.string.edit_profile_main_info)
        view.chipPersonOffers.tvChipHeader.text = fromDictionary(R.string.reg_account_can_help_with)
        view.chipPersonOffers.etChipInput.hint = fromDictionary(R.string.reg_person_type_here)
        view.chipPersonOffers.chipSearch = viewModel
        view.chipPersonOffers.setTags(offers)
        view.chipPersonInterests.tvChipHeader.text = fromDictionary(R.string.reg_account_interested_in)
        view.chipPersonInterests.etChipInput.hint = fromDictionary(R.string.reg_person_type_here)
        view.chipPersonInterests.chipSearch = viewModel
        view.chipPersonInterests.setTags(interests)
        view.etPersonFirstName.setText(accountModel.personalInfo?.firstName)
        view.etPersonSecondName.setText(accountModel.personalInfo?.lastName)
        view.etPersonUserName.setText(accountModel.userName)
        view.tilPersonFirstName.hint = fromDictionary(R.string.reg_personal_first_name)
        view.tilPersonSecondName.hint = fromDictionary(R.string.reg_personal_last_name)
        view.tilPersonUserName.hint = fromDictionary(R.string.reg_personal_user_name)
        view.ivUserAvatar.avatarSquare(accountModel.avatar)
        view.etDateOfBirthday.setText(getDateByTimeMillis(accountModel.createdAt))
        view.tilPersonCity.hint = fromDictionary(R.string.edit_profile_city)
        view.etPhoneNumber.setText(accountModel.contactPhone)
        view.etPhoneNumber.setHideMode(accountModel.showContactEmail ?: false)
        view.etYourEmail.setText(accountModel.contactEmail)
        view.etYourEmail.setHideMode(accountModel.showContactPhone ?: false)
        view.tvInfoGender.hint = fromDictionary(R.string.reg_person_info_gender)
        view.tvProfilePersonalInfo.text = fromDictionary(R.string.reg_person_info_title)
        view.rInfoBtnFemale.text = fromDictionary(R.string.reg_person_info_female_gender)
        view.rInfoBtnMale.text = fromDictionary(R.string.reg_person_info_male_gender)
        view.rInfoBtnFemale.isChecked = accountModel.gender.name.toLowerCase() == view.rInfoBtnMale.text.toString().toLowerCase()
        view.rInfoBtnMale.isChecked = accountModel.gender.name.toLowerCase() == view.rInfoBtnMale.text.toString().toLowerCase()
        val placeAutocompleteAdapter = PlaceAutocompleteAdapter(view.context, viewModel)
        view.actvPersonCity.setText(accountModel.location?.en?.placeName)
        view.actvPersonCity.setAdapter(placeAutocompleteAdapter)
        view.actvPersonCity.setOnItemClickListener({ _, _, i, _ ->
            placeAutocompleteAdapter.getItem(i) ?: return@setOnItemClickListener
            personSelectedPlaceId = placeAutocompleteAdapter.getItem(i)?.placeId
            personSelectedPlaceName = "${placeAutocompleteAdapter.getItem(i)?.primaryText} ${placeAutocompleteAdapter.getItem(i)?.secondaryText}"
            view.actvPersonCity.setText(personSelectedPlaceName ?: "")
        })
        view.containerSelectOccupation.setAbilities(accountModel.abilities)

        view.tilDateOfBirthday.hint = fromDictionary(R.string.reg_person_info_birthday)
        view.tilPhoneNumber.hint = fromDictionary(R.string.reg_info_phone_number)
        view.tvInfoGender.text = fromDictionary(R.string.reg_person_info_gender)
        view.rInfoBtnMale.text = fromDictionary(R.string.reg_person_info_male_gender)
        view.rInfoBtnFemale.text = fromDictionary(R.string.reg_person_info_female_gender)
        view.tilYourEmail.hint = fromDictionary(R.string.reg_info_email)
    }

    private fun getDateByTimeMillis(createdAt: Long?): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = createdAt ?: return ""
        return "${DateFormatSymbols().months[cal.get(Calendar.MONTH)]} ${cal.get(Calendar.DAY_OF_MONTH)}, ${cal.get(Calendar.YEAR)}"
    }

    companion object {
        private const val REQUEST_CODE_CROP = 101
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


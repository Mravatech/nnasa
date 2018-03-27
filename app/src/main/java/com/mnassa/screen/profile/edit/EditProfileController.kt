package com.mnassa.screen.profile.edit

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.View
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.activity.CropActivity
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.dialog.DialogHelper
import com.mnassa.domain.model.ProfileAccountModel
import com.mnassa.extensions.avatarSquare
import com.mnassa.google.PlayServiceHelper
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.profile.model.ProfileModel
import com.mnassa.screen.registration.PlaceAutocompleteAdapter
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.chip_layout.view.*
import kotlinx.android.synthetic.main.controller_edit_profile.view.*
import kotlinx.android.synthetic.main.header_main.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import timber.log.Timber
import java.text.DateFormatSymbols
import java.util.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/26/2018
 */

class EditProfileController(data: Bundle) : MnassaControllerImpl<EditProfileViewModel>(data) {

    override val layoutId = R.layout.controller_edit_profile
    override val viewModel: EditProfileViewModel by instance()
    private val accountModel: ProfileAccountModel by lazy { args.getSerializable(EXTRA_PROFILE) as ProfileAccountModel }
    private val playServiceHelper: PlayServiceHelper by instance()
    private val dialog: DialogHelper by instance()
    private var personSelectedPlaceName: String? = null
    private var personSelectedPlaceId: String? = null
    private var timeMillis: Long? = null

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        playServiceHelper.googleApiClient.connect()
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
        view.etEditProfileBirthday.isLongClickable = false
        view.etEditProfileBirthday.isFocusableInTouchMode = false
        timeMillis = accountModel.createdAt
        view.etEditProfileBirthday.setOnClickListener {
            dialog.calendarDialog(view.context, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                view.etEditProfileBirthday.setText("${DateFormatSymbols().months[month]} $dayOfMonth, $year")
                val cal = Calendar.getInstance()
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                cal.set(Calendar.MONTH, month)
                cal.set(Calendar.YEAR, year)
                timeMillis = cal.timeInMillis
            })
        }
        launchCoroutineUI {
            viewModel.imageUploadedChannel.consumeEach {
                view.ivEditProfileUserAvatar.avatarSquare(it)
            }
        }
        launchCoroutineUI {
            viewModel.getTagsByIds(accountModel.offers, true)
            viewModel.getTagsByIds(accountModel.interests, true)
            viewModel.tagChannel.consumeEach {
                when (it) {
                    is EditProfileViewModel.TagCommand.TagInterests -> view.clInterests.setTags(it.interests)
                    is EditProfileViewModel.TagCommand.TagOffers -> view.clOffers.setTags(it.offers)
                }
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

    private fun setupViews(view: View) {
        view.toolbarEditProfile.title = fromDictionary(R.string.edit_profile_title)
        view.toolbarEditProfile.onMoreClickListener = { close() }
        view.toolbarEditProfile.backButtonEnabled = true
        view.toolbarEditProfile.ivToolbarMore.setImageResource(R.drawable.ic_check)
        view.toolbarEditProfile.ivToolbarMore.setColorFilter(ContextCompat.getColor(view.context, R.color.turquoiseBlue), android.graphics.PorterDuff.Mode.SRC_IN)
        view.toolbarEditProfile.ivToolbarMore.visibility = View.VISIBLE
        view.tvEditProfileMoreInfo.text = fromDictionary(R.string.edit_profile_main_info)
        view.clOffers.tvChipHeader.text = fromDictionary(R.string.reg_account_can_help_with)
        view.clOffers.etChipInput.hint = fromDictionary(R.string.reg_person_type_here)
        view.clOffers.chipSearch = viewModel
        view.clOffers.setTags(emptyList())
        view.clInterests.tvChipHeader.text = fromDictionary(R.string.reg_account_interested_in)
        view.clInterests.etChipInput.hint = fromDictionary(R.string.reg_person_type_here)
        view.clInterests.chipSearch = viewModel
        view.clInterests.setTags(emptyList())
        view.etProfileFirstName.setText(accountModel.personalInfo?.firstName)
        view.etProfileLastName.setText(accountModel.personalInfo?.lastName)
        view.etProfileUserName.setText(accountModel.userName)
        view.tilProfileFirstName.hint = fromDictionary(R.string.reg_personal_first_name)
        view.tilProfileLastName.hint = fromDictionary(R.string.reg_personal_last_name)
        view.tilProfileUserName.hint = fromDictionary(R.string.reg_personal_user_name)
        view.ivEditProfileUserAvatar.avatarSquare(accountModel.avatar)
        view.tilEditProfileBirthday.hint = fromDictionary(R.string.reg_person_info_birthday)
        view.etEditProfileBirthday.setText(getDateByTimeMillis(accountModel.createdAt))
        view.tilProfileCity.hint = fromDictionary(R.string.edit_profile_city)
        view.tilEditProfilePhoneNumber.hint = fromDictionary(R.string.reg_person_info_phone)
        view.etEditProfilePhoneNumber.setText(accountModel.contactPhone)
        view.etEditProfilePhoneNumber.setHideMode(accountModel.showContactEmail ?: false)
        view.tilEditProfileEmail.hint = fromDictionary(R.string.reg_person_info_email)
        view.etEditProfileEmail.setText(accountModel.contactEmail)
        view.etEditProfileEmail.setHideMode(accountModel.showContactPhone ?: false)
        view.tvEditProfileGender.hint = fromDictionary(R.string.reg_person_info_gender)
        view.tvProfilePersonalInfo.text = fromDictionary(R.string.reg_person_info_title)
        view.rInfoBtnFemale.text = fromDictionary(R.string.reg_person_info_female_gender)
        view.rInfoBtnMale.text = fromDictionary(R.string.reg_person_info_male_gender)
        view.rInfoBtnFemale.isChecked = accountModel.gender.name.toLowerCase() == view.rInfoBtnMale.text.toString().toLowerCase()
        view.rInfoBtnMale.isChecked = accountModel.gender.name.toLowerCase() == view.rInfoBtnMale.text.toString().toLowerCase()
        val placeAutocompleteAdapter = PlaceAutocompleteAdapter(view.context, viewModel)
        view.actvProfileCity.setText(accountModel.location?.en?.placeName)
        view.actvProfileCity.setAdapter(placeAutocompleteAdapter)
        view.actvProfileCity.setOnItemClickListener({ _, _, i, _ ->
            placeAutocompleteAdapter.getItem(i) ?: return@setOnItemClickListener
            personSelectedPlaceId = placeAutocompleteAdapter.getItem(i)?.placeId
            personSelectedPlaceName = "${placeAutocompleteAdapter.getItem(i)?.primaryText} ${placeAutocompleteAdapter.getItem(i)?.secondaryText}"
            view.actvProfileCity.setText(personSelectedPlaceName ?: "")
        })
        view.containerProfileSelectOccupation.setAbilities(accountModel.abilities)
    }

    private fun getDateByTimeMillis(createdAt: Long?): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = createdAt ?: return ""
        return "${DateFormatSymbols().months[cal.get(Calendar.MONTH)]} ${cal.get(Calendar.DAY_OF_MONTH)}, ${cal.get(Calendar.YEAR)}"
    }

    companion object {
        private const val REQUEST_CODE_CROP = 101
        private const val EXTRA_PROFILE = "EXTRA_PROFILE"

        fun newInstance(profileModel: ProfileModel): EditProfileController {
            val params = Bundle()
            params.putSerializable(EditProfileController.EXTRA_PROFILE, profileModel.profile)
            return EditProfileController(params)
        }
    }

}


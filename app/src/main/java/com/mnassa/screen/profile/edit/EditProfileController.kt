package com.mnassa.screen.profile.edit

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.View
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.activity.CropActivity
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.ProfileAccountModel
import com.mnassa.extensions.avatarSquare
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.profile.model.ProfileModel
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.chip_layout.view.*
import kotlinx.android.synthetic.main.controller_edit_profile.view.*
import kotlinx.android.synthetic.main.header_main.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import timber.log.Timber

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/26/2018
 */

class EditProfileController(data: Bundle) : MnassaControllerImpl<EditProfileViewModel>(data) {

    override val layoutId = R.layout.controller_edit_profile
    override val viewModel: EditProfileViewModel by instance()
    private val accountModel: ProfileAccountModel by lazy { args.getSerializable(EXTRA_PROFILE) as ProfileAccountModel }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
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
        view.etEditProfileBirthday.setText(accountModel.createdAtDate)
        view.tilProfileCity.hint = fromDictionary(R.string.edit_profile_city)
        view.actvProfileCity.setText(accountModel.location?.en?.city)
        view.tilEditProfilePhoneNumber.hint = fromDictionary(R.string.reg_person_info_phone)
        view.etEditProfilePhoneNumber.setText(accountModel.contactPhone)
        view.tilEditProfileEmail.hint = fromDictionary(R.string.reg_person_info_email)
        view.etEditProfileEmail.setText(accountModel.contactEmail)
        view.tvEditProfileGender.hint = fromDictionary(R.string.reg_person_info_gender)
        view.tvProfilePersonalInfo.text = fromDictionary(R.string.reg_person_info_title)
        view.rInfoBtnFemale.text = fromDictionary(R.string.reg_person_info_female_gender)
        view.rInfoBtnMale.text = fromDictionary(R.string.reg_person_info_male_gender)
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


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
import com.mnassa.domain.model.TagModel
import com.mnassa.domain.model.impl.TagModelImpl
import com.mnassa.extensions.avatarSquare
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.chip_layout.view.*
import kotlinx.android.synthetic.main.controller_edit_profile.view.*
import kotlinx.android.synthetic.main.controller_profile.view.*
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
        view.toolbarEditProfile.title = fromDictionary(R.string.edit_profile_title)
        view.toolbarEditProfile.onMoreClickListener = { close() }
        view.toolbarEditProfile.backButtonEnabled = true
        view.toolbarEditProfile.ivToolbarMore.setImageResource(R.drawable.ic_check)
        view.toolbarEditProfile.ivToolbarMore.setColorFilter(ContextCompat.getColor(view.context, R.color.turquoiseBlue), android.graphics.PorterDuff.Mode.SRC_IN)
        view.toolbarEditProfile.ivToolbarMore.visibility = View.VISIBLE
        view.tvEditProfileMoreInfo.text = fromDictionary(R.string.edit_profile_main_info)
        view.clOffers.tvChipHeader.text = fromDictionary(R.string.reg_account_can_help_with)
        view.clOffers.etChipInput.setText(fromDictionary(R.string.reg_person_type_here))
        view.clInterests.tvChipHeader.text = fromDictionary(R.string.reg_account_interested_in)
        view.clInterests.etChipInput.setText(fromDictionary(R.string.reg_person_type_here))
        val tagsOffers = mutableListOf<TagModel>()
        for (tag in accountModel.offers!!) {
            tagsOffers.add(TagModelImpl(null, tag, null))
        }
        view.clOffers.setTags(tagsOffers)

        val tagsInterests = mutableListOf<TagModel>()
        for (tag in accountModel.interests!!) {
            tagsInterests.add(TagModelImpl(null, tag, null))
        }
        view.clOffers.setTags(tagsOffers)
        view.clInterests.setTags(tagsInterests)
        view.clOffers.chipSearch = viewModel
        view.clInterests.chipSearch = viewModel
        view.etProfileFirstName.setText(accountModel.personalInfo?.firstName)
        view.etProfileLastName.setText( accountModel.personalInfo?.lastName)
        view.etProfileUserName.setText(accountModel.userName)
        view.tilProfileFirstName.hint = fromDictionary(R.string.reg_personal_first_name)
        view.tilProfileLastName.hint = fromDictionary(R.string.reg_personal_last_name)
        view.tilProfileUserName.hint = fromDictionary(R.string.reg_personal_user_name)
        launchCoroutineUI {
            viewModel.imageUploadedChannel.consumeEach {
                view.ivCropImage.avatarSquare(it)
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_CROP = 101
        private const val EXTRA_PROFILE = "EXTRA_PROFILE"

        fun newInstance(profileAccountModel: ProfileAccountModel): EditProfileController {
            val params = Bundle()
            params.putSerializable(EditProfileController.EXTRA_PROFILE, profileAccountModel)
            return EditProfileController(params)
        }
    }

}


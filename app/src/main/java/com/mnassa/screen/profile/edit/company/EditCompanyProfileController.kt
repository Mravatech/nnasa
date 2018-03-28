package com.mnassa.screen.profile.edit.company

import android.Manifest
import android.os.Bundle
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
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.chip_layout.view.*
import kotlinx.android.synthetic.main.controller_organization_info.view.*
import kotlinx.android.synthetic.main.sub_company_info.view.*
import kotlinx.android.synthetic.main.sub_profile_avatar.view.*
import kotlinx.android.synthetic.main.sub_reg_company.view.*
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/28/2018
 */

class EditCompanyProfileController(data: Bundle) : MnassaControllerImpl<EditCompanyProfileViewModel>(data) {
    override val layoutId = R.layout.controller_edit_company_profile
    override val viewModel: EditCompanyProfileViewModel by instance()
    private val accountModel: ProfileAccountModel by lazy { args.getParcelable(EXTRA_PROFILE) as ProfileAccountModel }
    private val playServiceHelper: PlayServiceHelper by instance()
    private val dialog: DialogHelper by instance()
    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        setupView(view)
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
        launchCoroutineUI {
            viewModel.imageUploadedChannel.consumeEach {
                view.ivUserAvatar.avatarSquare(it)
            }
        }
    }

    private fun setupView(view: View) {
        view.ivUserAvatar.avatarSquare(accountModel.avatar)
        view.tilCompanyName.hint = fromDictionary(R.string.reg_account_company_name)
        view.tilCompanyUserName.hint = fromDictionary(R.string.reg_personal_user_name)
        view.tilCompanyCity.hint = fromDictionary(R.string.reg_personal_city)
        view.chipCompanyOffers.etChipInput.hint = fromDictionary(R.string.reg_person_type_here)
        view.chipCompanyOffers.tvChipHeader.text = fromDictionary(R.string.reg_account_can_help_with)
        view.chipCompanyInterests.etChipInput.hint = fromDictionary(R.string.reg_person_type_here)
        view.chipCompanyInterests.tvChipHeader.text = fromDictionary(R.string.reg_account_interested_in)
        view.chipCompanyOffers.chipSearch = viewModel
        view.chipCompanyInterests.chipSearch = viewModel
        view.tvHeader.text = fromDictionary(R.string.reg_company_title)
        view.btnHeaderNext.text = fromDictionary(R.string.reg_info_next)
        view.tilWebSite.hint = fromDictionary(R.string.reg_company_website)
        view.tilCompanyEmail.hint = fromDictionary(R.string.reg_info_email)
        view.tilFoundation.hint = fromDictionary(R.string.reg_company_founded)
        view.tvSkipThisStep.text = fromDictionary(R.string.reg_info_skip)
    }

    companion object {
        private const val REQUEST_CODE_CROP = 101
        private const val EXTRA_PROFILE = "EXTRA_PROFILE"

        fun newInstance(profileModel: ProfileModel): EditCompanyProfileController {
            val params = Bundle()
            params.putParcelable(EXTRA_PROFILE, profileModel.profile)
            return EditCompanyProfileController(params)
        }
    }

}
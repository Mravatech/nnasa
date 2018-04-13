package com.mnassa.screen.accountinfo.personal

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.github.salomonbrys.kodein.instance
import com.google.firebase.storage.StorageReference
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.activity.CropActivity
import com.mnassa.helper.DialogHelper
import com.mnassa.helper.GlideApp
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.buildnetwork.BuildNetworkController
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_personal_info.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import timber.log.Timber

/**
 * Created by Peter on 2/27/2018.
 */
class PersonalInfoController(data: Bundle) : MnassaControllerImpl<PersonalInfoViewModel>(data) {
    override val layoutId: Int = R.layout.controller_personal_info
    override val viewModel: PersonalInfoViewModel by instance()

    private val accountModel: ShortAccountModel by lazy { args.getSerializable(EXTRA_ACCOUNT) as ShortAccountModel }
    private val dialog: DialogHelper by instance()

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        view.tilDateOfBirthday.hint = fromDictionary(R.string.reg_person_info_birthday)
        view.tilPhoneNumber.hint = fromDictionary(R.string.reg_person_info_phone)
        view.tvInfoGender.text = fromDictionary(R.string.reg_person_info_gender)
        view.rInfoBtnMale.text = fromDictionary(R.string.reg_person_info_male_gender)
        view.rInfoBtnFemale.text = fromDictionary(R.string.reg_person_info_female_gender)
        view.tilYourEmail.hint = fromDictionary(R.string.reg_person_info_email)
        view.fabInfoAddPhoto.setOnClickListener {
            dialog.showSelectImageSourceDialog(it.context) { imageSource ->
                activity?.let {
                    val intent = CropActivity.start(imageSource, it)
                    startActivityForResult(intent, REQUEST_CODE_CROP)
                }
            }
        }
        view.btnNext.setOnClickListener {
            viewModel.processAccount(accountModel)
        }
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
        launchCoroutineUI {
            viewModel.imageUploadedChannel.consumeEach {
                setImage(view.ivUserAvatar, it)
            }
        }
        launchCoroutineUI {
            viewModel.openScreenChannel.consumeEach {
                val controller = when (it) {
                    is PersonalInfoViewModel.OpenScreenCommand.InviteScreen -> {
                        BuildNetworkController.newInstance()
                    }
                }
                open(controller)
            }
        }
    }

    private fun setImage(imageView: ImageView, result: StorageReference?) {
        GlideApp.with(imageView).load(result).into(imageView)
    }

    companion object {
        private const val REQUEST_CODE_CROP = 101
        private const val EXTRA_ACCOUNT = "EXTRA_ACCOUNT"

        fun newInstance(ac: ShortAccountModel): PersonalInfoController {
            val params = Bundle()
            params.putSerializable(EXTRA_ACCOUNT, ac)
            return PersonalInfoController(params)
        }
    }
}
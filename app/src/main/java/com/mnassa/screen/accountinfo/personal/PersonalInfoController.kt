package com.mnassa.screen.accountinfo.personal

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.support.annotation.IntRange
import android.view.View
import com.bluelinelabs.conductor.RouterTransaction
import com.github.salomonbrys.kodein.instance
import com.google.firebase.storage.StorageReference
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.other.*
import com.mnassa.screen.accountinfo.organization.OrganizationInfoController
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.main.MainController
import com.mnassa.screen.registration.RegistrationViewModel
import kotlinx.android.synthetic.main.controller_personal_info.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import timber.log.Timber

/**
 * Created by Peter on 2/27/2018.
 */
class PersonalInfoController(data: Bundle) : MnassaControllerImpl<PersonalInfoViewModel>(data), TakingPhotoListener {
    override val layoutId: Int = R.layout.controller_personal_info
    override val viewModel: PersonalInfoViewModel by instance()

    private val accountModel: ShortAccountModel by lazy { args.getSerializable(EXTRA_ACCOUNT) as ShortAccountModel }

    override fun onViewCreated(view: View) {

        with(view) {
            tilDateOfBirthday.hint = fromDictionary(R.string.reg_person_info_birthday)
            tilPhoneNumber.hint = fromDictionary(R.string.reg_person_info_phone)
            tvInfoGender.text = fromDictionary(R.string.reg_person_info_gender)
            rInfoBtnMale.text = fromDictionary(R.string.reg_person_info_male_gender)
            rInfoBtnFemale.text = fromDictionary(R.string.reg_person_info_female_gender)
            tilYourEmail.hint = fromDictionary(R.string.reg_person_info_email)
//
            fabInfoAddPhoto.setOnClickListener {
                showPhotoDialog(activity!!, this@PersonalInfoController)
            }

            btnNext.setOnClickListener {
                viewModel.processAccount(accountModel)
            }
        }

        onActivityResult.subscribe {
            if (it.resultCode == Activity.RESULT_OK && it.requestCode == REQUEST_CODE_CROP) {
                val uri: Uri? = it.data?.getParcelableExtra(CropActivity.URI_PHOTO_RESULT)
                uri?.let {
                    viewModel.sendPhotoToStorage(it)
                }
            } else if (it.resultCode == CropActivity.GET_PHOTO_ERROR) {
                Timber.i("CropActivity.GET_PHOTO_ERROR")
            }
        }

        launchCoroutineUI {
            viewModel.imageUploadedChannel.consumeEach {
                setImage(it)
            }
        }

        launchCoroutineUI {
            viewModel.openScreenChannel.consumeEach {
                val controller = when (it) {
                    is PersonalInfoViewModel.OpenScreenCommand.MainScreen -> {
                        MainController.newInstance()
                    }
                }
                router.popToRoot()
                router.replaceTopController(RouterTransaction.with(controller))
            }
        }
    }

    override fun startCropActivity(@IntRange(from = 1, to = 2) flag: Int) {
        activity?.let {
            val intent = CropActivity.start(flag, it)
            startActivityForResult(intent, REQUEST_CODE_CROP)
        }
    }

    private fun setImage(result: StorageReference?) {
        view?.ivUserAvatar?.let {
            GlideApp.with(it).load(result).into(it)
        }
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
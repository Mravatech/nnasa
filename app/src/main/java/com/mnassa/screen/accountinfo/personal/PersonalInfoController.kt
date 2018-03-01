package com.mnassa.screen.accountinfo.personal

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.support.annotation.IntRange
import android.view.View
import com.bumptech.glide.Glide
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.other.CropActivity
import com.mnassa.other.fromDictionary
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.registration.FirstRegistrationStepData
import kotlinx.android.synthetic.main.controller_personal_info.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import timber.log.Timber

/**
 * Created by Peter on 2/27/2018.
 */
class PersonalInfoController(data: Bundle) : MnassaControllerImpl<PersonalInfoViewModel>(data) {
    override val layoutId: Int = R.layout.controller_personal_info
    override val viewModel: PersonalInfoViewModel by instance()

    override fun onViewCreated(view: View) {

        with(view) {
            tilDateOfBirth.hint = fromDictionary(R.string.reg_person_info_birthday)
            tilPhoneNumber.hint = fromDictionary(R.string.reg_person_info_phone)
            tilAt.hint = fromDictionary(R.string.reg_person_info_at)
            tvPersonalHeader.text = fromDictionary(R.string.reg_person_info_title)
            fabAddPhotoCamera.setOnClickListener {
                startCropActivity(CropActivity.REQUEST_CODE_CAMERA)
            }

            fabAddPhotoGallery.setOnClickListener {
                startCropActivity(CropActivity.REQUEST_CODE_GALLERY)
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

        viewModel.getPhotoFromStorage()
    }

    private fun startCropActivity(@IntRange(from = 1, to = 2) flag: Int) {
        activity?.let {
            val intent = CropActivity.start(flag, it)
            startActivityForResult(intent, REQUEST_CODE_CROP)
        }
    }

    private fun setImage(result: String?) {
        result?.let {
            Glide.with(view?.ivUserPhoto).load(it).into(view?.ivUserPhoto)
        }
    }

    companion object {
        private const val REQUEST_CODE_CROP = 101

        private const val EXTRA_FIRST_STEP_DATA = "EXTRA_FIRST_STEP_DATA"

        fun newInstance(): PersonalInfoController {
            val params = Bundle()
            return PersonalInfoController(params)
        }
    }
}
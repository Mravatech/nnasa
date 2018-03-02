package com.mnassa.screen.profile

import android.app.Activity
import android.view.View
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.screen.base.MnassaControllerImpl
import kotlinx.android.synthetic.main.controller_crop.view.*
import android.net.Uri
import android.support.annotation.IntRange
import com.google.firebase.storage.StorageReference
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.other.CropActivity
import com.mnassa.other.GlideApp
import com.mnassa.other.TakingPhotoListener
import com.mnassa.other.showPhotoDialog
import kotlinx.coroutines.experimental.channels.consumeEach
import timber.log.Timber

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 2/26/2018
 */

class ProfileController : MnassaControllerImpl<ProfileViewModel>(), TakingPhotoListener {

    override val layoutId: Int = R.layout.controller_crop
    override val viewModel: ProfileViewModel by instance()

    override fun onViewCreated(view: View) {

        with(view) {

            //set text from resources
            btnTakePhotoFromCamera.text = "Take a photo"
            btnTakePhotoFromCamera.setOnClickListener {
                //                startCropActivity(CropActivity.REQUEST_CODE_CAMERA)
                showPhotoDialog(activity!!, this@ProfileController)
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

    override fun startCropActivity(@IntRange(from = 1, to = 2) flag: Int) {
        activity?.let {
            val intent = CropActivity.start(flag, it)
            startActivityForResult(intent, REQUEST_CODE_CROP)
        }
    }

    private fun setImage(result: StorageReference?) {
        view?.ivCropImage?.let {
            //todo
            GlideApp.with(it)
                    .load(result)
                    .into(it)
        }
    }

    companion object {
        private const val REQUEST_CODE_CROP = 101

        fun newInstance() = ProfileController()
    }
}
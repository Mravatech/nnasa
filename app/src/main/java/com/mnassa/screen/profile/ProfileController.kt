package com.mnassa.screen.profile

import android.app.Activity
import android.view.View
import com.bumptech.glide.Glide
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.screen.base.MnassaControllerImpl
import kotlinx.android.synthetic.main.controller_crop.view.*
import android.net.Uri
import android.support.annotation.IntRange
import com.mnassa.other.CropActivity
import timber.log.Timber
import java.io.File

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 2/26/2018
 */

class ProfileController : MnassaControllerImpl<ProfileViewModel>() {

    override val layoutId: Int = R.layout.controller_crop
    override val viewModel: ProfileViewModel by instance()

    private var cachedUri: Uri? = null
    private var mUri: Uri? = null
    override fun onViewCreated(view: View) {
        val filename = System.currentTimeMillis().toString() + ".jpg"
        cachedUri = Uri.fromFile(File(activity?.cacheDir.toString() + "/" + filename))
        with(view) {

            //set text from resources
            btnTakePhotoFromCamera.text = "Camera"
            btnTakePhotoFromCamera.setOnClickListener {
                startCropActivity(CropActivity.REQUEST_CODE_CAMERA)
            }

            btnTakePhotoFromGallery.text = "Gallery"
            btnTakePhotoFromGallery.setOnClickListener {
                startCropActivity(CropActivity.REQUEST_CODE_GALLERY)
            }
        }

        launchCoroutineUI {
            onActivityResult.subscribe {
                if (it.resultCode == Activity.RESULT_OK && it.requestCode == REQUEST_CODE_CROP) {
                    mUri = it.data?.getParcelableExtra(CropActivity.URI_PHOTO_RESULT) ?: mUri!!
                    setImage(mUri)
                } else if (it.resultCode == CropActivity.GET_PHOTO_ERROR) {
                    Timber.i("CropActivity.GET_PHOTO_ERROR")
                }
            }
        }
    }

    private fun startCropActivity(@IntRange(from = 1, to = 2) flag: Int) {
        val intent = CropActivity.start(flag, activity!!)
        startActivityForResult(intent, REQUEST_CODE_CROP)
    }

    private fun setImage(resultUri: Uri?) {
        resultUri?.let {
            Glide.with(activity).load(it).into(view?.ivCropImage)
        }
    }

    companion object {
        private const val REQUEST_CODE_CROP = 101

        fun newInstance() = ProfileController()
    }
}
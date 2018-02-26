package com.mnassa.screen.profile

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.provider.MediaStore
import android.view.View
import com.bumptech.glide.Glide
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.screen.base.MnassaControllerImpl
import kotlinx.android.synthetic.main.controller_crop.view.*
import android.net.Uri
import com.mnassa.other.CropActivity
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
                requestCamera()
            }

            btnTakePhotoFromGallery.text = "Gallery"
            btnTakePhotoFromGallery.setOnClickListener {
                pickFromGallery()
            }
            btnCrop.text = "Crop Photo"
            btnCrop.setOnClickListener {
                cropImage()
            }

        }

        launchCoroutineUI {
            onActivityResult.subscribe {

                if (it.resultCode == Activity.RESULT_OK && it.requestCode == REQUEST_CODE_CAMERA) {
                    mUri = if (it.data != null) it.data?.data else mUri
                    setImage(mUri)
                } else if (it.resultCode == Activity.RESULT_OK && it.requestCode == REQUEST_CODE_PICTURE) {
                    mUri = it.data?.data
                    setImage(mUri)
                } else if (it.resultCode == Activity.RESULT_OK && it.requestCode == CropActivity.CROP_PHOTO_CODE) {
                    mUri = it.data?.getParcelableExtra(CropActivity.URI_PHOTO_RESULT)
                            ?: mUri!!
                    setImage(mUri)
                } else if (it.resultCode == CropActivity.CROP_PHOTO_ERROR) {
                    //handle here
                }
            }
        }

    }

    private fun cropImage() {
        if (mUri != null) {
            startActivityForResult(CropActivity.start(mUri!!, activity!!), CropActivity.CROP_PHOTO_CODE)
        }
    }

    private fun requestCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        if (intent.resolveActivity(activity?.packageManager) != null) {
            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, System.currentTimeMillis())
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
            mUri = activity?.contentResolver?.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

            intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri)
            startActivityForResult(intent, REQUEST_CODE_CAMERA)
        } else {
            // handle
        }
    }

    private fun pickFromGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        // put to resources
        startActivityForResult(Intent.createChooser(intent, "Select photo"), REQUEST_CODE_PICTURE)
    }

    private fun setImage(resultUri: Uri?) {
        resultUri?.let {
            Glide.with(activity).load(it).into(view?.ivCropImage)
        }
    }

    companion object {
        private const val REQUEST_CODE_PICTURE = 1
        private const val REQUEST_CODE_CAMERA = 2

        fun newInstance() = ProfileController()
    }
}
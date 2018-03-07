package com.mnassa.screen.profile

import android.app.Activity
import android.view.View
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.screen.base.MnassaControllerImpl
import kotlinx.android.synthetic.main.controller_crop.view.*
import android.net.Uri
import android.support.annotation.IntRange
import android.widget.ImageView
import com.google.firebase.storage.StorageReference
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.activity.CropActivity
import com.mnassa.dialog.DialogHelper
import com.mnassa.module.GlideApp
import com.mnassa.dialog.PhotoListener
import kotlinx.coroutines.experimental.channels.consumeEach
import timber.log.Timber

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 2/26/2018
 */

class ProfileController : MnassaControllerImpl<ProfileViewModel>(), PhotoListener {

    override val layoutId: Int = R.layout.controller_crop
    override val viewModel: ProfileViewModel by instance()

    private val dialog: DialogHelper by instance()

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        with(view) {
            btnTakePhotoFromCamera.setOnClickListener {
                dialog.showPhotoDialog(activity!!, this@ProfileController)
            }
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
                setImage(view.ivCropImage, it)
            }
        }
    }

    override fun startCropActivity(@IntRange(from = 1, to = 2) flag: Int) {
        activity?.let {
            val intent = CropActivity.start(flag, it)
            startActivityForResult(intent, REQUEST_CODE_CROP)
        }
    }

    private fun setImage(imageView: ImageView, result: StorageReference?) {
        GlideApp.with(imageView).load(result).into(imageView)
    }

    companion object {
        private const val REQUEST_CODE_CROP = 101

        fun newInstance() = ProfileController()
    }
}
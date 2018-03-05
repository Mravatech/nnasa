package com.mnassa.dialog

import android.app.Dialog
import android.content.Context
import android.support.annotation.IntRange
import android.view.Window
import com.mnassa.R
import com.mnassa.activity.CropActivity
import kotlinx.android.synthetic.main.dialog_photo.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/5/2018
 */

fun showPhotoDialog(context: Context, listener: PhotoListener) {
    val dialog = Dialog(context, R.style.PhotoDialog)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setCancelable(false)
    dialog.setContentView(R.layout.dialog_photo)
    fun dialogButtonClicks(flag: Int) {
        listener.startCropActivity(flag)
        dialog.dismiss()
    }
    dialog.btnCamera.setOnClickListener {
        dialogButtonClicks(CropActivity.REQUEST_CODE_CAMERA)
    }
    dialog.btnGallery.setOnClickListener {
        dialogButtonClicks(CropActivity.REQUEST_CODE_GALLERY)
    }
    dialog.btnClose.setOnClickListener { dialog.dismiss() }
    dialog.show()
}


interface PhotoListener {
    fun startCropActivity(@IntRange(from = 1, to = 2) flag: Int)
}
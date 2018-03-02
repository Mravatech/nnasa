package com.mnassa.other

import android.app.Dialog
import android.content.Context
import android.view.View
import android.view.Window
import android.widget.Button
import com.mnassa.R

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/2/2018
 */

fun showPhotoDialog(context: Context, listener: TakingPhotoListener) {
    val dialog = Dialog(context, R.style.PhotoDialog)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setCancelable(false)
    dialog.setContentView(R.layout.dialog_photo)

    val cameraButton = dialog.findViewById<View>(R.id.btnCamera) as Button
    cameraButton.setOnClickListener {
        listener.startCropActivity(CropActivity.REQUEST_CODE_CAMERA)
        dialog.dismiss()
    }
    val galleryButton = dialog.findViewById<View>(R.id.btnGallery) as Button
    galleryButton.setOnClickListener {
        listener.startCropActivity(CropActivity.REQUEST_CODE_GALLERY)
        dialog.dismiss()
    }
    val closeButton = dialog.findViewById<View>(R.id.btnClose) as Button
    closeButton .setOnClickListener { dialog.dismiss() }
    dialog.show()
}

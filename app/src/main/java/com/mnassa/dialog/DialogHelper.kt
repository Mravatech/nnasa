package com.mnassa.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.support.annotation.IntRange
import android.view.LayoutInflater
import android.view.Window
import com.mnassa.R
import com.mnassa.activity.CropActivity
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.dialog_occupation.*
import kotlinx.android.synthetic.main.dialog_photo.*
import kotlinx.android.synthetic.main.dialog_welcome.view.*

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/5/2018
 */

class DialogHelper {

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

    fun showWelcomeDialog(context: Context, onOkClick: () -> Unit) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_welcome, null)
        dialogView.tvTitle.text = fromDictionary(R.string.welcome_dialog_title)
        dialogView.tvDescription.text = fromDictionary(R.string.welcome_dialog_description)

        AlertDialog.Builder(context)
                .setView(dialogView)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    onOkClick()
                }.setOnCancelListener {
                    onOkClick()
                }.show()
    }

    fun showChooseOccupationDialog(context: Context, onSelectClick: (position: Int) -> Unit) {
        val dialog = Dialog(context, R.style.OccupationDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_occupation)
        fun closeDialogAfterClick(position: Int) {
            onSelectClick(position)
//            dialog.dismiss() //todo remove if its will be unnecessary
        }
        dialog.tvOccupationHeader.text = fromDictionary(R.string.reg_dialog_header)
        dialog.rStudent.text = fromDictionary(R.string.reg_dialog_student)
        dialog.rHouseWife.text = fromDictionary(R.string.reg_dialog_housewife)
        dialog.rEmployee.text = fromDictionary(R.string.reg_dialog_employee)
        dialog.rBusinessOwner.text = fromDictionary(R.string.reg_dialog_business_owner)
        dialog.rOther.text = fromDictionary(R.string.reg_dialog_other)
        dialog.rStudent.setOnClickListener { closeDialogAfterClick(0) }
        dialog.rHouseWife.setOnClickListener { closeDialogAfterClick(1) }
        dialog.rEmployee.setOnClickListener { closeDialogAfterClick(2) }
        dialog.rBusinessOwner.setOnClickListener { closeDialogAfterClick(3) }
        dialog.rOther.setOnClickListener { closeDialogAfterClick(4) }
        dialog.show()
    }

}

interface PhotoListener {
    fun startCropActivity(@IntRange(from = 1, to = 2) flag: Int)
}
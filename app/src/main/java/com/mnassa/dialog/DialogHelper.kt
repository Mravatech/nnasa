package com.mnassa.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.support.annotation.IntRange
import android.support.v7.widget.AppCompatRadioButton
import android.view.LayoutInflater
import android.view.Window
import com.mnassa.R
import com.mnassa.activity.CropActivity
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.dialog_occupation.*
import kotlinx.android.synthetic.main.dialog_photo.*
import kotlinx.android.synthetic.main.dialog_welcome.view.*
import android.app.DatePickerDialog
import java.util.Calendar


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

    fun showChooseOccupationDialog(context: Context,
                                   occupations: List<String>,
                                   position: Int,
                                   onSelectClick: (position: Int) -> Unit) {
        val dialog = Dialog(context, R.style.OccupationDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_occupation)
        fun closeDialogAfterClick(position: Int) {
            onSelectClick(position)
            dialog.dismiss()
        }
        dialog.tvOccupationHeader.text = fromDictionary(R.string.reg_dialog_header)
        for ((index, value) in occupations.withIndex()) {
            val radioButton = dialog.rOccupationContainer.getChildAt(index) as AppCompatRadioButton
            radioButton.text = value
            radioButton.setOnClickListener { closeDialogAfterClick(index) }
            radioButton.isChecked = position == index
        }
        dialog.show()
    }

    fun calendarDialog(context: Context, listener: DatePickerDialog.OnDateSetListener) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(context, listener, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

}

interface PhotoListener {
    fun startCropActivity(@IntRange(from = 1, to = 2) flag: Int)
}
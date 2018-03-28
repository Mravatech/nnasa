package com.mnassa.dialog

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.support.v7.app.AlertDialog
import android.support.v7.widget.AppCompatRadioButton
import android.view.LayoutInflater
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import com.afollestad.materialdialogs.MaterialDialog
import com.mnassa.BuildConfig
import com.mnassa.R
import com.mnassa.activity.CropActivity
import com.mnassa.screen.progress.MnassaProgressDialog
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.dialog_company_status.*
import kotlinx.android.synthetic.main.dialog_occupation.*
import kotlinx.android.synthetic.main.dialog_welcome.view.*
import java.util.*


class DialogHelper {

    fun showSelectImageSourceDialog(context: Context, listener: (CropActivity.ImageSource) -> Unit) {
        MaterialDialog.Builder(context)
                .items(
                        fromDictionary(R.string.image_source_gallery),
                        fromDictionary(R.string.image_source_camera)
                )
                .itemsCallbackSingleChoice(-1, { dialog, itemView, which, text ->
                    listener(CropActivity.ImageSource.values()[which])
                    true
                })
                .cancelable(true)
                .show()
    }

    fun showWelcomeDialog(context: Context, onOkClick: () -> Unit) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_welcome, null)
        dialogView.tvTitle.text = fromDictionary(R.string.welcome_dialog_title)
        dialogView.tvDescription.text = fromDictionary(R.string.welcome_dialog_description)

        MaterialDialog.Builder(context)
                .customView(dialogView, false)
                .positiveText(android.R.string.ok)
                .onPositive { _, _ -> onOkClick() }
                .cancelListener { onOkClick() }
                .show()
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

    fun showChooseCompanyStatusDialog(context: Context,
                                   statuses: List<String>,
                                   position: Int,
                                   onSelectClick: (position: Int) -> Unit) {
        val dialog = Dialog(context, R.style.OccupationDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_company_status)
        fun closeDialogAfterClick(position: Int) {
            onSelectClick(position)
            dialog.dismiss()
        }
        dialog.tvCompanyStatusHeader.text = fromDictionary(R.string.reg_company_status_label)
        for ((index, value) in statuses.withIndex()) {
            val radioButton = dialog.rCompanyStatusContainer.getChildAt(index) as AppCompatRadioButton
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

    @SuppressLint("SetTextI18n")
    fun showLoginByEmailDialog(context: Context, listener: (email: String, password: String) -> Unit) {
        if (!BuildConfig.DEBUG) return
        //!!!DEBUG ONLY!!!
        val container = LinearLayout(context)
        container.orientation = LinearLayout.VERTICAL

        val email = EditText(context)
        email.hint = "Email"
        container.addView(email)

        val password = EditText(context)
        password.hint = "Password"
        container.addView(password)

        lateinit var dialog: AlertDialog

        var btnHardcodedEmailAndPassword = Button(context)
        btnHardcodedEmailAndPassword.text = "p3@nxt.ru"
        btnHardcodedEmailAndPassword.setOnClickListener {
            listener("p3@nxt.ru", "123123")
            dialog.dismiss()
        }
        container.addView(btnHardcodedEmailAndPassword)
        //
        btnHardcodedEmailAndPassword = Button(context)
        btnHardcodedEmailAndPassword.text = "chas@ukr.net"
        btnHardcodedEmailAndPassword.setOnClickListener {
            listener("chas@ukr.net", "123123")
            dialog.dismiss()
        }
        container.addView(btnHardcodedEmailAndPassword)
        //
        btnHardcodedEmailAndPassword = Button(context)
        btnHardcodedEmailAndPassword.text = "serg@u.net"
        btnHardcodedEmailAndPassword.setOnClickListener {
            listener("serg@u.net", "123123")
            dialog.dismiss()
        }
        container.addView(btnHardcodedEmailAndPassword)

        val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT)
        container.layoutParams = layoutParams

        dialog = AlertDialog.Builder(context)
                .setView(container)
                .setPositiveButton("Login", { _, _ ->
                    listener(email.text.toString(),
                            password.text.toString())
                })
                .show()
    }

    fun showProgressDialog(context: Context): Dialog {
        val dialog = MnassaProgressDialog(context, R.style.MnassaProgressTheme)
        dialog.setCancelable(false)
        dialog.show()
        return dialog
    }

}
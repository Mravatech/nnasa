package com.mnassa.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import com.afollestad.materialdialogs.MaterialDialog
import com.mnassa.BuildConfig
import com.mnassa.R
import com.mnassa.activity.CropActivity
import com.mnassa.screen.progress.MnassaProgressDialog
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.dialog_welcome.view.*


class DialogHelper {

    fun showSelectImageSourceDialog(context: Context, listener: (CropActivity.ImageSource) -> Unit) {
        MaterialDialog.Builder(context)
                .items(
                        fromDictionary(R.string.image_source_gallery),
                        fromDictionary(R.string.image_source_camera)
                )
                .itemsCallback{ dialog, itemView, which, text ->
                    listener(CropActivity.ImageSource.values()[which])
                    dialog.dismiss()
                }
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

    fun showConfirmPostRemovingDialog(context: Context, onOkClick: () -> Unit) {
        MaterialDialog.Builder(context)
                .title(fromDictionary(R.string.post_delete_dialog_title))
                .content(fromDictionary(R.string.post_delete_dialog_description))
                .positiveText(fromDictionary(R.string.post_delete_dialog_yes))
                .negativeText(fromDictionary(R.string.post_delete_dialog_no))
                .onPositive { _, _ -> onOkClick() }
                .show()
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
        //
        btnHardcodedEmailAndPassword = Button(context)
        btnHardcodedEmailAndPassword.text = "anton@u.net"
        btnHardcodedEmailAndPassword.setOnClickListener {
            listener("anton@u.net", "123123")
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
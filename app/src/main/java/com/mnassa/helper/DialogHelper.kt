package com.mnassa.helper

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
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
import com.mnassa.domain.model.TranslatedWordModel
import com.mnassa.screen.invite.InviteController.Companion.INVITE_WITH_SHARE
import com.mnassa.screen.invite.InviteController.Companion.INVITE_WITH_SMS
import com.mnassa.screen.invite.InviteController.Companion.INVITE_WITH_WHATS_APP
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.dialog_company_status.*
import kotlinx.android.synthetic.main.dialog_invite_with.*
import kotlinx.android.synthetic.main.dialog_occupation.*
import kotlinx.android.synthetic.main.dialog_welcome.view.*
import kotlinx.android.synthetic.main.dialog_yes_no.*
import java.util.*

class DialogHelper {

    fun showSelectImageSourceDialog(context: Context, listener: (CropActivity.ImageSource) -> Unit) {
        MaterialDialog.Builder(context)
                .items(
                        fromDictionary(R.string.image_source_gallery),
                        fromDictionary(R.string.image_source_camera)
                )
                .itemsCallback { dialog, _, which, _ ->
                    listener(CropActivity.ImageSource.values()[which])
                    dialog.dismiss()
                }
                .cancelable(true)
                .show()
    }

    fun showComplaintDialog(context: Context, reports: List<TranslatedWordModel>,  listener: (TranslatedWordModel) -> Unit)  {
        MaterialDialog.Builder(context)
                .items(                        reports                )
                .itemsCallback { dialog, _, which, _ ->
                    listener(reports[which])
                    dialog.dismiss()
                }
                .cancelable(true)
                .show()
    }

    fun showWelcomeDialog(context: Context, onOkClick: () -> Unit) {
        showSuccessDialog(
                context,
                fromDictionary(R.string.welcome_dialog_title),
                fromDictionary(R.string.welcome_dialog_description),
                onOkClick)
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


    fun showSuccessDialog(context: Context, title: CharSequence, description: CharSequence, onOkClick: () -> Unit = {}) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_welcome, null)
        dialogView.tvTitle.text = title
        dialogView.tvDescription.text = description

        val dialog = MaterialDialog.Builder(context)
                .customView(dialogView, false)
                .cancelListener { onOkClick() }
                .show()

        dialogView.btnOk.setOnClickListener { dialog.cancel() }
        dialogView.btnOk.setText(android.R.string.ok)
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

    fun connectionsDialog(context: Context,info: String, onOkClick: () -> Unit) {
        val dialog = Dialog(context, R.style.OccupationDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_yes_no)
        dialog.tvConnectionInfo.text = info
        dialog.tvYes.text = fromDictionary(R.string.user_profile_yes)
        dialog.tvNo.text = fromDictionary(R.string.user_profile_no)
        dialog.tvNo.setOnClickListener { dialog.dismiss() }
        dialog.tvYes.setOnClickListener {
            onOkClick()
            dialog.dismiss()
        }
        dialog.show()
    }

    fun chooseSendInviteWith(context: Context, name: String?, isWhatsAppInstalled: Boolean, onInviteWithClick: (inviteWith: Int) -> Unit) {
        val dialog = Dialog(context, R.style.DialogInvite)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_invite_with)
        dialog.tvShortMessageService.text = fromDictionary(R.string.invite_invite_send_with_sms)
        dialog.tvMore.text = fromDictionary(R.string.invite_invite_send_with_more)
        dialog.tvWhatsApp.text = fromDictionary(R.string.invite_invite_send_with_whats_app)
        dialog.tvInviteDialogSubTitle.text = fromDictionary(R.string.invite_invite_select_way_to_send)
        dialog.tvInviteDialogTitle.text = name?.let { fromDictionary(R.string.invite_invite_you_invite).format(it) }
                ?: run { fromDictionary(R.string.invite_invite_you_invite_unknown_name) }
        fun sendInvite(inviteWith: Int) {
            onInviteWithClick(inviteWith)
            dialog.dismiss()
        }
        if (isWhatsAppInstalled) {
            dialog.llWhatsApp.setOnClickListener { sendInvite(INVITE_WITH_WHATS_APP) }
        }
        dialog.llShortMessageService.setOnClickListener { sendInvite(INVITE_WITH_SMS) }
        dialog.llMore.setOnClickListener { sendInvite(INVITE_WITH_SHARE) }
        dialog.show()
    }

    @SuppressLint("SetTextI18n")
    fun showLoginByEmailDebugDialog(context: Context, listener: (email: String, password: String) -> Unit) {
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
        val dialog = object : ProgressDialog(context, R.style.MnassaProgressTheme) {
            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                setContentView(R.layout.dialog_progress)
            }
        }
        dialog.setCancelable(false)
        dialog.show()
        return dialog
    }
}
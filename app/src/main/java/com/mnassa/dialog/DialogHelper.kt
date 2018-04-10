package com.mnassa.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import com.afollestad.materialdialogs.MaterialDialog
import com.mnassa.BuildConfig
import com.mnassa.R
import com.mnassa.activity.CropActivity
import com.mnassa.screen.invite.InviteController.Companion.INVITE_WITH_SHARE
import com.mnassa.screen.invite.InviteController.Companion.INVITE_WITH_SMS
import com.mnassa.screen.invite.InviteController.Companion.INVITE_WITH_WHATS_APP
import com.mnassa.screen.progress.MnassaProgressDialog
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.dialog_delete_chat_message.*
import kotlinx.android.synthetic.main.dialog_invite_with.*
import kotlinx.android.synthetic.main.dialog_welcome.view.*


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

    fun showDeleteMessageDialog(
            context: Context,
            isMyMessageClicked: Boolean,
            onDeleteForMeClick: () -> Unit,
            onDeleteForBothClick: () -> Unit,
            onCopyClick: () -> Unit,
            onReplyClick: () -> Unit) {
        val dialog = Dialog(context, R.style.DialogInvite)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_delete_chat_message)
        if (isMyMessageClicked) {
            dialog.tvDeleteForBoth.text = fromDictionary(R.string.chats_delete_for_all)
            dialog.tvDeleteForBoth.setOnClickListener {
                onDeleteForBothClick()
                dialog.dismiss()
            }
            dialog.tvDeleteForBoth.visibility = View.VISIBLE
        } else {
            dialog.tvReply.text = fromDictionary(R.string.chats_reply)
            dialog.tvReply.setOnClickListener {
                onReplyClick()
                dialog.dismiss()
            }
            dialog.tvReply.visibility = View.VISIBLE
        }
        dialog.tvDeleteForMe.text = fromDictionary(R.string.chats_delete_for_me)
        dialog.tvCopyChatMessage.text = fromDictionary(R.string.chats_copy)
        dialog.tvCancel.text = fromDictionary(R.string.chats_cancel)
        dialog.tvDeleteForMe.setOnClickListener {
            onDeleteForMeClick()
            dialog.dismiss()
        }
        dialog.tvCopyChatMessage.setOnClickListener {
            onCopyClick()
            dialog.dismiss()
        }
        dialog.tvCancel.setOnClickListener { dialog.dismiss() }
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
        val dialog = MnassaProgressDialog(context, R.style.MnassaProgressTheme)
        dialog.setCancelable(false)
        dialog.show()
        return dialog
    }

}
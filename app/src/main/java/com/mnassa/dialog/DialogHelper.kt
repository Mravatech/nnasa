package com.mnassa.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.support.annotation.IntRange
import android.view.LayoutInflater
import android.view.Window
import com.mnassa.R
import com.mnassa.activity.CropActivity
import com.mnassa.screen.hail.InviteToMnassaController.Companion.INVITE_WITH_SHARE
import com.mnassa.screen.hail.InviteToMnassaController.Companion.INVITE_WITH_SMS
import com.mnassa.screen.hail.InviteToMnassaController.Companion.INVITE_WITH_WHATS_APP
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.dialog_coutry.*
import kotlinx.android.synthetic.main.dialog_invite_with.*
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

    fun chooseCountryInvite(context: Context, onCountryClick: (countryCode: String) -> Unit) {
        val dialog = Dialog(context, R.style.DialogInvite)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_coutry)
        dialog.tvSaudiArabiaCode.text = fromDictionary(R.string.invite_invite_country_sa_code)
        dialog.tvSaudiArabiaCountry.text = fromDictionary(R.string.invite_invite_country_sa_country)
        dialog.tvUkraineCode.text = fromDictionary(R.string.invite_invite_country_ua_code)
        dialog.tvUkraineCountry.text = fromDictionary(R.string.invite_invite_country_ua_country)
        dialog.tvUnitedStatesCode.text = fromDictionary(R.string.invite_invite_country_us_code)
        dialog.tvUnitedStatesCountry.text = fromDictionary(R.string.invite_invite_country_us_country)
        dialog.tvCanadaCode.text = fromDictionary(R.string.invite_invite_country_ca_code)
        dialog.tvCanadaCountry.text = fromDictionary(R.string.invite_invite_country_ca_country)
        fun setCode(code: String) {
            onCountryClick(code)
            dialog.dismiss()
        }
        dialog.llSaudiArabia.setOnClickListener { setCode(fromDictionary(R.string.invite_invite_country_sa_code)) }
        dialog.llUkraine.setOnClickListener { setCode(fromDictionary(R.string.invite_invite_country_ua_code)) }
        dialog.llUnitedStates.setOnClickListener { setCode(fromDictionary(R.string.invite_invite_country_us_code)) }
        dialog.llCanada.setOnClickListener { setCode(fromDictionary(R.string.invite_invite_country_ca_code)) }
        dialog.show()
    }

    fun chooseSendInviteWith(context: Context, name: String?, isWhatsAppInstalled: Boolean, onInviteWithClick: (inviteWith: Int) -> Unit) {
        val dialog = Dialog(context, R.style.DialogInvite)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_invite_with)
        dialog.tvSMS.text = fromDictionary(R.string.invite_invite_send_with_sms)
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
        dialog.llSMS.setOnClickListener { sendInvite(INVITE_WITH_SMS) }
        dialog.llMore.setOnClickListener { sendInvite(INVITE_WITH_SHARE) }
        dialog.show()
    }

}

interface PhotoListener {
    fun startCropActivity(@IntRange(from = 1, to = 2) flag: Int)
}
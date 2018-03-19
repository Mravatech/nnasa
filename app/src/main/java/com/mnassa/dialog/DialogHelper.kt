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
import kotlinx.android.synthetic.main.dialog_coutry.*
import kotlinx.android.synthetic.main.dialog_coutry.view.*
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
        val dialog = Dialog(context, R.style.DialogCountry)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_coutry)
        dialog.llSaudiArabia.tvSaudiArabiaCode.text = fromDictionary(R.string.invite_invite_country_sa_code)
        dialog.llSaudiArabia.tvSaudiArabiaCountry.text = fromDictionary(R.string.invite_invite_country_sa_country)
        dialog.llUkraine.tvUkraineCode.text = fromDictionary(R.string.invite_invite_country_ua_code)
        dialog.llUkraine.tvUkraineCountry.text = fromDictionary(R.string.invite_invite_country_ua_country)
        dialog.llUnitedStates.tvUnitedStatesCode.text = fromDictionary(R.string.invite_invite_country_us_code)
        dialog.llUnitedStates.tvUnitedStatesCountry.text = fromDictionary(R.string.invite_invite_country_us_country)
        dialog.llCanada.tvCanadaCode.text = fromDictionary(R.string.invite_invite_country_ca_code)
        dialog.llCanada.tvCanadaCountry.text = fromDictionary(R.string.invite_invite_country_ca_country)
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

}

interface PhotoListener {
    fun startCropActivity(@IntRange(from = 1, to = 2) flag: Int)
}
package com.mnassa.helper

import android.content.Intent
import android.net.Uri
import java.net.URLEncoder

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/21/2018
 */
class IntentHelper {

    fun getSMSIntent(text: String, number: String): Intent {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:$number"))
        intent.putExtra(SMS_BODY, text)
        return intent
    }

    fun getShareIntent(text: String): Intent {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.putExtra(Intent.EXTRA_TEXT, text)
        intent.type = "text/plain"
        return intent
    }

    fun getWhatsAppIntent(text: String, number: String): Intent {
        val intent = Intent(Intent.ACTION_VIEW)
        val url = "${WHATS_APP_START_URI}$number${WHATS_APP_MIDDLE_URI}${URLEncoder.encode(text, "UTF-8")}"
        intent.`package` = WHATS_APP_PACKAGE
        intent.data = Uri.parse(url)
        return intent
    }

    companion object {
        const val WHATS_APP_PACKAGE = "com.whatsapp"
        const val WHATS_APP_START_URI = "https://api.whatsapp.com/send?phone="
        const val WHATS_APP_MIDDLE_URI = "&text="
        const val SMS_BODY = "sms_body"
    }
}
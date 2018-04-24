package com.mnassa.screen.settings

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import com.mnassa.R
import com.mnassa.domain.other.LanguageProvider
import com.mnassa.helper.DialogHelper
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.settings.push.PushSettingsController
import com.mnassa.translation.LanguageProviderImpl
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_settings.view.*
import org.kodein.di.generic.instance


/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/19/2018
 */
class SettingsController : MnassaControllerImpl<SettingsViewModel>() {
    override val layoutId: Int = R.layout.controller_settings

    override val viewModel: SettingsViewModel by instance()
    private val dialog: DialogHelper by instance()
    private val languageProvider: LanguageProvider by instance()

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        with(view) {
            tvPushNotifications.text = fromDictionary(R.string.settings_push_row)
            tvLanguage.text = fromDictionary(R.string.settings_language_row)
            llLanguage.setOnClickListener {
                val title = fromDictionary(R.string.settings_language_change_title)
                val message = fromDictionary(R.string.settings_language_change_message)
                val text = "$title\n$message"
                dialog.yesNoDialog(view.context, getOneSpanText(text, title, Color.BLACK)) {
                    val lang = languageProvider.changeLocale()
                    val prefs = view.context.getSharedPreferences(LanguageProviderImpl.LANGUAGE_PREFERENCE, Context.MODE_PRIVATE)
                    prefs.edit().putString(LanguageProviderImpl.LANGUAGE_SETTINGS, lang).apply()
                    val intent = view.context.packageManager.getLaunchIntentForPackage(view.context.packageName)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                }
            }
            llPushSettings.setOnClickListener { open(PushSettingsController.newInstance()) }
        }
    }

    //todo move to helper class
    private fun getOneSpanText(text: String, spanText: String, color: Int): SpannableString {
        val span = SpannableString(text)
        val pointsReturnsPosition = text.indexOf(spanText)
        span.setSpan(ForegroundColorSpan(color), pointsReturnsPosition, pointsReturnsPosition + spanText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        span.setSpan(StyleSpan(Typeface.BOLD), pointsReturnsPosition, pointsReturnsPosition + spanText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return span
    }

    companion object {

        fun newInstance() = SettingsController()
    }

}
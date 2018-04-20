package com.mnassa.screen.settings

import android.view.View
import com.mnassa.R
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.settings.push.PushSettingsController
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

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        with(view) {
            tvPushNotifications.text = fromDictionary(R.string.settings_push_row)
            tvLanguage.text = fromDictionary(R.string.settings_language_row)
            llLanguage.setOnClickListener { }//todo change language
            llPushSettings.setOnClickListener { open(PushSettingsController.newInstance()) }
        }
    }

    companion object {

        fun newInstance() = SettingsController()
    }

}
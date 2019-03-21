package com.mnassa.screen.settings.push

import com.mnassa.core.addons.launchWorker
import com.mnassa.domain.interactor.SettingsInteractor
import com.mnassa.domain.model.PushSettingModel
import com.mnassa.exceptions.resolveExceptions
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BroadcastChannel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/20/2018
 */
class PushSettingsViewModelImpl(private val settingsInteractor: SettingsInteractor) : MnassaViewModelImpl(), PushSettingsViewModel {
    override val settingsChannel: BroadcastChannel<List<PushSettingModel>> = BroadcastChannel(10)

    override fun onSetup(setupScope: CoroutineScope) {
        super.onSetup(setupScope)
        launchWorker {
            val settings = settingsInteractor.getUserPushSettings()
            settingsChannel.send(settings)
        }
    }

    override fun changeSetting(setting: PushSettingModel) {
        launchWorker {
            withProgressSuspend {
                val settings = settingsInteractor.changeSetting(setting)
                settingsChannel.send(settings)
            }
        }
    }
}
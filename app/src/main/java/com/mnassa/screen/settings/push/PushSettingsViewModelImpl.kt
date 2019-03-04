package com.mnassa.screen.settings.push

import com.mnassa.domain.interactor.SettingsInteractor
import com.mnassa.domain.model.PushSettingModel
import com.mnassa.exceptions.resolveExceptions
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BroadcastChannel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/20/2018
 */
class PushSettingsViewModelImpl(private val settingsInteractor: SettingsInteractor) : MnassaViewModelImpl(), PushSettingsViewModel {

    override val notificationChangeChannel: BroadcastChannel<List<PushSettingModel>> = BroadcastChannel(10)

    override suspend fun getSettings(): List<PushSettingModel> = settingsInteractor.getUserPushSettings()

    private var changePushJob: Job? = null
    override fun changeSetting(setting: PushSettingModel) {
        changePushJob?.cancel()
        changePushJob = resolveExceptions {
            withProgressSuspend {
                val result = settingsInteractor.changeSetting(setting)
                notificationChangeChannel.send(result)
            }
        }

    }
}
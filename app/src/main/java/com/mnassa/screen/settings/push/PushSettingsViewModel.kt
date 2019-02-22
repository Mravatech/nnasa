package com.mnassa.screen.settings.push

import com.mnassa.domain.model.PushSettingModel
import com.mnassa.screen.base.MnassaViewModel
import kotlinx.coroutines.channels.BroadcastChannel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/20/2018
 */
interface PushSettingsViewModel : MnassaViewModel {

    val notificationChangeChannel: BroadcastChannel<List<PushSettingModel>>
    suspend fun getSettings(): List<PushSettingModel>
    fun changeSetting(setting: PushSettingModel)


}
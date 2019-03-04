package com.mnassa.domain.interactor

import com.mnassa.domain.model.PushSettingModel
import kotlinx.coroutines.channels.ReceiveChannel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/19/2018
 */
interface SettingsInteractor {

    suspend fun getUserPushSettings(): List<PushSettingModel>
    suspend fun changeSetting(setting: PushSettingModel): List<PushSettingModel>
    suspend fun getMaintenanceServerStatus(): ReceiveChannel<Boolean>

}
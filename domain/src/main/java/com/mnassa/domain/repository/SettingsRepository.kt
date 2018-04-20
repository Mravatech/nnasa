package com.mnassa.domain.repository

import com.mnassa.domain.model.PushSettingModel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/19/2018
 */

interface SettingsRepository {

    suspend fun getUserPushSettings(): List<PushSettingModel>
    suspend fun changeSetting(setting: PushSettingModel): List<PushSettingModel>

}
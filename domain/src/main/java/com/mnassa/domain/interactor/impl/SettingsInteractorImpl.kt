package com.mnassa.domain.interactor.impl

import com.mnassa.domain.interactor.SettingsInteractor
import com.mnassa.domain.model.PushSettingModel
import com.mnassa.domain.repository.SettingsRepository

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/19/2018
 */
class SettingsInteractorImpl(private val settingsRepository: SettingsRepository) : SettingsInteractor {
    override suspend fun getUserPushSettings(): List<PushSettingModel> = settingsRepository.getUserPushSettings()

    override suspend fun changeSetting(setting: PushSettingModel) =  settingsRepository.changeSetting(setting)
}
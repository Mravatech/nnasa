package com.mnassa.data.converter

import com.mnassa.core.converter.ConvertersContext
import com.mnassa.core.converter.ConvertersContextRegistrationCallback
import com.mnassa.core.converter.registerConverter
import com.mnassa.data.network.bean.firebase.PushSettingDbEntity
import com.mnassa.domain.model.PushSettingModel
import com.mnassa.domain.model.impl.PushSettingModelImpl

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/20/2018
 */

class PushSettingsConverter : ConvertersContextRegistrationCallback {

    override fun register(convertersContext: ConvertersContext) {
        convertersContext.registerConverter(this::convertPushSettings)
    }

    private fun convertPushSettings(input: PushSettingDbEntity): PushSettingModel {
        return PushSettingModelImpl(
                isActive = input.isActive ?: PushSettingModel.DEFAULT_IS_ACTIVE,
                withSound = input.withSound ?: PushSettingModel.DEFAULT_WITH_SOUND,
                name = input.id
        )
    }
}
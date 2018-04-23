package com.mnassa.data.converter

import com.androidkotlincore.entityconverter.ConvertersContext
import com.androidkotlincore.entityconverter.ConvertersContextRegistrationCallback
import com.androidkotlincore.entityconverter.registerConverter
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

    private fun convertPushSettings(input: PushSettingDbEntity, token: Any?, converter: ConvertersContext): PushSettingModel {
        return PushSettingModelImpl(
                isActive = input.isActive,
                withSound = input.withSound,
                name = input.id
        )
    }
}
package com.mnassa.domain.model.impl

import com.mnassa.domain.model.PushSettingModel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/20/2018
 */
data class PushSettingModelImpl(
        override val isActive: Boolean,
        override val withSound: Boolean,
        override var name: String
) : PushSettingModel
package com.mnassa.domain.model

import java.io.Serializable

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/20/2018
 */
interface PushSettingModel : Serializable{
    val name: String
    val isActive: Boolean
    val withSound: Boolean

    companion object {
        const val DEFAULT_IS_ACTIVE = false
        const val DEFAULT_WITH_SOUND = false
    }
}
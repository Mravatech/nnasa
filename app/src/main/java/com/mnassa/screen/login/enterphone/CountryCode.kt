package com.mnassa.screen.login.enterphone

import com.mnassa.domain.model.TranslatedWordModel

/**
 * Created by Peter on 3/1/2018.
 */
data class CountryCode(val flagRes: Int, val name: TranslatedWordModel, val phonePrefix: PhonePrefix)

sealed class PhonePrefix(val code: String) {
    object SaudiArabia : PhonePrefix("+966")
    object Ukraine : PhonePrefix("+380")
    object UnitedState : PhonePrefix("+1")
    object Canada : PhonePrefix("+1")
}
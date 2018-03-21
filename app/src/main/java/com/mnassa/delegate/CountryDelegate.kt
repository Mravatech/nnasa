package com.mnassa.delegate

import com.mnassa.R
import com.mnassa.domain.model.impl.TranslatedWordModelImpl
import com.mnassa.screen.login.enterphone.CountryCode
import com.mnassa.screen.login.enterphone.PhonePrefix
import com.mnassa.translation.fromDictionary
import kotlin.reflect.KProperty

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/21/2018
 */
class CountryDelegate {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): MutableList<CountryCode>{
        return mutableListOf(
                CountryCode(
                        flagRes = R.drawable.ic_flag_of_saudi_arabia,
                        name = TranslatedWordModelImpl(fromDictionary(R.string.country_saudi_arabia)),
                        phonePrefix = PhonePrefix.SaudiArabia),
                CountryCode(
                        flagRes = R.drawable.ic_flag_of_ukraine,
                        name = TranslatedWordModelImpl(fromDictionary(R.string.country_ukraine)),
                        phonePrefix = PhonePrefix.Ukraine),
                CountryCode(
                        flagRes = R.drawable.ic_flag_of_the_united_states,
                        name = TranslatedWordModelImpl(fromDictionary(R.string.country_united_states)),
                        phonePrefix = PhonePrefix.UnitedState),
                CountryCode(
                        flagRes = R.drawable.ic_flag_of_canada,
                        name = TranslatedWordModelImpl(fromDictionary(R.string.country_canada)),
                        phonePrefix = PhonePrefix.Canada)
        )
    }
}
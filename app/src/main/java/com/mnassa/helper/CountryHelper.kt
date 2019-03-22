package com.mnassa.helper

import com.mnassa.R
import com.mnassa.domain.model.impl.TranslatedWordModelImpl
import com.mnassa.domain.other.LanguageProvider
import com.mnassa.screen.login.enterphone.CountryCode
import com.mnassa.screen.login.enterphone.PhonePrefix
import com.mnassa.translation.fromDictionary


class CountryHelper(languageProvider: LanguageProvider) {
        val countries = mutableListOf(
                CountryCode(
                        flagRes = R.drawable.ic_flag_of_saudi_arabia,
                        name = TranslatedWordModelImpl(languageProvider, fromDictionary(R.string.country_saudi_arabia)),
                        phonePrefix = PhonePrefix.SaudiArabia),
                CountryCode(
                        flagRes = R.drawable.ic_flag_of_ukraine,
                        name = TranslatedWordModelImpl(languageProvider, fromDictionary(R.string.country_ukraine)),
                        phonePrefix = PhonePrefix.Ukraine),
                CountryCode(
                        flagRes = R.drawable.ic_flag_of_the_united_states,
                        name = TranslatedWordModelImpl(languageProvider, fromDictionary(R.string.country_united_states)),
                        phonePrefix = PhonePrefix.UnitedState),
                CountryCode(
                        flagRes = R.drawable.ic_flag_of_canada,
                        name = TranslatedWordModelImpl(languageProvider, fromDictionary(R.string.country_canada)),
                        phonePrefix = PhonePrefix.Canada),
                CountryCode(
                        flagRes = R.drawable.ic_flag_of_egypt,
                        name = TranslatedWordModelImpl(languageProvider, fromDictionary(R.string.country_egypt)),
                        phonePrefix = PhonePrefix.Egypt)
        )
}
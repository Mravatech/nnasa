package com.mnassa.data.converter

import com.androidkotlincore.entityconverter.ConvertersContext
import com.androidkotlincore.entityconverter.ConvertersContextRegistrationCallback
import com.androidkotlincore.entityconverter.registerConverter
import com.mnassa.data.network.bean.firebase.TranslatedWordBean
import com.mnassa.domain.models.impl.TranslatedWordImpl

/**
 * Created by Peter on 2/23/2018.
 */
class TranslatedWordConverter : ConvertersContextRegistrationCallback {
    override fun register(convertersContext: ConvertersContext) {
        convertersContext.registerConverter(this::convert)
    }

    private fun convert(input: TranslatedWordBean): TranslatedWordImpl {
        return TranslatedWordImpl(
                id = input.id,
                engTranslate = input.en,
                arabicTranslate = input.ar,
                info = input.info)
    }
}
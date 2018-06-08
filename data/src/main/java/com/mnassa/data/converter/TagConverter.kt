package com.mnassa.data.converter

import com.androidkotlincore.entityconverter.ConvertersContext
import com.androidkotlincore.entityconverter.ConvertersContextRegistrationCallback
import com.androidkotlincore.entityconverter.registerConverter
import com.mnassa.data.network.bean.firebase.TagDbEntity
import com.mnassa.domain.model.impl.TagModelImpl
import com.mnassa.domain.model.impl.TranslatedWordModelImpl
import com.mnassa.domain.other.LanguageProvider

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/13/2018
 */
class TagConverter(private val languageProvider: LanguageProvider) : ConvertersContextRegistrationCallback {

    override fun register(convertersContext: ConvertersContext) {
        convertersContext.registerConverter(this::convertTag)
    }

    private fun convertTag(input: TagDbEntity): TagModelImpl {
        return TagModelImpl(
                status = input.status,
                name = TranslatedWordModelImpl(
                        languageProvider = languageProvider,
                        id = input.id,
                        info = input.en ?: input.ar
                        ?: "",
                        engTranslate = input.en,
                        arabicTranslate = input.ar
                ),
                id = input.id
        )
    }

}
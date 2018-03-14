package com.mnassa.data.converter

import com.androidkotlincore.entityconverter.ConvertersContext
import com.androidkotlincore.entityconverter.ConvertersContextRegistrationCallback
import com.androidkotlincore.entityconverter.registerConverter
import com.mnassa.data.network.bean.firebase.TagDbEntity
import com.mnassa.domain.model.impl.TagModelImpl
import com.mnassa.domain.other.LanguageProvider

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/13/2018
 */
class TagConverter(languageProviderLazy: () -> LanguageProvider) : ConvertersContextRegistrationCallback {

    private val languageProvider: LanguageProvider by lazy(languageProviderLazy)
    private val isArabian = languageProvider.isArabian
    override fun register(convertersContext: ConvertersContext) {
        if (isArabian) {
            convertersContext.registerConverter(this::convertTagAr)
        } else {
            convertersContext.registerConverter(this::convertTagEn)
        }
    }

    private fun convertTagEn(input: TagDbEntity): TagModelImpl {
        return TagModelImpl(input.status, input.en, input.id)
    }

    private fun convertTagAr(input: TagDbEntity): TagModelImpl {
        return TagModelImpl(input.status, input.ar, input.id)
    }
}
package com.mnassa.data.converter

import com.androidkotlincore.entityconverter.ConvertersContext
import com.androidkotlincore.entityconverter.ConvertersContextRegistrationCallback
import com.androidkotlincore.entityconverter.registerConverter
import com.mnassa.data.network.bean.firebase.TagDbEntity
import com.mnassa.domain.model.impl.TagModelTempImpl
import com.mnassa.domain.other.LanguageProvider

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/13/2018
 */
class TempTagConverter(languageProviderLazy: () -> LanguageProvider) : ConvertersContextRegistrationCallback {

    private val languageProvider: LanguageProvider by lazy(languageProviderLazy)
    private val isArabian = languageProvider.isArabian
    override fun register(convertersContext: ConvertersContext) {
        if (isArabian)convertersContext.registerConverter(this::convertTagAr)
        else convertersContext.registerConverter(this::convertTagEn)
    }

    private fun convertTagEn(input: TagDbEntity): TagModelTempImpl {
        return TagModelTempImpl(input.id, input.en, input.status)
    }

    private fun convertTagAr(input: TagDbEntity): TagModelTempImpl {
        return TagModelTempImpl(input.id, input.ar, input.status)
    }
}
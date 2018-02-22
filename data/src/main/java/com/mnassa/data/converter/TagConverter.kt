package com.mnassa.data.converter

import com.androidkotlincore.entityconverter.ConvertersContext
import com.androidkotlincore.entityconverter.ConvertersContextRegistrationCallback
import com.androidkotlincore.entityconverter.registerConverter
import com.mnassa.data.network.bean.firebase.TagBean
import com.mnassa.domain.models.impl.TagModelImpl

/**
 * Created by Peter on 2/22/2018.
 */
class TagConverter : ConvertersContextRegistrationCallback {
    override fun register(convertersContext: ConvertersContext) {
        convertersContext.registerConverter(this::convertTag)
    }

    private fun convertTag(input: TagBean): TagModelImpl {
        return TagModelImpl(input.id, input.status, input.ar, input.en)
    }
}
package com.mnassa.data.converter

import com.androidkotlincore.entityconverter.ConvertersContext
import com.androidkotlincore.entityconverter.ConvertersContextRegistrationCallback
import com.androidkotlincore.entityconverter.registerConverter
import com.mnassa.data.network.bean.firebase.EventDbEntity
import com.mnassa.domain.model.impl.EventModelImpl

/**
 * Created by Peter on 4/13/2018.
 */
class EventsConverter : ConvertersContextRegistrationCallback {

    override fun register(convertersContext: ConvertersContext) {
        convertersContext.registerConverter(this::convertEvent)
    }

    private fun convertEvent(eventDbEntity: EventDbEntity, tag: Any?, convertersContext: ConvertersContext): EventModelImpl {
        TODO()
    }
}
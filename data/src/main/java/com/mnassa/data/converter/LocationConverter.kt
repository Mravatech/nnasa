package com.mnassa.data.converter

import com.androidkotlincore.entityconverter.ConvertersContext
import com.androidkotlincore.entityconverter.ConvertersContextRegistrationCallback
import com.androidkotlincore.entityconverter.registerConverter
import com.mnassa.data.network.bean.firebase.LocationDbEntity
import com.mnassa.domain.model.TranslatedWordModel
import com.mnassa.domain.model.impl.LocationPlaceModelImpl
import com.mnassa.domain.model.impl.TranslatedWordModelImpl

/**
 * Created by Peter on 3/15/2018.
 */
class LocationConverter : ConvertersContextRegistrationCallback {
    override fun register(convertersContext: ConvertersContext) {
        convertersContext.registerConverter(this::convertLocationPlace)
    }

    private fun convertLocationPlace(input: LocationDbEntity): LocationPlaceModelImpl {
        val city: TranslatedWordModel? =
                if (!input.en?.city.isNullOrBlank() || !input.ar?.city.isNullOrBlank()) {
                    TranslatedWordModelImpl("", "", input.en?.city, input.ar?.city)
                } else null

        val placeName: TranslatedWordModel? =
                if (!input.en?.placeName.isNullOrBlank() || !input.ar?.placeName.isNullOrBlank()) {
                    TranslatedWordModelImpl("", "", input.en?.placeName, input.ar?.placeName)
                } else null

        return LocationPlaceModelImpl(city = city, lat = input.en?.lat ?: 0.0, lng = input.en?.lng ?: 0.0, placeId = input.placeId, placeName = placeName)
    }
}
package com.mnassa.data.converter

import com.mnassa.core.converter.ConvertersContext
import com.mnassa.core.converter.ConvertersContextRegistrationCallback
import com.mnassa.core.converter.registerConverter
import com.mnassa.data.network.bean.firebase.LocationDbEntity
import com.mnassa.domain.model.TranslatedWordModel
import com.mnassa.domain.model.impl.LocationPlaceModelImpl
import com.mnassa.domain.model.impl.TranslatedWordModelImpl
import com.mnassa.domain.other.LanguageProvider

/**
 * Created by Peter on 3/15/2018.
 */
class LocationConverter(private val locationProvider: LanguageProvider) : ConvertersContextRegistrationCallback {
    override fun register(convertersContext: ConvertersContext) {
        convertersContext.registerConverter(this::convertLocationPlace)
    }

    private fun convertLocationPlace(input: LocationDbEntity): LocationPlaceModelImpl {
        val city: TranslatedWordModel? =
                if (!input.en?.city.isNullOrBlank() || !input.ar?.city.isNullOrBlank()) {
                    TranslatedWordModelImpl(locationProvider,"", input.en?.city ?: input.ar?.city ?: "", input.en?.city, input.ar?.city)
                } else null

        val placeName: TranslatedWordModel? =
                if (!input.en?.placeName.isNullOrBlank() || !input.ar?.placeName.isNullOrBlank()) {
                    TranslatedWordModelImpl(locationProvider,"", input.en?.placeName ?: input.ar?.placeName ?: "", input.en?.placeName, input.ar?.placeName)
                } else null

        return LocationPlaceModelImpl(city = city, lat = input.en?.lat ?: 0.0, lng = input.en?.lng ?: 0.0, placeId = input.placeId, placeName = placeName)
    }
}
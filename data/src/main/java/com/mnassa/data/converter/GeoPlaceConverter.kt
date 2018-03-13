package com.mnassa.data.converter

import android.graphics.Typeface
import android.text.style.StyleSpan
import com.androidkotlincore.entityconverter.ConvertersContext
import com.androidkotlincore.entityconverter.ConvertersContextRegistrationCallback
import com.androidkotlincore.entityconverter.registerConverter
import com.google.android.gms.location.places.AutocompletePrediction
import com.mnassa.domain.model.GeoPlaceModel
import com.mnassa.domain.model.impl.GeoPlaceModelImpl

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/12/2018
 */

class GeoPlaceConverter : ConvertersContextRegistrationCallback {
    override fun register(convertersContext: ConvertersContext) {
        convertersContext.registerConverter(this::convertPlace)
    }

    private fun convertPlace(input: AutocompletePrediction): GeoPlaceModel {
        return GeoPlaceModelImpl(input.placeId, input.getPrimaryText(STYLE_BOLD), input.getSecondaryText(STYLE_BOLD))
    }

    companion object {
        private val STYLE_BOLD = StyleSpan(Typeface.BOLD)
    }
}
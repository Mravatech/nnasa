package com.mnassa.domain.model

import java.io.Serializable

/**
 * Created by Peter on 3/15/2018.
 */
interface LocationPlaceModel : Serializable {
    val city: TranslatedWordModel?
    val lat: Double
    val lng: Double
    val placeId: String
    val placeName: TranslatedWordModel?
}
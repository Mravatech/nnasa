package com.mnassa.domain.model.impl

import com.mnassa.domain.model.LocationPlaceModel
import com.mnassa.domain.model.TranslatedWordModel

/**
 * Created by Peter on 3/15/2018.
 */
data class LocationPlaceModelImpl(override val city: TranslatedWordModel?,
                                  override val lat: Double,
                                  override val lng: Double,
                                  override val placeId: String,
                                  override val placeName: TranslatedWordModel?) : LocationPlaceModel {

}
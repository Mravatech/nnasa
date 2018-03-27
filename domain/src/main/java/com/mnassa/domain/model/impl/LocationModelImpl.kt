package com.mnassa.domain.model.impl

import com.mnassa.domain.model.LocationDetailModel
import com.mnassa.domain.model.LocationModel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/27/2018
 */

data class LocationModelImpl(
        override val placeId: String?,
        override val en: LocationDetailModel?,
        override val ar: LocationDetailModel?) : LocationModel

data class LocationDetailModelImpl(
        override val city: String?,
        override val lat: Double?,
        override val lng: Double?,
        override val placeId: String?,
        override val placeName: String?) : LocationDetailModel
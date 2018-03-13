package com.mnassa.domain.model.impl

import com.mnassa.domain.model.GeoPlaceModel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/12/2018
 */
data class GeoPlaceModelImpl(
        override val placeId: String?,
        override val primaryText: CharSequence,
        override val secondaryText: CharSequence
) : GeoPlaceModel
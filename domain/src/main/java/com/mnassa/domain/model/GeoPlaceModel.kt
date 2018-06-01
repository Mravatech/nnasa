package com.mnassa.domain.model

import java.io.Serializable

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/12/2018
 */
interface GeoPlaceModel : Serializable {
    val placeId: String?
    val primaryText: CharSequence
    val secondaryText: CharSequence
}
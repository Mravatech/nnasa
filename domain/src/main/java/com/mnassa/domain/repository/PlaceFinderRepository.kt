package com.mnassa.domain.repository

import com.mnassa.domain.model.GeoPlaceModel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/12/2018
 */

interface PlaceFinderRepository {
    fun getReqieredPlaces(constraint: CharSequence): List<GeoPlaceModel>
}
package com.mnassa.domain.interactor

import com.mnassa.domain.model.GeoPlaceModel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/12/2018
 */
interface PlaceFinderInteractor {
    fun getReqieredPlaces(constraint: CharSequence): List<GeoPlaceModel>?
}
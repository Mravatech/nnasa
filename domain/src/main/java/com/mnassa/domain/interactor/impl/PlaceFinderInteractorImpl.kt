package com.mnassa.domain.interactor.impl

import com.mnassa.domain.interactor.PlaceFinderInteractor
import com.mnassa.domain.model.GeoPlaceModel
import com.mnassa.domain.repository.PlaceFinderRepository

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 3/12/2018
 */
class PlaceFinderInteractorImpl(private val placeFinderRepository: PlaceFinderRepository) : PlaceFinderInteractor {
    override fun getReqieredPlaces(constraint: CharSequence): List<GeoPlaceModel> {
        return placeFinderRepository.getReqieredPlaces(constraint)
    }
}
package com.mnassa.screen.events.create

import com.mnassa.domain.interactor.EventsInteractor
import com.mnassa.domain.interactor.PlaceFinderInteractor
import com.mnassa.domain.model.GeoPlaceModel
import com.mnassa.screen.base.MnassaViewModelImpl

/**
 * Created by Peter on 4/23/2018.
 */
class CreateEventViewModelImpl(private val eventId: String?,
                               private val eventsInteractor: EventsInteractor,
                               private val placeFinderInteractor: PlaceFinderInteractor) : MnassaViewModelImpl(), CreateEventViewModel {

    override fun getAutocomplete(constraint: CharSequence): List<GeoPlaceModel> {
        return placeFinderInteractor.getReqieredPlaces(constraint)
    }
}
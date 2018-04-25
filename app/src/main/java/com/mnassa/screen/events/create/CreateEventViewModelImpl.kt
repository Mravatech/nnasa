package com.mnassa.screen.events.create

import com.mnassa.domain.interactor.EventsInteractor
import com.mnassa.screen.base.MnassaViewModelImpl

/**
 * Created by Peter on 4/23/2018.
 */
class CreateEventViewModelImpl(private val eventId: String?, private val eventsInteractor: EventsInteractor) : MnassaViewModelImpl(), CreateEventViewModel {

}
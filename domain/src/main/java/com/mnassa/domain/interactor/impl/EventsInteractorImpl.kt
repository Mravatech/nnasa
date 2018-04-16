package com.mnassa.domain.interactor.impl

import com.mnassa.domain.interactor.EventsInteractor
import com.mnassa.domain.repository.EventsRepository

/**
 * Created by Peter on 4/13/2018.
 */
class EventsInteractorImpl(private val eventsRepository: EventsRepository) : EventsInteractor, EventsRepository by eventsRepository {
}
package com.mnassa.screen.events

import android.os.Bundle
import com.mnassa.domain.repository.EventsRepository
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by Peter on 3/6/2018.
 */
class EventsViewModelImpl(private val eventsRepository: EventsRepository) : MnassaViewModelImpl(), EventsViewModel {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleException {
            eventsRepository.getEventsFeedChannel().consumeEach {

            }
        }
    }
}
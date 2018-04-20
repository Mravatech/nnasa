package com.mnassa.screen.events.details

import android.os.Bundle
import com.mnassa.domain.interactor.EventsInteractor
import com.mnassa.domain.model.EventModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by Peter on 4/17/2018.
 */
class EventDetailsViewModelImpl(private val eventId: String,
                                private val eventsInteractor: EventsInteractor) : MnassaViewModelImpl(), EventDetailsViewModel {
    override val eventChannel: ConflatedBroadcastChannel<EventModel> = ConflatedBroadcastChannel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleException {
            eventsInteractor.loadByIdChannel(eventId).consumeEach {
                if (it != null) {
                    eventChannel.send(it)
                } else {
                    //TODO
                }
            }
        }
    }
}
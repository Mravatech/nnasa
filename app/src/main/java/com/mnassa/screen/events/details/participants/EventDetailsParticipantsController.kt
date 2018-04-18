package com.mnassa.screen.events.details.participants

import android.os.Bundle
import com.mnassa.R
import com.mnassa.screen.base.MnassaControllerImpl
import org.kodein.di.generic.instance

/**
 * Created by Peter on 4/18/2018.
 */
class EventDetailsParticipantsController(args: Bundle) : MnassaControllerImpl<EventDetailsParticipantsViewModel>(args) {
    private val eventId by lazy { args.getString(EXTRA_EVENT_ID) }
    override val layoutId: Int = R.layout.controller_event_details_participants
    override val viewModel: EventDetailsParticipantsViewModel by instance(arg = eventId)

    companion object {
        private const val EXTRA_EVENT_ID = "EXTRA_EVENT_ID"

        fun newInstance(eventId: String): EventDetailsParticipantsController {
            val args = Bundle()
            args.putString(EXTRA_EVENT_ID, eventId)
            return EventDetailsParticipantsController(args)
        }
    }
}
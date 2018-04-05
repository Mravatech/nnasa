package com.mnassa.screen.events

import org.kodein.di.generic.instance
import com.mnassa.R
import com.mnassa.screen.base.MnassaControllerImpl

/**
 * Created by Peter on 3/6/2018.
 */
class EventsController : MnassaControllerImpl<EventsViewModel>() {
    override val layoutId: Int = R.layout.controller_events_list
    override val viewModel: EventsViewModel by instance()

    companion object {
        fun newInstance() = EventsController()
    }
}
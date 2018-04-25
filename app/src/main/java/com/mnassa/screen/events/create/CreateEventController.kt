package com.mnassa.screen.events.create

import android.os.Bundle
import com.mnassa.R
import com.mnassa.domain.model.EventModel
import com.mnassa.screen.base.MnassaControllerImpl
import org.kodein.di.generic.instance

/**
 * Created by Peter on 4/23/2018.
 */
class CreateEventController(args: Bundle) : MnassaControllerImpl<CreateEventViewModel>(args) {
    override val layoutId: Int = R.layout.controller_event_create
    override val viewModel: CreateEventViewModel by instance()



    companion object {
        private const val EXTRA_EVENT_ID = "EXTRA_EVENT_ID"
        private const val EXTRA_EVENT = "EXTRA_EVENT"

        fun newInstance(): CreateEventController = CreateEventController(Bundle())
        fun newInstance(event: EventModel): CreateEventController {
            val args = Bundle()
            args.putString(EXTRA_EVENT_ID, event.id)
            args.putSerializable(EXTRA_EVENT, event)
            return CreateEventController(args)
        }
    }
}
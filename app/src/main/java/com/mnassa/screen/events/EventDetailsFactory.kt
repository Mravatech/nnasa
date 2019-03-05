package com.mnassa.screen.events

import android.os.Bundle
import com.bluelinelabs.conductor.Controller
import com.mnassa.App
import com.mnassa.core.addons.launchWorker
import com.mnassa.di.getInstance
import com.mnassa.domain.model.EventModel
import com.mnassa.extensions.markAsOpened
import com.mnassa.screen.events.details.EventDetailsController
import com.mnassa.screen.invite.InviteSource
import com.mnassa.screen.invite.InviteSourceHolder
import kotlinx.coroutines.GlobalScope

/**
 * @author Artem Chepurnoy
 */
class EventDetailsFactory {

    fun newInstance(event: EventModel): Controller {
        GlobalScope.launchWorker {
            event.markAsOpened()
        }

        App.context.getInstance<InviteSourceHolder>().source = InviteSource.Event(event)

        val args = Bundle().apply {
            putString(EXTRA_EVENT_ID, event.id)
            putSerializable(EXTRA_EVENT, event)
        }

        return EventDetailsController(args)
    }

    companion object {
        const val EXTRA_EVENT = "EXTRA_EVENT"
        const val EXTRA_EVENT_ID = "EXTRA_EVENT_ID"
    }

}
package com.mnassa.screen.events.details.participants

import android.os.Bundle
import android.view.View
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.EventModel
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.events.details.info.EventDetailsInfoController
import com.mnassa.screen.profile.ProfileController
import kotlinx.android.synthetic.main.controller_event_details_participants.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import org.kodein.di.generic.instance

/**
 * Created by Peter on 4/18/2018.
 */
class EventDetailsParticipantsController(args: Bundle) : MnassaControllerImpl<EventDetailsParticipantsViewModel>(args) {
    private val eventId by lazy { args.getString(EXTRA_EVENT_ID) }
    private val eventParam by lazy { args[EventDetailsInfoController.EXTRA_EVENT] as EventModel? }
    override val layoutId: Int = R.layout.controller_event_details_participants
    override val viewModel: EventDetailsParticipantsViewModel by instance(arg = eventId)
    private val allParticipantsadAdapter = EventParticipantsRVAdapter()
    private val selectParticipantAdapter = EventSelectParticipantsRVAdapter()

    override fun onCreated(savedInstanceState: Bundle?) {
        super.onCreated(savedInstanceState)

        allParticipantsadAdapter.isLoadingEnabled = true
        selectParticipantAdapter.isLoadingEnabled = true
        controllerSubscriptionContainer.launchCoroutineUI {
            viewModel.participantsChannel.consumeEach {
                allParticipantsadAdapter.isLoadingEnabled = false
                selectParticipantAdapter.isLoadingEnabled = false

                allParticipantsadAdapter.set(it)
                selectParticipantAdapter.set(it.flatMap { it.withGuests() })
            }
        }
        allParticipantsadAdapter.onParticipantClickListener = { open(ProfileController.newInstance(it.user)) }
        allParticipantsadAdapter.onCheckParticipantsClickListener = {
            view?.rvParticipants?.adapter = selectParticipantAdapter
        }
        selectParticipantAdapter.onSaveClickListener = { dataSet ->
            launchCoroutineUI {
                viewModel.saveParticipants(dataSet)
                getViewSuspend().rvParticipants.adapter = allParticipantsadAdapter
            }
        }
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        view.rvParticipants.adapter = allParticipantsadAdapter
    }

    companion object {
        private const val EXTRA_EVENT_ID = "EXTRA_EVENT_ID"
        private const val EXTRA_EVENT = "EXTRA_EVENT"

        fun newInstance(eventId: String, event: EventModel? = null): EventDetailsParticipantsController {
            val args = Bundle()
            args.putString(EXTRA_EVENT_ID, eventId)
            args.putSerializable(EXTRA_EVENT, event)
            return EventDetailsParticipantsController(args)
        }
    }
}
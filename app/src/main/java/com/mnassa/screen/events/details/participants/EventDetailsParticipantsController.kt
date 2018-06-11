package com.mnassa.screen.events.details.participants

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.mnassa.R
import com.mnassa.activity.SearchActivity
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
    override val viewModel: EventDetailsParticipantsViewModel by instance(arg = eventParam)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != SearchActivity.REQUEST_CODE_SEARCH) return
        when (resultCode) {
            SearchActivity.SELECT_PARTICIPANT_RESULT -> {
                val resultList = data?.getSerializableExtra(SearchActivity.EXTRA_LIST_RESULT) as ArrayList<EventParticipantItem>
                selectParticipantAdapter.dataStorage.set(resultList)
            }
            SearchActivity.ALL_PARTICIPANT_RESULT -> {
                val item = data?.getSerializableExtra(SearchActivity.EXTRA_ITEM_TO_OPEN_SCREEN_RESULT) as EventParticipantItem.User
                open(ProfileController.newInstance(item.user))
            }
        }
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        view.rvParticipants.adapter = allParticipantsadAdapter
        selectParticipantAdapter.onSearchClickListener = {
            val intent = SearchActivity.start(view.context, selectParticipantAdapter.dataStorage.toList(), SearchActivity.SELECT_PARTICIPANT_TYPE)
            startActivityForResult(intent, SearchActivity.REQUEST_CODE_SEARCH)
        }
        allParticipantsadAdapter.onSearchClickListener = {
            val intent = SearchActivity.start(view.context,
                    allParticipantsadAdapter.dataStorage.toList(),
                    SearchActivity.ALL_PARTICIPANT_TYPE)
            startActivityForResult(intent, SearchActivity.REQUEST_CODE_SEARCH)
        }
    }

    companion object {
        private const val EXTRA_EVENT_ID = "EXTRA_EVENT_ID"
        private const val EXTRA_EVENT = "EXTRA_EVENT"

        fun newInstance(eventId: String, event: EventModel): EventDetailsParticipantsController {
            val args = Bundle()
            args.putString(EXTRA_EVENT_ID, eventId)
            args.putSerializable(EXTRA_EVENT, event)
            return EventDetailsParticipantsController(args)
        }
    }
}
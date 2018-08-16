package com.mnassa.screen.group.profile.events

import android.os.Bundle
import android.view.View
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.core.addons.launchWorker
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.EventModel
import com.mnassa.extensions.isInvisible
import com.mnassa.extensions.markAsOpened
import com.mnassa.extensions.subscribeToUpdates
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.events.EventsRVAdapter
import com.mnassa.screen.events.details.EventDetailsController
import com.mnassa.screen.group.details.GroupDetailsController
import com.mnassa.screen.profile.ProfileController
import kotlinx.android.synthetic.main.controller_group_profile_events.view.*
import org.kodein.di.generic.instance

/**
 * Created by Peter on 09.08.2018.
 */
class GroupEventsController(args: Bundle) : MnassaControllerImpl<GroupEventsViewModel>() {
    override val layoutId: Int = R.layout.controller_group_profile_events
    private val groupId: String by lazy { args.getString(EXTRA_GROUP_ID) }
    override val viewModel: GroupEventsViewModel by instance(arg = groupId)

    private val userInteractor: UserProfileInteractor by instance()
    private val adapter by lazy { EventsRVAdapter(userInteractor) }

    override fun onCreated(savedInstanceState: Bundle?) {
        super.onCreated(savedInstanceState)
        savedInstanceState?.apply { adapter.restoreState(this) }

        adapter.onAuthorClickListener = { open(ProfileController.newInstance(it.author)) }
        adapter.onGroupClickListener = { open(GroupDetailsController.newInstance(it)) }
        adapter.onItemClickListener = { openEvent(it) }

        adapter.onDataChangedListener = { itemsCount ->
            view?.rlEmptyView?.isInvisible = itemsCount > 0 || adapter.isLoadingEnabled
        }
        controllerSubscriptionContainer.launchCoroutineUI {
            viewModel.newsFeedChannel.subscribeToUpdates(
                    adapter = adapter,
                    emptyView = { getViewSuspend().rlEmptyView }
            )
        }
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        with(view) {
            rvGroupEvents.adapter = adapter
        }
    }

    override fun onSaveViewState(view: View, outState: Bundle) {
        super.onSaveViewState(view, outState)
        outState.putInt(EMPTY_STATE_VISIBILITY, view.rlEmptyView.visibility)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        adapter.saveState(outState)
    }

    override fun onRestoreViewState(view: View, savedViewState: Bundle) {
        super.onRestoreViewState(view, savedViewState)
        view.rlEmptyView.visibility = savedViewState.getInt(EMPTY_STATE_VISIBILITY, view.rlEmptyView.visibility)
    }

    override fun onDestroyView(view: View) {
        view.rvGroupEvents.adapter = null
        super.onDestroyView(view)
    }

    private fun openEvent(event: EventModel) {
        launchWorker {
            event.markAsOpened()
        }

        open(EventDetailsController.newInstance(event))
    }

    companion object {
        private const val EXTRA_GROUP_ID = "EXTRA_GROUP_ID"
        private const val EMPTY_STATE_VISIBILITY = "EMPTY_STATE_VISIBILITY"

        fun newInstance(groupId: String): GroupEventsController {
            val args = Bundle()
            args.putString(EXTRA_GROUP_ID, groupId)
            return GroupEventsController(args)
        }
    }
}
package com.mnassa.screen.group.list

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.mnassa.R
import com.mnassa.core.addons.asReference
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.GroupModel
import com.mnassa.extensions.isGone
import com.mnassa.extensions.isInvisible
import com.mnassa.extensions.setHeaderWithCounter
import com.mnassa.helper.PopupMenuHelper
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.group.create.CreateGroupController
import com.mnassa.screen.group.details.GroupDetailsController
import com.mnassa.screen.group.list.adapters.AllGroupsRecyclerViewAdapter
import com.mnassa.screen.group.list.adapters.NewGroupRequestsRecyclerViewAdapter
import com.mnassa.screen.group.profile.GroupProfileController
import com.mnassa.screen.group.requests.GroupConnectionRequestsController
import kotlinx.android.synthetic.main.controller_group_list.view.*
import kotlinx.android.synthetic.main.controller_groups_header.view.*
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.consumeEach
import org.kodein.di.generic.instance

/**
 * Created by Peter on 5/14/2018.
 */
class GroupListController : MnassaControllerImpl<GroupListViewModel>() {
    override val layoutId: Int = R.layout.controller_group_list
    override val viewModel: GroupListViewModel by instance()
    private val popupMenuHelper: PopupMenuHelper by instance()
    private val allGroupsAdapter = AllGroupsRecyclerViewAdapter(true)
    private val newConnectionRequestsAdapter = NewGroupRequestsRecyclerViewAdapter()

    override fun onCreated(savedInstanceState: Bundle?) {
        super.onCreated(savedInstanceState)

        savedInstanceState?.apply {
            allGroupsAdapter.restoreState(this)
            newConnectionRequestsAdapter.restoreState(this)
        }

        newConnectionRequestsAdapter.onAcceptClickListener = { viewModel.accept(it) }
        newConnectionRequestsAdapter.onDeclineClickListener = { viewModel.decline(it) }
        newConnectionRequestsAdapter.onItemClickListener = { open(GroupDetailsController.newInstance(it)) }
        newConnectionRequestsAdapter.onShowAllClickListener = { open(GroupConnectionRequestsController.newInstance()) }

        allGroupsAdapter.isLoadingEnabled = true
        allGroupsAdapter.onBindHeader = { bindHeader(it) }
        allGroupsAdapter.onItemOptionsClickListener = { item, view -> openGroupItemMenu(item, view) }
        allGroupsAdapter.onItemClickListener = { open(GroupProfileController.newInstance(it)) }

        controllerSubscriptionContainer.launchCoroutineUI {
            viewModel.myGroupsChannel.consumeEach {
                allGroupsAdapter.isLoadingEnabled = false
                allGroupsAdapter.setGroups(it)
            }
        }
        controllerSubscriptionContainer.launchCoroutineUI {
            viewModel.groupConnectionRequestsChannel.consumeEach {
                newConnectionRequestsAdapter.setWithMaxRange(it, MAX_REQUESTS_COUNT)
            }
        }
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        with(view) {
            rvAllGroups.adapter = allGroupsAdapter

            toolbar.onMoreClickListener = {
                popupMenuHelper.showGroupsMenu(
                        view = it,
                        openRequests = { open(GroupConnectionRequestsController.newInstance()) }
                )
            }

            fabCreateGroup.setOnClickListener {
                open(CreateGroupController.newInstance())
            }
        }

        launchCoroutineUI {
            viewModel.permissionsChannel.consumeEach {
                view.fabCreateGroup.isInvisible = !it.canCreateGroup
            }
        }
    }

    override fun onDestroyView(view: View) {
        view.rvAllGroups.adapter = null
        super.onDestroyView(view)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        allGroupsAdapter.saveState(outState)
        newConnectionRequestsAdapter.saveState(outState)
    }

    private var loadRecommendedConnectionsJob: Job? = null

    private fun bindHeader(header: View) {
        with(header) {
            rvNewConnectionRequests.itemAnimator = null
            rvNewConnectionRequests.isNestedScrollingEnabled = false

            rvNewConnectionRequests.adapter = newConnectionRequestsAdapter
            rvNewConnectionRequests.layoutManager = BlockedScrollingLayoutManager(context, RecyclerView.VERTICAL, false)
        }

        val headerRef = header.asReference()

        loadRecommendedConnectionsJob?.cancel()
        loadRecommendedConnectionsJob = launchCoroutineUI {
            viewModel.groupConnectionRequestsChannel.consumeEach {
                with(headerRef()) {
                    tvGroupInvites.setHeaderWithCounter(R.string.groups_requests, it.size)
                    tvGroupInvites.isGone = it.isEmpty()
                    vGroupInvites.isGone = it.isEmpty()

                    rvNewConnectionRequests.isGone = it.isEmpty()
                }
            }
        }
    }

    private fun openGroupItemMenu(group: GroupModel, sender: View) {
        popupMenuHelper.showGroupItemMenu(
                view = sender,
                onLeave = { viewModel.leave(group) },
                onDetails = { open(GroupDetailsController.newInstance(group)) }
        )
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private class BlockedScrollingLayoutManager(
            context: Context,
            orientation: Int,
            reverseLayout: Boolean
    ) : LinearLayoutManager(context, orientation, reverseLayout) {
        override fun canScrollHorizontally(): Boolean = false
        override fun canScrollVertically(): Boolean = false
    }

    companion object {
        private const val MAX_REQUESTS_COUNT = 3

        fun newInstance() = GroupListController()
    }
}
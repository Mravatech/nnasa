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
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.extensions.isGone
import com.mnassa.extensions.setHeaderWithCounter
import com.mnassa.helper.PopupMenuHelper
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.chats.message.ChatMessageController
import com.mnassa.screen.connections.archived.ArchivedConnectionController
import com.mnassa.screen.connections.newrequests.NewRequestsController
import com.mnassa.screen.connections.recommended.RecommendedConnectionsController
import com.mnassa.screen.connections.sent.SentConnectionsController
import com.mnassa.screen.group.list.adapters.AllGroupsRecyclerViewAdapter
import com.mnassa.screen.group.list.adapters.NewGroupRequestsRecyclerViewAdapter
import com.mnassa.screen.group.profile.GroupProfileController
import com.mnassa.screen.profile.ProfileController
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_groups_header.view.*
import kotlinx.android.synthetic.main.controller_group_list.view.*
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
        newConnectionRequestsAdapter.onDeclineClickListener = { account -> viewModel.decline(account) }
        newConnectionRequestsAdapter.onItemClickListener = { open(GroupProfileController.newInstance(it)) }
        newConnectionRequestsAdapter.onShowAllClickListener = { openNewRequestsScreen() }

        allGroupsAdapter.isLoadingEnabled = savedInstanceState == null
        allGroupsAdapter.onBindHeader = { bindHeader(it) }
        allGroupsAdapter.onItemOptionsClickListener = { item, view -> onMoreConnectedAccountFunctions(item, view) }
        allGroupsAdapter.onItemClickListener = { open(GroupProfileController.newInstance(it)) }
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        with(view) {
            toolbar.backButtonEnabled = false
            toolbar.title = fromDictionary(R.string.tab_connections_title)

            rvAllGroups.adapter = allGroupsAdapter

            toolbar.onMoreClickListener = {
                popupMenuHelper.showConnectionsTabMenu(
                        view = it,
                        openRecommendedConnectionsScreen = { openRecommendedConnectionsScreen() },
                        openSentRequestsScreen = { openSentRequestsScreen() },
                        openArchivedConnectionsScreen = { openArchivedConnectionsScreen() }
                )
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

    private var loadAllConnectionsJob: Job? = null
    private var loadRecommendedConnectionsJob: Job? = null
    private var loadNewConnectionsJob: Job? = null

    private fun bindHeader(header: View) {
        with(header) {
            rvNewConnectionRequests.itemAnimator = null
            rvNewConnectionRequests.isNestedScrollingEnabled = false

            rvNewConnectionRequests.adapter = newConnectionRequestsAdapter
            rvNewConnectionRequests.layoutManager = BlockedScrollingLayoutManager(context, RecyclerView.VERTICAL, false)
        }

        val headerRef = header.asReference()

        loadAllConnectionsJob?.cancel()
        loadAllConnectionsJob = launchCoroutineUI {
            viewModel.myGroupsChannel.consumeEach {
                allGroupsAdapter.isLoadingEnabled = false
                allGroupsAdapter.set(it)

                with(headerRef()) {
                    tvAllGroups.setHeaderWithCounter(R.string.tab_connections_all, it.size)
                    tvAllGroups.isGone = it.isEmpty()
                    vAllGroups.isGone = it.isEmpty()
                }
            }
        }

        loadRecommendedConnectionsJob?.cancel()
        loadRecommendedConnectionsJob = launchCoroutineUI {
            viewModel.groupConnectionRequestsChannel.consumeEach {
                newConnectionRequestsAdapter.setWithMaxRange(it, MAX_RECOMMENDED_ITEMS_COUNT)

                with(headerRef()) {
                    tvGroupInvites.setHeaderWithCounter(R.string.groups_requests, it.size)
                    tvGroupInvites.isGone = it.isEmpty()
                    vGroupInvites.isGone = it.isEmpty()

                    rvNewConnectionRequests.isGone = it.isEmpty()
                }
            }
        }
//
//        loadNewConnectionsJob?.cancel()
//        loadNewConnectionsJob = launchCoroutineUI {
//            viewModel.newConnectionRequestsChannel.consumeEach {
//                newConnectionRequestsAdapter.setWithMaxRange(it, MAX_REQUESTED_ITEMS_COUNT)
//
//                with(headerRef()) {
//                    tvNewConnectionRequests.isGone = it.isEmpty()
//                    rvNewConnectionRequests.isGone = it.isEmpty()
//                    vNewConnectionRequests.isGone = it.isEmpty()
//
//                    tvNewConnectionRequests.setHeaderWithCounter(R.string.tab_connections_new_requests, it.size)
//                }
//            }
//        }
    }

    ///////////////////////////////////////// CONNECTION TYPE SCREENS ///////////////////////////////

    private fun openRecommendedConnectionsScreen() = open(RecommendedConnectionsController.newInstance())
    private fun openSentRequestsScreen() = open(SentConnectionsController.newInstance())
    private fun openArchivedConnectionsScreen() = open(ArchivedConnectionController.newInstance())
    private fun openNewRequestsScreen() = open(NewRequestsController.newInstance())
    private fun openChat(accountModel: ShortAccountModel) = open(ChatMessageController.newInstance(accountModel))
    private fun openProfile(accountModel: ShortAccountModel) = open(ProfileController.newInstance(accountModel))

    private fun onMoreConnectedAccountFunctions(accountModel: GroupModel, sender: View) {
//        popupMenuHelper.showConnectedAccountMenu(
//                view = sender,
//                onChat = { openChat(accountModel) },
//                onProfile = { openProfile(accountModel) },
//                onDisconnect = { viewModel.disconnect(accountModel) })
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
        private const val MAX_RECOMMENDED_ITEMS_COUNT = 10
        private const val MAX_REQUESTED_ITEMS_COUNT = 2

        fun newInstance() = GroupListController()
    }
}
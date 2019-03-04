package com.mnassa.screen.group.requests

import android.view.View
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.extensions.isInvisible
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.group.details.GroupDetailsController
import com.mnassa.screen.group.list.adapters.NewGroupRequestsRecyclerViewAdapter
import kotlinx.android.synthetic.main.controller_group_connection_requests.view.*
import kotlinx.coroutines.channels.consumeEach
import org.kodein.di.generic.instance

/**
 * Created by Peter on 5/22/2018.
 */
class GroupConnectionRequestsController : MnassaControllerImpl<GroupConnectionRequestsViewModel>() {
    override val layoutId: Int = R.layout.controller_group_connection_requests
    override val viewModel: GroupConnectionRequestsViewModel by instance()
    private val adapter = NewGroupRequestsRecyclerViewAdapter()

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        adapter.onAcceptClickListener = { viewModel.accept(it) }
        adapter.onDeclineClickListener = { viewModel.decline(it) }
        adapter.onItemClickListener = { open(GroupDetailsController.newInstance(it)) }

        view.rvGroupRequests.adapter = adapter

        adapter.isLoadingEnabled = true
        launchCoroutineUI {
            viewModel.groupConnectionRequestsChannel.consumeEach {
                adapter.isLoadingEnabled = false
                adapter.set(it)

                view.rlEmptyView.isInvisible = it.isNotEmpty()
                view.rvGroupRequests.isInvisible = it.isEmpty()
            }
        }
    }

    override fun onDestroyView(view: View) {
        adapter.destroyCallbacks()
        view.rvGroupRequests.adapter = null
        super.onDestroyView(view)
    }

    companion object {
        fun newInstance() = GroupConnectionRequestsController()
    }
}
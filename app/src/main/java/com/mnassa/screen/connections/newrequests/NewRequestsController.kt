package com.mnassa.screen.connections.newrequests

import android.support.v7.widget.LinearLayoutManager
import android.view.View
import org.kodein.di.generic.instance
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.connections.adapters.NewConnectionRequestsRecyclerViewAdapter
import com.mnassa.screen.profile.ProfileController
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_connections_new_requests.view.*
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by Peter on 9.03.2018.
 */
class NewRequestsController : MnassaControllerImpl<NewRequestsViewModel>() {
    override val layoutId: Int = R.layout.controller_connections_new_requests
    override val viewModel: NewRequestsViewModel by instance()
    private val adapter = NewConnectionRequestsRecyclerViewAdapter()

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        adapter.onAcceptClickListener = { viewModel.accept(it) }
        adapter.onDeclineClickListener = { viewModel.decline(it) }
        adapter.onItemClickListener = { open(ProfileController.newInstance(it)) }

        with(view) {
            toolbar.title = fromDictionary(R.string.new_requests_title)
            tvEmptyTitle.text = fromDictionary(R.string.new_requests_empty_title)
            tvEmptyDescription.text = fromDictionary(R.string.new_requests_empty_description)

            rvNewConnectionRequests.layoutManager = LinearLayoutManager(context)
            rvNewConnectionRequests.adapter = adapter
        }

        adapter.isLoadingEnabled = true
        launchCoroutineUI {
            viewModel.newConnectionRequestsChannel.consumeEach {
                adapter.isLoadingEnabled = false
                adapter.set(it)
                view.rlEmptyView.visibility = if (it.isEmpty()) View.VISIBLE else View.INVISIBLE
                view.rvNewConnectionRequests.visibility = if (it.isNotEmpty()) View.VISIBLE else View.INVISIBLE
            }
        }
    }

    override fun onDestroyView(view: View) {
        adapter.destroyCallbacks()
        view.rvNewConnectionRequests.adapter = null
        super.onDestroyView(view)
    }

    companion object {
        fun newInstance() = NewRequestsController()
    }
}
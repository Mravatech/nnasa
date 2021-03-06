package com.mnassa.screen.connections.newrequests

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.extensions.isInvisible
import com.mnassa.helper.DialogHelper
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.connections.adapters.NewConnectionRequestsRecyclerViewAdapter
import com.mnassa.screen.profile.ProfileController
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_connections_new_requests.view.*
import kotlinx.coroutines.channels.consumeEach
import org.kodein.di.generic.instance

/**
 * Created by Peter on 9.03.2018.
 */
class NewRequestsController : MnassaControllerImpl<NewRequestsViewModel>() {
    override val layoutId: Int = R.layout.controller_connections_new_requests
    override val viewModel: NewRequestsViewModel by instance()
    private val adapter = NewConnectionRequestsRecyclerViewAdapter()
    private val dialog: DialogHelper by instance()

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        adapter.onAcceptClickListener = { viewModel.accept(it) }
        adapter.onDeclineClickListener = {
            launchCoroutineUI {
                val disconnectDays = viewModel.getDisconnectTimeoutDays()
                dialog.showDeclineConnectionDialog(view.context, disconnectDays) {
                    viewModel.decline(it)
                }
            }
        }
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

                view.rlEmptyView.isInvisible = it.isNotEmpty()
                view.rvNewConnectionRequests.isInvisible = it.isEmpty()
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
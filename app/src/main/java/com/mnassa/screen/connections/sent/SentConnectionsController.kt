package com.mnassa.screen.connections.sent

import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_connections_sent.view.*
import kotlinx.android.synthetic.main.header_main.view.*
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by Peter on 9.03.2018.
 */
class SentConnectionsController : MnassaControllerImpl<SentConnectionsViewModel>() {
    override val layoutId: Int = R.layout.controller_connections_sent
    override val viewModel: SentConnectionsViewModel by instance()
    private val adapter = SentConnectionsRecyclerViewAdapter()

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        with(view) {
            tvScreenHeader.text = fromDictionary(R.string.sent_connection_requests_title)
            rvSentConnections.layoutManager = LinearLayoutManager(context)
            rvSentConnections.adapter = adapter

            adapter.onCancelClickListener = { viewModel.cancelRequest(it) }
        }

        adapter.isLoadingEnabled = true
        launchCoroutineUI {
            viewModel.sentConnectionsChannel.consumeEach {
                view.rlEmptyView.visibility = if (it.isEmpty()) View.VISIBLE else View.INVISIBLE

                adapter.isLoadingEnabled = false
                adapter.set(it)
            }
        }
    }

    companion object {
        fun newInstance() = SentConnectionsController()
    }
}
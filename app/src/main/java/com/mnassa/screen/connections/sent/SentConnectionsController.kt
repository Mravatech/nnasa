package com.mnassa.screen.connections.sent

import android.support.v7.widget.LinearLayoutManager
import android.view.View
import org.kodein.di.generic.instance
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_connections_sent.view.*
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

        adapter.onCancelClickListener = { viewModel.cancelRequest(it) }

        with(view) {
            toolbar.title = fromDictionary(R.string.sent_connection_requests_title)
            rvSentConnections.layoutManager = LinearLayoutManager(context)
            rvSentConnections.adapter = adapter
        }

        adapter.isLoadingEnabled = true
        launchCoroutineUI {
            viewModel.sentConnectionsChannel.consumeEach {
                view.rlEmptyView.visibility = if (it.isEmpty()) View.VISIBLE else View.INVISIBLE
                view.rvSentConnections.visibility = if (it.isNotEmpty()) View.VISIBLE else View.INVISIBLE

                adapter.isLoadingEnabled = false
                adapter.set(it)
            }
        }
    }

    override fun onDestroyView(view: View) {
        adapter.destoryCallbacks()
        super.onDestroyView(view)
    }

    companion object {
        fun newInstance() = SentConnectionsController()
    }
}
package com.mnassa.screen.connections

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_connections.view.*
import kotlinx.android.synthetic.main.header_login.view.*
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by Peter on 3/6/2018.
 */
class ConnectionsController : MnassaControllerImpl<ConnectionsViewModel>() {
    override val layoutId: Int = R.layout.controller_connections
    override val viewModel: ConnectionsViewModel by instance()
    private val allConnectionsAdapter = AllConnectionsRecyclerViewAdapter()
    private val recommendedConnectionsAdapter = RecommendedConnectionsRecyclerViewAdapter()
    private val requestedConnectionsAdapter = RequestedConnectionsRecyclerViewAdapter()

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        with(view) {
            tvScreenHeader.text = fromDictionary(R.string.tab_connections_title)
            tvRequestedConnections.text = fromDictionary(R.string.tab_connections_requested)
            tvRecommendedConnections.text = fromDictionary(R.string.tab_connections_recommended)
            tvAllConnections.text = fromDictionary(R.string.tab_connections_all)

            rvAllConnections.adapter = allConnectionsAdapter
            rvRecommendedConnections.adapter = recommendedConnectionsAdapter
            rvRequestedConnections.adapter = requestedConnectionsAdapter

            rvAllConnections.layoutManager = BlockedScrollingLayoutManager(context, RecyclerView.VERTICAL, false)
            rvRecommendedConnections.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            rvRequestedConnections.layoutManager = BlockedScrollingLayoutManager(context, RecyclerView.VERTICAL, false)
        }

        launchCoroutineUI {
            viewModel.allConnectionsChannel.consumeEach {
                allConnectionsAdapter.set(it)
            }
        }

        launchCoroutineUI {
            viewModel.recommendedConnectionsChannel.consumeEach {
                recommendedConnectionsAdapter.set(it)
            }
        }

        launchCoroutineUI {
            viewModel.requestedConnectionsChannel.consumeEach {
                requestedConnectionsAdapter.set(it)
            }
        }

    }

    class BlockedScrollingLayoutManager(context: Context, orientation: Int,
                                        reverseLayout: Boolean) : LinearLayoutManager(context, orientation, reverseLayout) {
        override fun canScrollHorizontally(): Boolean = false
        override fun canScrollVertically(): Boolean = false
    }

    companion object {
        fun newInstance() = ConnectionsController()
    }
}
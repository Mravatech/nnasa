package com.mnassa.screen.connections

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.connections.adapters.AllConnectionsRecyclerViewAdapter
import com.mnassa.screen.connections.adapters.NewConnectionRequestsRecyclerViewAdapter
import com.mnassa.screen.connections.adapters.RecommendedConnectionsRecyclerViewAdapter
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_connections.view.*
import kotlinx.android.synthetic.main.header_main.view.*
import kotlinx.android.synthetic.main.red_badge.view.*
import kotlinx.coroutines.experimental.channels.consumeEach


/**
 * Created by Peter on 3/6/2018.
 */
class ConnectionsController : MnassaControllerImpl<ConnectionsViewModel>() {
    override val layoutId: Int = R.layout.controller_connections
    override val viewModel: ConnectionsViewModel by instance()
    private val allConnectionsAdapter = AllConnectionsRecyclerViewAdapter()
    private val recommendedConnectionsAdapter = RecommendedConnectionsRecyclerViewAdapter()
    private val newConnectionRequestsAdapter = NewConnectionRequestsRecyclerViewAdapter()

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        with(view) {
            tvScreenHeader.text = fromDictionary(R.string.tab_connections_title)
            tvNewConnectionRequests.text = fromDictionary(R.string.tab_connections_new_requests)
            tvRecommendedConnections.text = fromDictionary(R.string.tab_connections_recommended)
            tvAllConnections.text = fromDictionary(R.string.tab_connections_all)

            recommendedConnectionsAdapter.onShowAllClickListener = { openRecommendedConnectionsScreen() }
            recommendedConnectionsAdapter.onConnectClickListener = { viewModel.connect(it) }

            newConnectionRequestsAdapter.onApplyClickListener = { viewModel.apply(it) }
            newConnectionRequestsAdapter.onDeclineClickListener = { viewModel.decline(it) }
            newConnectionRequestsAdapter.onShowAllClickListener = { openNewRequestsScreen() }

            rvAllConnections.adapter = allConnectionsAdapter
            rvRecommendedConnections.adapter = recommendedConnectionsAdapter
            rvNewConnectionRequests.adapter = newConnectionRequestsAdapter

            rvAllConnections.layoutManager = BlockedScrollingLayoutManager(context, RecyclerView.VERTICAL, false)
            rvRecommendedConnections.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            rvNewConnectionRequests.layoutManager = BlockedScrollingLayoutManager(context, RecyclerView.VERTICAL, false)

            ivMore.setOnClickListener {
                //Creating the instance of PopupMenu
                val popup = PopupMenu(it.context, it)
                //Inflating the Popup using xml file
                popup.menuInflater.inflate(R.menu.connections_main, popup.menu)
                popup.menu.findItem(R.id.action_recommended_connections).title = fromDictionary(R.string.tab_connections_recommended)
                popup.menu.findItem(R.id.action_sent_requests).title = fromDictionary(R.string.tab_connections_new_requests)
                popup.menu.findItem(R.id.action_archived).title = fromDictionary(R.string.tab_connections_rejected)

                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.action_recommended_connections -> openRecommendedConnectionsScreen()
                        R.id.action_sent_requests -> openSendRequestConnectionsScreen()
                        R.id.action_archived -> openArchivedConnectionsScreen()
                    }
                    true
                }

                popup.show()
            }
        }

        launchCoroutineUI {
//            viewModel.allConnectionsChannel.consumeEach {
            viewModel.recommendedConnectionsChannel.consumeEach {
                allConnectionsAdapter.set(it)
                view.tvAllConnections.text = formatTextWithCounter(R.string.tab_connections_all, it.size)
            }
        }

        launchCoroutineUI {
            viewModel.recommendedConnectionsChannel.consumeEach {
                recommendedConnectionsAdapter.set(it)
                view.tvRecommendedConnections.text = formatTextWithCounter(R.string.tab_connections_recommended, it.size)

            }
        }

        launchCoroutineUI {
//            viewModel.requestedConnectionsChannel.consumeEach {
            viewModel.recommendedConnectionsChannel.consumeEach {
                newConnectionRequestsAdapter.set(it)
                view.tvNewConnectionRequests.visibility = if (it.isEmpty()) View.GONE else View.VISIBLE
                view.rvNewConnectionRequests.visibility = if (it.isEmpty()) View.GONE else View.VISIBLE
                view.flBadgeRoot.visibility = if (it.isEmpty()) View.GONE else View.VISIBLE
                view.tvBadgeCount.text = it.size.toString()
                view.tvNewConnectionRequests.text = formatTextWithCounter(R.string.tab_connections_new_requests, it.size)
            }
        }
    }

    ///////////////////////////////////////// CONNECTION TYPE SCREENS ///////////////////////////////

    private fun openRecommendedConnectionsScreen() {

    }

    private fun openSendRequestConnectionsScreen() {

    }

    private fun openArchivedConnectionsScreen() {

    }

    private fun openNewRequestsScreen() {

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private fun formatTextWithCounter(dictionaryResId: Int, counterValue: Int): CharSequence {
        val head = fromDictionary(dictionaryResId) + " "
        val spannable = SpannableString(head + counterValue.toString())
        val color = ContextCompat.getColor(requireNotNull(applicationContext), R.color.coolGray)
        spannable.setSpan(ForegroundColorSpan(color), head.length, spannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannable
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
package com.mnassa.screen.connections.recommended

import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_connections_recommended.view.*
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by Peter on 09.03.2018.
 */
class RecommendedConnectionsController : MnassaControllerImpl<RecommendedConnectionsViewModel>() {
    override val layoutId: Int = R.layout.controller_connections_recommended
    override val viewModel: RecommendedConnectionsViewModel by instance()
    private val adapter = GroupedRecommendedConnectionsRVAdapter()

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        adapter.onConnectClickListener = { viewModel.connect(it) }

        with(view) {
            toolbar.title = fromDictionary(R.string.recommended_connections_title)

            rvRecommendedConnections.layoutManager = LinearLayoutManager(context)
            rvRecommendedConnections.adapter = adapter
        }

        adapter.isLoadingEnabled = true
        launchCoroutineUI {
            viewModel.recommendedConnectionsChannel.consumeEach {
                adapter.isLoadingEnabled = false
                adapter.set(it)
                view.rlEmptyView.visibility = if (it.isEmpty) View.VISIBLE else View.INVISIBLE
            }
        }
    }

    override fun onDestroyView(view: View) {
        adapter.destroyCallbacks()
        super.onDestroyView(view)
    }

    companion object {
        fun newInstance() = RecommendedConnectionsController()
    }
}
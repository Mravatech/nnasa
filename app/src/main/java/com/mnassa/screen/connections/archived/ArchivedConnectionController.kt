package com.mnassa.screen.connections.archived

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_archived.view.*
import kotlinx.coroutines.channels.consumeEach
import org.kodein.di.generic.instance

/**
 * Created by Peter on 9.03.2018.
 */
class ArchivedConnectionController : MnassaControllerImpl<ArchivedConnectionViewModel>() {
    override val layoutId: Int = R.layout.controller_archived
    override val viewModel: ArchivedConnectionViewModel by instance()
    private val adapter = ArchivedConnectionsRVAdapter()

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        adapter.onConnectClickListener = { viewModel.connect(it) }

        with(view) {
            toolbar.title = fromDictionary(R.string.archived_connections_title)
            tvEmptyTitle.text = fromDictionary(R.string.archived_connections_empty_title)
            tvEmptyDescription.text = fromDictionary(R.string.archived_connections_empty_description)

            rvArchivedConnection.layoutManager = LinearLayoutManager(context)
            rvArchivedConnection.adapter = adapter
        }

        adapter.isLoadingEnabled = true
        launchCoroutineUI {
            adapter.disconnectTimeoutDays = viewModel.getDisconnectTimeoutDays()

            viewModel.declinedConnectionsChannel.consumeEach {
                adapter.isLoadingEnabled = false
                adapter.set(it)
                view.rlEmptyView.visibility = if (it.isEmpty()) View.VISIBLE else View.INVISIBLE
                view.rvArchivedConnection.visibility = if (it.isNotEmpty()) View.VISIBLE else View.INVISIBLE
            }
        }
    }

    override fun onDestroyView(view: View) {
        adapter.destroyCallbacks()
        view.rvArchivedConnection.adapter = null
        super.onDestroyView(view)
    }

    companion object {
        fun newInstance() = ArchivedConnectionController()
    }
}
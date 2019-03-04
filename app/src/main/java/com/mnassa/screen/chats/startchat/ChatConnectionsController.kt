package com.mnassa.screen.chats.startchat

import android.view.View
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.extensions.SimpleTextWatcher
import com.mnassa.extensions.isInvisible
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.chats.message.ChatMessageController
import com.mnassa.screen.connections.adapters.AllConnectionsRecyclerViewAdapter
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_connections_all.view.*
import kotlinx.android.synthetic.main.search_view.view.*
import kotlinx.coroutines.channels.consumeEach
import org.kodein.di.generic.instance

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 5/2/2018
 */
class ChatConnectionsController : MnassaControllerImpl<ChatConnectionsViewModel>() {
    override val layoutId: Int = R.layout.controller_connections_all
    override val viewModel: ChatConnectionsViewModel by instance()

    private val allConnectionsAdapter = AllConnectionsRecyclerViewAdapter()
    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        with(view) {
            toolbar.title = fromDictionary(R.string.tab_connections_all)
            rvAllConnections.adapter = allConnectionsAdapter
            searchView.etSearch.addTextChangedListener(SimpleTextWatcher {
                allConnectionsAdapter.searchByName(it)
            })
        }

        allConnectionsAdapter.onItemClickListener = {
            open(ChatMessageController.newInstance(it))
        }
        allConnectionsAdapter.isLoadingEnabled = true
        launchCoroutineUI {
            viewModel.allConnectionsChannel.consumeEach {
                allConnectionsAdapter.isLoadingEnabled = false
                allConnectionsAdapter.set(it)

                view.rlEmptyView.isInvisible = it.isNotEmpty()
                view.rvAllConnections.isInvisible = it.isEmpty()
            }
        }
    }

    companion object {
        fun newInstance() = ChatConnectionsController()
    }

}
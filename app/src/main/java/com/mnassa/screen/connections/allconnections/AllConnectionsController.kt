package com.mnassa.screen.connections.allconnections

import android.view.View
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.extensions.SimpleTextWatcher
import com.mnassa.extensions.isInvisible
import com.mnassa.helper.PopupMenuHelper
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.chats.message.ChatMessageController
import com.mnassa.screen.connections.adapters.AllConnectionsRecyclerViewAdapter
import com.mnassa.screen.profile.ProfileController
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_connections_all.view.*
import kotlinx.android.synthetic.main.search_view.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import org.kodein.di.generic.instance

/**
 * Created by Peter on 3/14/2018.
 */
class AllConnectionsController : MnassaControllerImpl<AllConnectionsViewModel>() {
    override val layoutId: Int = R.layout.controller_connections_all
    override val viewModel: AllConnectionsViewModel by instance()
    private val popupMenuHelper: PopupMenuHelper by instance()

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

        allConnectionsAdapter.onItemOptionsClickListener = { item, view -> onMoreConnectedAccountFunctions(item, view) }
        allConnectionsAdapter.onItemClickListener = { open(ProfileController.newInstance(it)) }

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

    override fun onDestroyView(view: View) {
        allConnectionsAdapter.destroyCallbacks()
        view.rvAllConnections.adapter = null
        super.onDestroyView(view)
    }

    private fun openChat(accountModel: ShortAccountModel) {
        open(ChatMessageController.newInstance(accountModel))
    }

    private fun openProfile(accountModel: ShortAccountModel) {
        open(ProfileController.newInstance(accountModel))
    }

    private fun onMoreConnectedAccountFunctions(accountModel: ShortAccountModel, sender: View) {
        popupMenuHelper.showConnectedAccountMenu(
                view = sender,
                onChat = { openChat(accountModel) },
                onProfile = { openProfile(accountModel) },
                onDisconnect = { viewModel.disconnect(accountModel) })
    }

    companion object {
        fun newInstance() = AllConnectionsController()
    }
}
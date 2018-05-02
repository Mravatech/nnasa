package com.mnassa.screen.chats.startchat

import android.view.View
import com.bluelinelabs.conductor.Controller
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.connections.adapters.AllConnectionsRecyclerViewAdapter
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_connections_all.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
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
    private val resultListener by lazy { targetController as ChatConnectionsResult }
    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        require(targetController is ChatConnectionsResult) {
            "$targetController must implement ${ChatConnectionsResult::class.java.name}"
        }

        with(view) {
            toolbar.title = fromDictionary(R.string.tab_connections_all)
            rvAllConnections.adapter = allConnectionsAdapter
        }

        allConnectionsAdapter.onItemClickListener = {
            resultListener.onChatChosen(it)
            close()
        }
        allConnectionsAdapter.isLoadingEnabled = true
        launchCoroutineUI {
            viewModel.allConnectionsChannel.consumeEach {
                allConnectionsAdapter.isLoadingEnabled = false
                allConnectionsAdapter.set(it)

                view.rlEmptyView.visibility = if (it.isEmpty()) View.VISIBLE else View.INVISIBLE
                view.rvAllConnections.visibility = if (it.isNotEmpty()) View.VISIBLE else View.INVISIBLE
            }
        }
    }

    interface ChatConnectionsResult {
        fun onChatChosen(accountModel: ShortAccountModel)
    }

    companion object {
        fun <T> newInstance(listener: T): ChatConnectionsController where T : ChatConnectionsResult, T : Controller {
            val controller = ChatConnectionsController()
            controller.targetController = listener
            return controller
        }
    }

}
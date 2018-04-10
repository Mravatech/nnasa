package com.mnassa.screen.connections.allconnections

import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PopupMenu
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Toast
import com.github.salomonbrys.kodein.instance
import com.mnassa.App
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.domain.model.formattedName
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.chats.message.ChatMessageController
import com.mnassa.screen.connections.adapters.AllConnectionsRecyclerViewAdapter
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_connections_all.view.*
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by Peter on 3/14/2018.
 */
class AllConnectionsController : MnassaControllerImpl<AllConnectionsViewModel>() {
    override val layoutId: Int = R.layout.controller_connections_all
    override val viewModel: AllConnectionsViewModel by instance()

    private val allConnectionsAdapter = AllConnectionsRecyclerViewAdapter()

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)

        with(view) {
            toolbar.title = fromDictionary(R.string.tab_connections_all)


            rvAllConnections.layoutManager = LinearLayoutManager(context)
            rvAllConnections.adapter = allConnectionsAdapter
        }

        allConnectionsAdapter.onItemClickListener = { item, view -> onMoreConnectedAccountFunctions(item, view) }

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

    private fun openChat(accountModel: ShortAccountModel) {
        open(ChatMessageController.newInstance(accountModel))
    }

    private fun openProfile(accountModel: ShortAccountModel) {
        Toast.makeText(App.context, "Opening profile of ${accountModel.formattedName}", Toast.LENGTH_SHORT).show()

    }


    private fun onMoreConnectedAccountFunctions(accountModel: ShortAccountModel, sender: View) {
        //Creating the instance of PopupMenu
        val popup = PopupMenu(sender.context, sender)
        //Inflating the Popup using xml file
        popup.menuInflater.inflate(R.menu.connections_item, popup.menu)
        popup.menu.findItem(R.id.action_connections_send_message).title = fromDictionary(R.string.tab_connections_all_item_send_message)
        popup.menu.findItem(R.id.action_connections_view_profile).title = fromDictionary(R.string.tab_connections_all_item_view_profile)

        val disconnectSpan = SpannableString(fromDictionary(R.string.tab_connections_all_item_disconnect))
        val disconnectTextColor = ContextCompat.getColor(App.context, R.color.red)
        disconnectSpan.setSpan(ForegroundColorSpan(disconnectTextColor), 0, disconnectSpan.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        popup.menu.findItem(R.id.action_connections_disconnect).title = disconnectSpan

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_connections_send_message -> openChat(accountModel)
                R.id.action_connections_view_profile -> openProfile(accountModel)
                R.id.action_connections_disconnect -> viewModel.disconnect(accountModel)
            }
            true
        }

        popup.show()
    }

    companion object {
        fun newInstance() = AllConnectionsController()
    }
}
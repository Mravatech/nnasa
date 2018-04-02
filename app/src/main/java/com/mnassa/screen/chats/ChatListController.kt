package com.mnassa.screen.chats

import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.chats.message.ChatMessageController
import com.mnassa.screen.connections.allconnections.AllConnectionsController
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_chat_list.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import timber.log.Timber

/**
 * Created by Peter on 3/6/2018.
 */
class ChatListController : MnassaControllerImpl<ChatListViewModel>() {
    override val layoutId: Int = R.layout.controller_chat_list
    override val viewModel: ChatListViewModel by instance()

    val adapter = ChatListAdapter()

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        view.fabAddChat.setOnClickListener { open(AllConnectionsController.newInstance()) }
        view.rvMessages.layoutManager = LinearLayoutManager(view.context)
        view.rvMessages.addItemDecoration(ChatRoomItemDecoration(ContextCompat.getDrawable(view.context, R.drawable.chat_decorator)!!))
        view.rvMessages.adapter = adapter
        adapter.onItemClickListener = { open(ChatMessageController.newInstance(requireNotNull(requireNotNull(it.chatMessageModel).account), it.id)) }
        view.tvNoConversation.text = fromDictionary(R.string.chats_no_conversation)
        launchCoroutineUI {
            viewModel.getMessagesChannel().consumeEach {
                Timber.i(it.item.toString())
                when (it) {
                    is ListItemEvent.Added -> {
                        adapter.isLoadingEnabled = false
                        adapter.dataStorage.add(it.item)
                    }
                    is ListItemEvent.Changed -> adapter.dataStorage.add(it.item)
                    is ListItemEvent.Moved -> adapter.dataStorage.add(it.item)
                    is ListItemEvent.Removed -> adapter.dataStorage.remove(it.item)
                }
                if (view.llEmptyMessages.visibility == View.VISIBLE) {
                    view.llEmptyMessages.visibility = View.GONE
                }
            }
        }

    }

    companion object {
        fun newInstance() = ChatListController()
    }
}
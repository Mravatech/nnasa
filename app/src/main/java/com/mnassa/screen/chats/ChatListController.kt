package com.mnassa.screen.chats

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.bluelinelabs.conductor.RouterTransaction
import com.mnassa.R
import com.mnassa.activity.SearchActivity
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.ChatRoomModel
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.chats.message.ChatMessageController
import com.mnassa.screen.chats.startchat.ChatConnectionsController
import com.mnassa.screen.main.OnPageSelected
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_chat_list.view.*
import kotlinx.android.synthetic.main.header_main.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import org.kodein.di.generic.instance

/**
 * Created by Peter on 3/6/2018.
 */
class ChatListController : MnassaControllerImpl<ChatListViewModel>(), ChatConnectionsController.ChatConnectionsResult, OnPageSelected {

    override val layoutId: Int = R.layout.controller_chat_list
    override val viewModel: ChatListViewModel by instance()

    val adapter = ChatListAdapter()

    override fun onCreated(savedInstanceState: Bundle?) {
        super.onCreated(savedInstanceState)

        savedInstanceState?.apply {
            adapter.restoreState(this)
        }
        adapter.isLoadingEnabled = savedInstanceState == null
        controllerSubscriptionContainer.launchCoroutineUI {
            val view = getViewSuspend()
            viewModel.listMessagesChannel.consumeEach {
                when (it) {
                    is ListItemEvent.Added -> {
                        adapter.isLoadingEnabled = false
                        adapter.dataStorage.add(it.item)
                        view.llEmptyMessages.visibility = View.GONE
                    }
                    is ListItemEvent.Changed -> adapter.dataStorage.add(it.item)
                    is ListItemEvent.Moved -> adapter.dataStorage.add(it.item)
                    is ListItemEvent.Removed -> adapter.dataStorage.remove(it.item)
                    is ListItemEvent.Cleared -> {
                        adapter.dataStorage.clear()
                        adapter.isLoadingEnabled = false
                        view.llEmptyMessages.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        with(view) {
            fabAddChat.setOnClickListener {
                router.pushController(RouterTransaction.with(ChatConnectionsController.newInstance(this@ChatListController)))
            }
            rvMessages.layoutManager = LinearLayoutManager(view.context)
            rvMessages.addItemDecoration(ChatRoomItemDecoration(ContextCompat.getDrawable(view.context, R.drawable.chat_decorator)!!))
            rvMessages.adapter = adapter
            toolbar.title = fromDictionary(R.string.chats_title)
            toolbar.title = fromDictionary(R.string.chats_title)

            tvNoConversation.text = fromDictionary(R.string.chats_no_conversation)
            toolbar.onMoreClickListener = {
                startActivityForResult(SearchActivity.start(context, adapter.dataStorage.toList(), SearchActivity.CHAT_TYPE), REQUEST_CODE_SEARCH)
            }
            toolbar.ivToolbarMore.setImageResource(R.drawable.ic_search)
        }
        adapter.onItemClickListener = { open(ChatMessageController.newInstance(requireNotNull(it.account))) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_CODE_SEARCH) return
        when (resultCode) {
            SearchActivity.CHAT_RESULT -> {
                val item = data?.getSerializableExtra(SearchActivity.EXTRA_ITEM_TO_OPEN_SCREEN_RESULT) as ChatRoomModel
                open(ChatMessageController.newInstance(requireNotNull(item.account)))
            }
        }
    }

    override fun onPageSelected() {
        view?.rvMessages?.scrollToPosition(0)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        adapter.saveState(outState)
    }

    override fun onChatChosen(accountModel: ShortAccountModel) {
        open(ChatMessageController.newInstance(accountModel))
    }

    companion object {
        private const val REQUEST_CODE_SEARCH = 999
        fun newInstance() = ChatListController()
    }
}
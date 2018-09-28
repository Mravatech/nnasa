package com.mnassa.screen.chats

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.mnassa.R
import com.mnassa.activity.SearchActivity
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.ChatRoomModel
import com.mnassa.extensions.isInvisible
import com.mnassa.extensions.subscribeToUpdates
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.chats.message.ChatMessageController
import com.mnassa.screen.chats.startchat.ChatConnectionsController
import com.mnassa.screen.main.OnPageSelected
import com.mnassa.screen.main.OnScrollToTop
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_chat_list.view.*
import kotlinx.android.synthetic.main.header_main.view.*
import org.kodein.di.generic.instance

/**
 * Created by Peter on 3/6/2018.
 */
class ChatListController : MnassaControllerImpl<ChatListViewModel>(), OnPageSelected, OnScrollToTop {

    override val layoutId: Int = R.layout.controller_chat_list
    override val viewModel: ChatListViewModel by instance()
    private val adapter = ChatListAdapter()

    override fun onCreated(savedInstanceState: Bundle?) {
        super.onCreated(savedInstanceState)

        savedInstanceState?.apply {
            adapter.restoreState(this)
        }
        adapter.isLoadingEnabled = savedInstanceState == null
        adapter.onDataChangedListener = { itemsCount ->
            view?.llEmptyMessages?.isInvisible = itemsCount > 0 || adapter.isLoadingEnabled
        }

        controllerSubscriptionContainer.launchCoroutineUI {
            viewModel.listMessagesChannel.subscribeToUpdates(
                    adapter = adapter,
                    emptyView = { getViewSuspend().llEmptyMessages }
            )
        }
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        with(view) {
            fabAddChat.setOnClickListener {
                open(ChatConnectionsController.newInstance())
            }
            rvMessages.layoutManager = LinearLayoutManager(view.context)
            rvMessages.addItemDecoration(ChatRoomItemDecoration(ContextCompat.getDrawable(view.context, R.drawable.chat_decorator)!!))
            rvMessages.adapter = adapter
            toolbar.title = fromDictionary(R.string.chats_title)
            toolbar.title = fromDictionary(R.string.chats_title)

            tvNoConversation.text = fromDictionary(R.string.chats_no_conversation)
            toolbar.onMoreClickListener = {
                startActivityForResult(SearchActivity.start(context, adapter.dataStorage.toList(), SearchActivity.CHAT_TYPE), SearchActivity.REQUEST_CODE_SEARCH)
            }
            toolbar.ivToolbarMore.setImageResource(R.drawable.ic_search)
        }
        adapter.onItemClickListener = { open(ChatMessageController.newInstance(requireNotNull(it.account))) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != SearchActivity.REQUEST_CODE_SEARCH) return
        when (resultCode) {
            SearchActivity.CHAT_RESULT -> {
                val item = data?.getSerializableExtra(SearchActivity.EXTRA_ITEM_TO_OPEN_SCREEN_RESULT) as ChatRoomModel
                open(ChatMessageController.newInstance(requireNotNull(item.account)))
            }
        }
    }

    override fun onPageSelected() {
        //do nothing here
    }

    override fun scrollToTop() {
        view?.rvMessages?.scrollToPosition(0)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        adapter.saveState(outState)
    }

    companion object {
        fun newInstance() = ChatListController()
    }
}
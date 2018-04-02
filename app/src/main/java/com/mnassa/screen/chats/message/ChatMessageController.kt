package com.mnassa.screen.chats.message

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.Toast
import com.github.salomonbrys.kodein.instance
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_chat_message.view.*
import kotlinx.android.synthetic.main.header_main.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import timber.log.Timber

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/2/2018
 */

class ChatMessageController(data: Bundle) : MnassaControllerImpl<ChatMessageViewModel>(data) {
    override val layoutId: Int = R.layout.controller_chat_message
    override val viewModel: ChatMessageViewModel by instance()
    private val accountModel: ShortAccountModel by lazy { args.getSerializable(CHAT_ACCOUNT) as ShortAccountModel }
    private val chatId: String by lazy { args.getString(CHAT_ID) }

    val adapter = MessagesAdapter(accountModel.id)

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        view.toolbarChatMessage.title = accountModel.userName
        view.toolbarChatMessage.ivToolbarMore.setImageResource(R.drawable.ic_info)
        view.toolbarChatMessage.onMoreClickListener = { Toast.makeText(view.context,"Set profile after merge",Toast.LENGTH_SHORT).show() }
        view.rvMessages.layoutManager = LinearLayoutManager(view.context)
        view.rvMessages.adapter = adapter
        view.tvNoMessages.text = fromDictionary(R.string.chats_no_messages)
        view.btnSend.text = fromDictionary(R.string.chats_send_messages_post)
        view.etWriteMessage.hint = fromDictionary(R.string.chats_send_messages_write)
        launchCoroutineUI {
            viewModel.getMessageChannel(chatId).consumeEach {
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
                if (view.llNoMessages.visibility == View.VISIBLE) {
                    view.llNoMessages.visibility = View.GONE
                }
            }
        }
    }

    companion object {
        private const val CHAT_ACCOUNT = "CHAT_ACCOUNT"
        private const val CHAT_ID = "CHAT_ID"

        fun newInstance(ac: ShortAccountModel, chatId: String): ChatMessageController {
            val params = Bundle()
            params.putSerializable(CHAT_ACCOUNT, ac)
            params.putString(CHAT_ID, chatId)
            return ChatMessageController(params)
        }
    }

}
package com.mnassa.screen.chats.message

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.Toast
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.ChatMessageModel
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.PostModel
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.helper.DialogHelper
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.posts.need.details.PostDetailsController
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_chat_message.view.*
import kotlinx.android.synthetic.main.header_main.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import org.kodein.di.generic.instance
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
    private val postModel: PostModel? by lazy { args.getSerializable(CHAT_POST) as PostModel? }
    private val dialog: DialogHelper by instance()

    val adapter = MessagesAdapter(accountModel.id)
    private var replyMessageModel: ChatMessageModel? = null
    private var replyPostModel: PostModel? = null

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        setupView(view)
        setupOnClickListeners(view)
        view.toolbarChatMessage.title = accountModel.userName
        view.toolbarChatMessage.ivToolbarMore.setImageResource(R.drawable.ic_info)
        view.toolbarChatMessage.onMoreClickListener = { Toast.makeText(view.context, "Set profile after merge", Toast.LENGTH_SHORT).show() }
        view.rvMessages.layoutManager = LinearLayoutManager(view.context)
        view.rvMessages.adapter = adapter
        viewModel.retrieveChatId(accountModel.id)
        postModel?.let {
            view.rlReplyMessageContainer.visibility = View.VISIBLE
            view.tvReplyMessageText.text = it.text
            replyPostModel = postModel
        }
        launchCoroutineUI {
            viewModel.messageChannel.consumeEach {
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
                    viewModel.resetChatUnreadCount()
                }
                view.rvMessages.scrollToPosition(adapter.itemCount)//todo handle here doesn't work properly
            }
        }
        adapter.onUserMessageLongClick = { callDialog(view, false, it) }
        adapter.onMyMessageLongClick = { callDialog(view, true, it) }
        adapter.onReplyClick = { chatMessageModel, post ->
            if (post != null) {
                open(PostDetailsController.newInstance(post))
            } else {
                val position = adapter.dataStorage.indexOf(chatMessageModel)
                view.rvMessages.scrollToPosition(position)
            }
        }
    }

    private fun callDialog(view: View, isMyMessageClicked: Boolean, item: ChatMessageModel) {
        dialog.showDeleteMessageDialog(
                context = view.context,
                isMyMessageClicked = isMyMessageClicked,
                onDeleteForMeClick = { viewModel.deleteMessage(item, false) },
                onDeleteForBothClick = { viewModel.deleteMessage(item, true) },
                onCopyClick = {
                    val clipboard = view.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("message text", item.text)
                    clipboard.primaryClip = clip
                },
                onReplyClick = {
                    view.rlReplyMessageContainer.visibility = View.VISIBLE
                    view.tvReplyMessageText.text = item.text
                    replyMessageModel = item
                    replyPostModel = null
                })
    }

    private fun setupOnClickListeners(view: View) {
        view.ivReplyClose.setOnClickListener {
            view.rlReplyMessageContainer.visibility = View.GONE
            view.tvReplyMessageText.text = null
            replyMessageModel = null
            replyPostModel = null
        }
        view.btnSend.setOnClickListener {
            val text = view.etWriteMessage.text.toString().trim()
            if (text.isNotBlank()) {
                viewModel.sendMessage(text, MessagesAdapter.TEXT_TYPE, replyMessageModel?.id, replyPostModel?.id)
                view.etWriteMessage.text = null
                view.ivReplyClose.callOnClick()
            }
        }
    }

    private fun setupView(view: View) {
        view.tvNoMessages.text = fromDictionary(R.string.chats_no_messages)
        view.btnSend.text = fromDictionary(R.string.chats_send_messages_post)
        view.etWriteMessage.hint = fromDictionary(R.string.chats_send_messages_write)
    }

    companion object {
        private const val CHAT_ACCOUNT = "CHAT_ACCOUNT"
        private const val CHAT_POST = "CHAT_POST"

        fun newInstance(account: ShortAccountModel): ChatMessageController {
            val params = Bundle()
            params.putSerializable(CHAT_ACCOUNT, account)
            return ChatMessageController(params)
        }

        fun newInstance(post: PostModel, account: ShortAccountModel): ChatMessageController {
            val params = Bundle()
            params.putSerializable(CHAT_POST, post)
            params.putSerializable(CHAT_ACCOUNT, account)
            return ChatMessageController(params)
        }
    }

}
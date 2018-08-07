package com.mnassa.screen.chats.message

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.mnassa.R
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.model.*
import com.mnassa.extensions.isInvisible
import com.mnassa.helper.DialogHelper
import com.mnassa.screen.base.MnassaControllerImpl
import com.mnassa.screen.posts.PostDetailsFactory
import com.mnassa.screen.profile.ProfileController
import com.mnassa.translation.fromDictionary
import kotlinx.android.synthetic.main.controller_chat_message.view.*
import kotlinx.android.synthetic.main.header_main.view.*
import kotlinx.coroutines.experimental.channels.consumeEach
import org.kodein.di.generic.instance

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/2/2018
 */

class ChatMessageController(data: Bundle) : MnassaControllerImpl<ChatMessageViewModel>(data) {
    override val layoutId: Int = R.layout.controller_chat_message
    private val accountModel by lazy { args.getSerializable(CHAT_ACCOUNT) as ShortAccountModel? }
    override val viewModel: ChatMessageViewModel by instance(arg = accountModel?.id)
    private val postModel: PostModel? by lazy { args.getSerializable(CHAT_POST) as PostModel? }
    private val dialog: DialogHelper by instance()

    private val adapter = MessagesAdapter()
    private var replyMessageModel: ChatMessageModel? = null
    private var replyPostModel: PostModel? = null

    override fun onCreated(savedInstanceState: Bundle?) {
        super.onCreated(savedInstanceState)

        adapter.isLoadingEnabled = savedInstanceState == null
        controllerSubscriptionContainer.launchCoroutineUI {
            viewModel.messageChannel.consumeEach {
                when (it) {
                    is ListItemEvent.Added, is ListItemEvent.Changed, is ListItemEvent.Moved -> {
                        adapter.isLoadingEnabled = false
                        adapter.dataStorage.add(it.item)
                    }
                    is ListItemEvent.Removed -> {
                        adapter.dataStorage.remove(it.item)
                    }
                    is ListItemEvent.Cleared -> {
                        adapter.dataStorage.clear()
                        adapter.isLoadingEnabled = true
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        with(view) {
            setupView(this)
            setupOnClickListeners(this)
            toolbarChatMessage.title = accountModel?.formattedName ?: fromDictionary(R.string.support_chat_with)
            toolbarChatMessage.ivToolbarMore.setImageResource(R.drawable.ic_info)
            toolbarChatMessage.onMoreClickListener = { accountModel?.let { open(ProfileController.newInstance(it)) } }
            rvMessages.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true)
            rvMessages.adapter = adapter
        }
        postModel?.let {
            view.rlReplyMessageContainer.visibility = View.VISIBLE
            view.tvReplyMessageText.text = it.text
            replyPostModel = postModel
        }
        adapter.accountId = viewModel.currentUserAccountId
        adapter.onUserMessageLongClick = { callDialog(view, false, it) }
        adapter.onMyMessageLongClick = { callDialog(view, true, it) }
        adapter.onReplyClick = { chatMessageModel, post ->
            if (post != null) {
                val postDetailsFactory: PostDetailsFactory by instance()
                open(postDetailsFactory.newInstance(post))
            } else {
                val position = adapter.dataStorage.indexOf(chatMessageModel)
                if (position >= 0)
                    view.rvMessages.scrollToPosition(position + 1)
            }
        }

        view.postDelayed({
            adapter.isLoadingEnabled = false
            view?.llNoMessages?.isInvisible = !adapter.dataStorage.isEmpty()
        }, WAIT_FOR_LOADING_DELAY)

        adapter.onDataChangedListener = {
            view.llNoMessages.isInvisible = !adapter.dataStorage.isEmpty()
            view.rvMessages.scrollToPosition(0)
        }
    }

    override fun onDestroyView(view: View) {
        adapter.destroyCallbacks()
        view.rvMessages.adapter = null
        super.onDestroyView(view)
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
                launchCoroutineUI {
                    if (viewModel.sendMessage(text, MessagesAdapter.TEXT_TYPE, replyMessageModel, replyPostModel)) {
                        view.etWriteMessage.text = null
                        view.ivReplyClose.callOnClick()
                    }
                }
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
        private const val WAIT_FOR_LOADING_DELAY = 4000L

        fun newInstance(): ChatMessageController {
            val params = Bundle()
            return ChatMessageController(params)
        }

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
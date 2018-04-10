package com.mnassa.screen.chats.message

import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.interactor.ChatInteractor
import com.mnassa.domain.model.ChatMessageModel
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.channels.consumeEach
import timber.log.Timber

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/2/2018
 */
class ChatMessageViewModelImpl(private val chatInteractor: ChatInteractor) : MnassaViewModelImpl(), ChatMessageViewModel {

    override val messageChannel: BroadcastChannel<ListItemEvent<ChatMessageModel>> = BroadcastChannel(10)

    private lateinit var chatID: String

    override fun retrieveChatId(accointId: String) {
        handleException {
            showProgress()
            val chatId = chatInteractor.getChatIdByUserId(accointId)
            chatID = chatId
            hideProgress()
            chatInteractor.listOfMessages(chatId, accointId).consumeEach {
                messageChannel.send(it)
            }
        }
    }

    override fun resetChatUnreadCount() {
        launchCoroutineUI {
            try {
                chatInteractor.resetChatUnreadCount(chatID)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    override fun sendMessage(text: String, type: String, linkedMessageId: String?, linkedPostId: String?) {
        handleException {
            chatInteractor.sendMessage(
                    chatID = chatID,
                    text = text,
                    type = type,
                    linkedMessageId = linkedMessageId,
                    linkedPostId = linkedPostId)
        }
    }

    override fun deleteMessage(item: ChatMessageModel, isDeleteForBothMessages: Boolean) {
        handleException {
            chatInteractor.deleteMessage(item.id, chatID, isDeleteForBothMessages)
        }
    }
}
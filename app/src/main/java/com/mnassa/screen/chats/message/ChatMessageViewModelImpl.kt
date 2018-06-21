package com.mnassa.screen.chats.message

import android.os.Bundle
import com.mnassa.core.addons.asyncUI
import com.mnassa.core.addons.consumeTo
import com.mnassa.core.addons.launchCoroutineUI
import com.mnassa.domain.interactor.ChatInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.ChatMessageModel
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.PostModel
import com.mnassa.domain.model.impl.ChatMessageModelImpl
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.BroadcastChannel
import java.util.*

class ChatMessageViewModelImpl(
        private val userAccountId: String?, //if null - this is chat with admin
        private val chatInteractor: ChatInteractor,
        private val userProfileInteractor: UserProfileInteractor) : MnassaViewModelImpl(), ChatMessageViewModel {

    override val messageChannel: BroadcastChannel<ListItemEvent<ChatMessageModel>> = BroadcastChannel(10)
    override val currentUserAccountId: String get() = userProfileInteractor.getAccountIdOrException()

    private val chatId = asyncUI { chatInteractor.getChatIdByUserId(userAccountId) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleException {
            chatInteractor.listOfMessages(chatId.await(), userAccountId).consumeTo(messageChannel)
        }
    }

    override fun resetChatUnreadCount() {
        launchCoroutineUI {
            chatInteractor.resetChatUnreadCount(chatId.await())
        }
    }

    override fun sendMessage(text: String, type: String, linkedMessage: ChatMessageModel?, linkedPost: PostModel?) {
        handleException {
            chatInteractor.sendMessage(
                    chatID = chatId.await(),
                    text = text,
                    type = type,
                    linkedMessageId = linkedMessage?.id,
                    linkedPostId = linkedPost?.id)
        }

//        handleException {
//            messageChannel.send(ListItemEvent.Added(
//                    ChatMessageModelImpl(
//                            id = "TEST",
//                            createdAt = Date(),
//                            creator = currentUserAccountId,
//                            text = text,
//                            replyMessage =
//                            )
//            ))
//        }
    }

    override fun deleteMessage(item: ChatMessageModel, isDeleteForBothMessages: Boolean) {
        handleException {
            chatInteractor.deleteMessage(item.id, chatId.await(), isDeleteForBothMessages)
        }
    }
}
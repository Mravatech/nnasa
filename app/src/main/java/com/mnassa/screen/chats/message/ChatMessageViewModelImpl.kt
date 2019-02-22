package com.mnassa.screen.chats.message

import android.os.Bundle
import com.mnassa.core.addons.asyncUI
import com.mnassa.domain.interactor.ChatInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.ChatMessageModel
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.PostModel
import com.mnassa.exceptions.resolveExceptions
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.consumeEach

class ChatMessageViewModelImpl(
        private val params: ChatMessageViewModel.Params, //if null - this is chat with admin
        private val chatInteractor: ChatInteractor,
        private val userProfileInteractor: UserProfileInteractor) : MnassaViewModelImpl(), ChatMessageViewModel {

    override val messageChannel: BroadcastChannel<ListItemEvent<List<ChatMessageModel>>> = BroadcastChannel(10)
    override val currentUserAccountId: String get() = userProfileInteractor.getAccountIdOrException()

    private val chatId = GlobalScope.asyncUI {
        params.chatId
            ?: handleExceptionsSuspend {
                withContext(Dispatchers.Default) {
                    chatInteractor.getChatIdByUserId(params.accountId)
                }
            }
            ?: "UNDEFINED"
    }
    private var resetCounterJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        resolveExceptions {
            chatInteractor.loadMessagesWithChangesHandling(chatId.await()).consumeEach {
                messageChannel.send(it)
                resetChatUnreadCount()
            }
        }
    }

    private fun resetChatUnreadCount() {
        resetCounterJob?.cancel()
        resetCounterJob = resolveExceptions(showErrorMessage = false) {
            delay(1_000)
            chatInteractor.resetChatUnreadCount(chatId.await())
        }
    }

    override suspend fun sendMessage(text: String, type: String, linkedMessage: ChatMessageModel?, linkedPost: PostModel?) =
            handleExceptionsSuspend {
                withProgressSuspend {
                    chatInteractor.sendMessage(
                            chatID = chatId.await(),
                            text = text,
                            type = type,
                            linkedMessageId = linkedMessage?.id,
                            linkedPostId = linkedPost?.id)
                }
                true
            } ?: false

    override fun deleteMessage(item: ChatMessageModel, isDeleteForBothMessages: Boolean) {
        GlobalScope.resolveExceptions {
            chatInteractor.deleteMessage(item.id, chatId.await(), isDeleteForBothMessages)
        }
    }
}
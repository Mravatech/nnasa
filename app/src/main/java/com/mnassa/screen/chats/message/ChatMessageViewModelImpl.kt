package com.mnassa.screen.chats.message

import com.mnassa.core.addons.asyncWorker
import com.mnassa.core.addons.launchWorker
import com.mnassa.core.addons.launchWorkerNoExceptions
import com.mnassa.domain.interactor.ChatInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.ChatMessageModel
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.model.PostModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.consumeEach
import kotlin.Exception

class ChatMessageViewModelImpl(
    private val params: ChatMessageViewModel.Params, //if null - this is chat with admin
    private val chatInteractor: ChatInteractor,
    private val userProfileInteractor: UserProfileInteractor
) : MnassaViewModelImpl(), ChatMessageViewModel {

    override val clearInputChannel: BroadcastChannel<Unit> = BroadcastChannel(1)
    override val messageChannel: BroadcastChannel<ListItemEvent<List<ChatMessageModel>>> = BroadcastChannel(10)
    override val currentUserAccountId: String get() = userProfileInteractor.getAccountIdOrException()

    private var deferredChatId: Deferred<String>? = null

    private suspend fun getChatId(): String {
        return params.chatId
        // Reuse a task
            ?: deferredChatId
                ?.let {
                    // Catch an exception to recreate the
                    // task below.
                    try {
                        it.await()
                    } catch (e: Exception) {
                        null
                    }
                }
            // Start a new task
            ?: asyncWorker {
                chatInteractor.getChatIdByUserId(params.accountId)
            }.let { deferred ->
                deferredChatId = deferred
                return@let deferred.await()
            }
    }

    override fun onSetup(setupScope: CoroutineScope) {
        super.onSetup(setupScope)
        setupScope.launchWorker {
            var firstMessage = true
            val chatId = getChatId()
            chatInteractor.loadMessagesWithChangesHandling(chatId).consumeEach {
                messageChannel.send(it)

                if (firstMessage) {
                    firstMessage = false
                    resetChatUnreadCount()
                }
            }
        }
    }

    private fun resetChatUnreadCount() {
        launchWorkerNoExceptions {
            val chatId = getChatId()
            chatInteractor.resetChatUnreadCount(chatId)
        }
    }

    override fun sendMessage(
        text: String,
        type: String,
        linkedMessage: ChatMessageModel?,
        linkedPost: PostModel?
    ) {
        launchWorker {
            withProgressSuspend(hideKeyboard = false) {
                val chatId = getChatId()
                chatInteractor.sendMessage(
                    chatID = chatId,
                    text = text,
                    type = type,
                    linkedMessageId = linkedMessage?.id,
                    linkedPostId = linkedPost?.id
                )
            }
        }.invokeOnCompletion {
            // Clear input field on success.
            if (it == null) launch {
                clearInputChannel.send(Unit)
            }
        }
    }

    override fun deleteMessage(item: ChatMessageModel, isDeleteForBothMessages: Boolean) {
        launchWorker {
            withProgressSuspend {
                val chatId = getChatId()
                chatInteractor.deleteMessage(item.id, chatId, isDeleteForBothMessages)
            }
        }
    }
}
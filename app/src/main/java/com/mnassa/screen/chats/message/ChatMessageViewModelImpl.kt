package com.mnassa.screen.chats.message

import com.mnassa.domain.interactor.ChatInteractor
import com.mnassa.domain.model.ChatMessageModel
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/2/2018
 */
class ChatMessageViewModelImpl(private val chatInteractor: ChatInteractor) : MnassaViewModelImpl(), ChatMessageViewModel {
    override suspend fun getMessageChannel(chatId: String): ReceiveChannel<ListItemEvent<ChatMessageModel>> =
            chatInteractor.listOfMessages(chatId)
}
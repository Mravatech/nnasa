package com.mnassa.domain.interactor.impl

import com.mnassa.domain.interactor.ChatInteractor
import com.mnassa.domain.model.ChatRoomModel
import com.mnassa.domain.model.ListItemEvent
import com.mnassa.domain.repository.ChatRepository
import com.mnassa.domain.repository.UserRepository
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/2/2018
 */
class ChatInteractorImpl(private val chatRepository: ChatRepository, private val userRepository: UserRepository) : ChatInteractor {

    override suspend fun listOfChats(): ReceiveChannel<ListItemEvent<ChatRoomModel>> =
            chatRepository.listOfChats()

}
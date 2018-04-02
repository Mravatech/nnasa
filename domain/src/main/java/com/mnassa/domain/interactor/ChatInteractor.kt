package com.mnassa.domain.interactor

import com.mnassa.domain.model.ChatRoomModel
import com.mnassa.domain.model.ListItemEvent
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 4/2/2018
 */
interface ChatInteractor {

    suspend fun listOfChats(): ReceiveChannel<ListItemEvent<ChatRoomModel>>
}
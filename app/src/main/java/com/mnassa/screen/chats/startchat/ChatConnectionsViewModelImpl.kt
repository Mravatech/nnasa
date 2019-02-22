package com.mnassa.screen.chats.startchat

import android.os.Bundle
import com.mnassa.core.addons.consumeTo
import com.mnassa.domain.interactor.ConnectionsInteractor
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.exceptions.resolveExceptions
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.channels.ConflatedBroadcastChannel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 5/2/2018
 */
class ChatConnectionsViewModelImpl(private val connectionsInteractor: ConnectionsInteractor) : MnassaViewModelImpl(),  ChatConnectionsViewModel {

    override val allConnectionsChannel: ConflatedBroadcastChannel<List<ShortAccountModel>> = ConflatedBroadcastChannel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        resolveExceptions {
            connectionsInteractor.getConnectedConnections().consumeTo(allConnectionsChannel)
        }
    }
}
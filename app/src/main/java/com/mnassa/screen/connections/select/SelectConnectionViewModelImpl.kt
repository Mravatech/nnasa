package com.mnassa.screen.connections.select

import android.os.Bundle
import com.mnassa.core.addons.consumeTo
import com.mnassa.domain.interactor.ConnectionsInteractor
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.ConflatedBroadcastChannel

/**
 * Created by Peter on 4/2/2018.
 */
class SelectConnectionViewModelImpl(private val connectionsInteractor: ConnectionsInteractor) : MnassaViewModelImpl(), SelectConnectionViewModel {

    override val allConnectionsChannel: ConflatedBroadcastChannel<List<ShortAccountModel>> = ConflatedBroadcastChannel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleException {
            connectionsInteractor.getConnectedConnections().consumeTo(allConnectionsChannel)
        }
    }
}
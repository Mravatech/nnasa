package com.mnassa.screen.posts.need.sharing

import android.os.Bundle
import com.mnassa.domain.interactor.ConnectionsInteractor
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.channels.ConflatedChannel
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by Peter on 3/21/2018.
 */
class SharingOptionsViewModelImpl(private val connectionsInteractor: ConnectionsInteractor) : MnassaViewModelImpl(), SharingOptionsViewModel {
    override val allConnections: ConflatedChannel<List<ShortAccountModel>> = ConflatedChannel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleException {
            connectionsInteractor.getConnectedConnections().consumeEach {
                allConnections.send(it)
            }
        }
    }

}
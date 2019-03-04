package com.mnassa.screen.posts.need.sharing

import android.os.Bundle
import com.mnassa.domain.interactor.ConnectionsInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.exceptions.resolveExceptions
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach

/**
 * Created by Peter on 3/21/2018.
 */
class SharingOptionsViewModelImpl(
        private val params: SharingOptionsViewModel.SharingOptionsParams,
        private val connectionsInteractor: ConnectionsInteractor,
        private val userProfileInteractor: UserProfileInteractor) : MnassaViewModelImpl(), SharingOptionsViewModel {

    override val allConnections: Channel<List<ShortAccountModel>> = Channel(Channel.CONFLATED)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        resolveExceptions {
            connectionsInteractor.getConnectedConnections().consumeEach {
                val currentUser = userProfileInteractor.getAccountIdOrNull()
                allConnections.send(it.filter { !params.excludedAccounts.contains(it.id) && it.id != currentUser })
            }
        }
    }
}
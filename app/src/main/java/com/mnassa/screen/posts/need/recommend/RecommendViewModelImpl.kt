package com.mnassa.screen.posts.need.recommend

import android.os.Bundle
import com.mnassa.domain.interactor.ConnectionsInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.exceptions.resolveExceptions
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.consumeEach

/**
 * Created by Peter on 3/27/2018.
 */
class RecommendViewModelImpl(
        private val params: RecommendViewModel.RecommendViewModelParams,
        private val connectionsInteractor: ConnectionsInteractor,
        private val userInteractor: UserProfileInteractor) : MnassaViewModelImpl(), RecommendViewModel {

    override val connectionsChannel: ConflatedBroadcastChannel<List<ShortAccountModel>> = ConflatedBroadcastChannel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        resolveExceptions {
            val excludedAccounts = HashSet(params.excludedAccounts)
            excludedAccounts.add(userInteractor.getAccountIdOrException())
            excludedAccounts.add(userInteractor.getValueCenterId())
            excludedAccounts.add(userInteractor.getAdminId())

            connectionsInteractor.getConnectedConnections().consumeEach {
                val result = it.filterNot { excludedAccounts.contains(it.id) }
                connectionsChannel.send(result)
            }
        }
    }
}
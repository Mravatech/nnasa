package com.mnassa.screen.posts.need.recommend

import com.mnassa.core.addons.launchWorker
import com.mnassa.domain.interactor.ConnectionsInteractor
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.domain.model.ShortAccountModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.CoroutineScope
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

    override fun onSetup(setupScope: CoroutineScope) {
        super.onSetup(setupScope)
        setupScope.launchWorker {
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
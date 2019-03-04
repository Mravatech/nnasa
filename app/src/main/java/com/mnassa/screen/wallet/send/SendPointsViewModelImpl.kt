package com.mnassa.screen.wallet.send

import com.mnassa.core.addons.asyncWorker
import com.mnassa.domain.interactor.GroupsInteractor
import com.mnassa.domain.interactor.WalletInteractor
import com.mnassa.domain.model.TransactionSideModel
import com.mnassa.exceptions.resolveExceptions
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BroadcastChannel

/**
 * Created by Peter on 4/2/2018.
 */
class SendPointsViewModelImpl(private val walletInteractor: WalletInteractor,
                              private val groupsInteractor: GroupsInteractor) : MnassaViewModelImpl(), SendPointsViewModel {
    override val resultListenerChannel: BroadcastChannel<SendPointsViewModel.SendPointsResult> = BroadcastChannel(1)
    private val hasAnyGroup = GlobalScope.asyncWorker { handleExceptionsSuspend { groupsInteractor.hasAnyGroup() } ?: false }

    override suspend fun hasAnyGroup(): Boolean = hasAnyGroup.await()

    override fun sendPoints(amount: Long, sender: TransactionSideModel, recipient: TransactionSideModel, description: String?) {
        resolveExceptions {
            withProgressSuspend {
                walletInteractor.sendPoints(amount = amount, sender = sender, recipient = recipient, description = description)
                resultListenerChannel.send(SendPointsViewModel.SendPointsResult(
                        amount = amount,
                        sender = sender,
                        recipient = recipient,
                        description = description
                ))
            }
        }
    }
}
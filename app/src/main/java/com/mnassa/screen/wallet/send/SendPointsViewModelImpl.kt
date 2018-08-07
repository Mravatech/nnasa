package com.mnassa.screen.wallet.send

import com.mnassa.domain.interactor.GroupsInteractor
import com.mnassa.domain.interactor.WalletInteractor
import com.mnassa.domain.model.TransactionSideModel
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.ArrayBroadcastChannel

/**
 * Created by Peter on 4/2/2018.
 */
class SendPointsViewModelImpl(private val walletInteractor: WalletInteractor,
                              private val groupsInteractor: GroupsInteractor) : MnassaViewModelImpl(), SendPointsViewModel {
    override val resultListenerChannel: ArrayBroadcastChannel<SendPointsViewModel.SendPointsResult> = ArrayBroadcastChannel(1)
    private val hasAnyGroup = async { handleExceptionsSuspend { groupsInteractor.hasAnyGroup() } ?: false }

    override suspend fun hasAnyGroup(): Boolean = hasAnyGroup.await()

    override fun sendPoints(amount: Long, sender: TransactionSideModel, recipient: TransactionSideModel, description: String?) {
        handleException {
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
package com.mnassa.domain.extensions

import com.mnassa.core.events.awaitFirst
import com.mnassa.domain.interactor.UserProfileInteractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.isActive

suspend fun UserProfileInteractor.produceAccountChangedEvents(): ReceiveChannel<Unit> =
    GlobalScope.produce(Dispatchers.Unconfined) {
        while (isActive) {
            onAccountIdChangedListener.awaitFirst()
            send(Unit)
        }
    }

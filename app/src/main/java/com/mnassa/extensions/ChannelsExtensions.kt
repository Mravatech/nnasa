package com.mnassa.extensions

import com.github.salomonbrys.kodein.instance
import com.mnassa.core.events.awaitFirst
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Created by Peter on 4/4/2018.
 */
abstract class ReConsumableBroadcastChannel<T>(
        outputChannelInstanceCreator: () -> BroadcastChannel<T>,
        private val beforeReConsume: suspend (outputChannel: BroadcastChannel<T>) -> Unit = {},
        private val reConsumableEvent: suspend (viewModel: MnassaViewModelImpl) -> Unit,
        private val onEachEvent: suspend (event: T) -> Unit = {},
        private val receiveChannelProvider: suspend () -> ReceiveChannel<T>) : ReadOnlyProperty<MnassaViewModelImpl, BroadcastChannel<T>> {

    private val outputChannel = outputChannelInstanceCreator()
    private var previousConsumeJob: Job? = null
    private var previousWaitForReConsumeJob: Job? = null
    private var previousReceiveChannel: ReceiveChannel<T>? = null

    override fun getValue(thisRef: MnassaViewModelImpl, property: KProperty<*>): BroadcastChannel<T> {
        previousWaitForReConsumeJob?.cancel()
        previousWaitForReConsumeJob = thisRef.handleException {
            while (true) {
                reConsumableEvent(thisRef)
                reConsume(thisRef)
            }
        }
        return reConsume(thisRef)
    }

    private fun reConsume(thisRef: MnassaViewModelImpl): BroadcastChannel<T> {
        previousConsumeJob?.cancel()
        previousReceiveChannel?.cancel()

        previousConsumeJob = thisRef.handleException {
            beforeReConsume(outputChannel)
            val inputChannel = receiveChannelProvider()
            previousReceiveChannel = inputChannel
            inputChannel.consumeEach {
                outputChannel.send(it)
                onEachEvent(it)
            }
        }
        return outputChannel
    }
}

class ReConsumeWhenAccountChangedArrayBroadcastChannel<T>(beforeReConsume: suspend (outputChannel: BroadcastChannel<T>) -> Unit = { },
                                                          onEachEvent: suspend (event: T) -> Unit = {},
                                                          receiveChannelProvider: suspend () -> ReceiveChannel<T>) : ReConsumableBroadcastChannel<T>(
        outputChannelInstanceCreator = { ArrayBroadcastChannel(100) },
        beforeReConsume = beforeReConsume,
        onEachEvent = onEachEvent,
        receiveChannelProvider = receiveChannelProvider,
        reConsumableEvent = { viewModel ->
            val userProfileInteractor: UserProfileInteractor by viewModel.instance()
            userProfileInteractor.onAccountChangedListener.awaitFirst()
        }
) {
    override fun getValue(thisRef: MnassaViewModelImpl, property: KProperty<*>): ArrayBroadcastChannel<T> {
        return super.getValue(thisRef, property) as ArrayBroadcastChannel
    }
}

class ReConsumeWhenAccountChangedConflatedBroadcastChannel<T>(beforeReConsume: suspend (outputChannel: BroadcastChannel<T>) -> Unit = { },
                                                              onEachEvent: suspend (event: T) -> Unit = {},
                                                              receiveChannelProvider: suspend () -> ReceiveChannel<T>) : ReConsumableBroadcastChannel<T>(
        outputChannelInstanceCreator = { ConflatedBroadcastChannel() },
        beforeReConsume = beforeReConsume,
        onEachEvent = onEachEvent,
        receiveChannelProvider = receiveChannelProvider,
        reConsumableEvent = { viewModel ->
            val userProfileInteractor: UserProfileInteractor by viewModel.instance()
            userProfileInteractor.onAccountChangedListener.awaitFirst()
        }
) {
    override fun getValue(thisRef: MnassaViewModelImpl, property: KProperty<*>): ConflatedBroadcastChannel<T> {
        return super.getValue(thisRef, property) as ConflatedBroadcastChannel
    }
}


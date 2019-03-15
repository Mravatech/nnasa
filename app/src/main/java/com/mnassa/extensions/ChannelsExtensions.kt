package com.mnassa.extensions

import com.mnassa.core.events.awaitFirst
import com.mnassa.domain.interactor.UserProfileInteractor
import com.mnassa.exceptions.resolveExceptions
import com.mnassa.screen.base.MnassaViewModelImpl
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.yield
import org.kodein.di.generic.instance
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Created by Peter on 4/4/2018.
 */

/**
 * Channel delegate, which wraps the input channel and emits each input event from it to the
 * output channel.
 * Also, when [reConsumableEvent] happens, input channel will be cancelled and the output channel
 * will be subscribed to the NEW input channel which must be provided by [receiveChannelProvider].
 * !!! Always returns a new channel !!!
 */
abstract class ReConsumableBroadcastChannel<T>(
        outputChannelInstanceCreator: () -> BroadcastChannel<T>,
        private val beforeReConsume: suspend (outputChannel: BroadcastChannel<T>) -> Unit = {},
        private val reConsumableEvent: suspend (viewModel: MnassaViewModelImpl) -> Unit,
        private val onEachEvent: suspend (event: T) -> Unit = {},
        private val receiveChannelProvider: suspend () -> ReceiveChannel<T>,
        private val invokeReConsumeFirstly: Boolean = false) : ReadOnlyProperty<MnassaViewModelImpl, BroadcastChannel<T>> {

    private val outputChannel = outputChannelInstanceCreator()
    private var previousConsumeJob: Job? = null
    private var previousWaitForReConsumeJob: Job? = null
    private var previousReceiveChannel: ReceiveChannel<T>? = null

    override fun getValue(thisRef: MnassaViewModelImpl, property: KProperty<*>): BroadcastChannel<T> {
        previousWaitForReConsumeJob?.cancel()
        previousWaitForReConsumeJob = thisRef.resolveExceptions {
            while (true) {
                yield()
                reConsumableEvent(thisRef)
                reConsume(thisRef)
            }
        }
        return reConsume(thisRef)
    }

    private fun reConsume(thisRef: MnassaViewModelImpl): BroadcastChannel<T> {
        previousConsumeJob?.cancel()
        previousReceiveChannel?.cancel()

        val wasConsumedBefore = previousConsumeJob != null

        previousConsumeJob = thisRef.resolveExceptions {
            if (wasConsumedBefore || invokeReConsumeFirstly) beforeReConsume(outputChannel)
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

class ProcessAccountChangeArrayBroadcastChannel<T>(beforeReConsume: suspend (outputChannel: BroadcastChannel<T>) -> Unit = { },
                                                   invokeReConsumeFirstly: Boolean = false,
                                                   onEachEvent: suspend (event: T) -> Unit = {},
                                                   receiveChannelProvider: suspend () -> ReceiveChannel<T>) : ReConsumableBroadcastChannel<T>(
        outputChannelInstanceCreator = { BroadcastChannel(100) },
        beforeReConsume = beforeReConsume,
        onEachEvent = onEachEvent,
        receiveChannelProvider = receiveChannelProvider,
        reConsumableEvent = { viewModel ->
            val userProfileInteractor: UserProfileInteractor by viewModel.instance()
            userProfileInteractor.onAccountIdChangedListener.awaitFirst()
        },
        invokeReConsumeFirstly = invokeReConsumeFirstly
)

class ProcessAccountChangeConflatedBroadcastChannel<T>(beforeReConsume: suspend (outputChannel: BroadcastChannel<T>) -> Unit = { },
                                                       invokeReConsumeFirstly: Boolean = false,
                                                       onEachEvent: suspend (event: T) -> Unit = {},
                                                       receiveChannelProvider: suspend () -> ReceiveChannel<T>) : ReConsumableBroadcastChannel<T>(
        outputChannelInstanceCreator = { ConflatedBroadcastChannel() },
        beforeReConsume = beforeReConsume,
        onEachEvent = onEachEvent,
        receiveChannelProvider = receiveChannelProvider,
        reConsumableEvent = { viewModel ->
            val userProfileInteractor: UserProfileInteractor by viewModel.instance()
            userProfileInteractor.onAccountIdChangedListener.awaitFirst()
        },
        invokeReConsumeFirstly = invokeReConsumeFirstly
) {
    override fun getValue(thisRef: MnassaViewModelImpl, property: KProperty<*>): ConflatedBroadcastChannel<T> {
        return super.getValue(thisRef, property) as ConflatedBroadcastChannel
    }
}

suspend fun <T> BroadcastChannel<T>.consumeOne(): T {
    return consume { receive() }
}

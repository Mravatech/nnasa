package com.mnassa.core.addons

import kotlinx.coroutines.experimental.channels.BroadcastChannel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.SendChannel
import kotlinx.coroutines.experimental.channels.consumeEach

/**
 * Created by Peter on 4/4/2018.
 */
interface ConsumableChannel<E> {
}

suspend inline fun <E> ConsumableChannel<E>.consumeEach(action: (E) -> Unit) {
    when (this) {
        is ReceiveConsumableChanel -> input.consumeEach(action)
        is BroadcastConsumableChanel -> input.consumeEach(action)
    }
}

class ReceiveConsumableChanel<E>(val input: ReceiveChannel<E>) : ConsumableChannel<E>
class BroadcastConsumableChanel<E>(val input: BroadcastChannel<E>) : ConsumableChannel<E>

suspend fun <T, R> ReceiveChannel<T>.consumeTo(sendChannel: SendChannel<R>, mapper: (T) -> R) = consumeEach { sendChannel.send(mapper(it)) }
suspend fun <T> ReceiveChannel<T>.consumeTo(sendChannel: SendChannel<T>) = consumeEach { sendChannel.send(it) }
suspend fun <T, R> BroadcastChannel<T>.consumeTo(sendChannel: SendChannel<R>, mapper: (T) -> R) = consumeEach { sendChannel.send(mapper(it)) }
suspend fun <T, R> ConsumableChannel<T>.consumeTo(sendChannel: SendChannel<R>, mapper: (T) -> R) = consumeEach { sendChannel.send(mapper(it)) }
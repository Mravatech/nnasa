package com.mnassa.domain.aggregator

import com.mnassa.domain.live.Live
import com.mnassa.domain.live.consume
import com.mnassa.domain.extensions.toCoroutineScope
import com.mnassa.domain.model.HasId
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.lang.Exception
import kotlin.coroutines.coroutineContext

/**
 * @author Artem Chepurnoy
 */
class AggregatorLive<T : HasId>(
    private val source: suspend () -> ReceiveChannel<AggregatorInEvent<out T>>,
    private val reconsume: suspend () -> ReceiveChannel<Unit>,
    /**
     * Defines the order of items.
     */
    comparator: Comparator<T>,
    /**
     * Defines whether to show this model or
     * hide it.
     */
    isValid: (T) -> Boolean = { true }
) : Live<AggregatorObserver<T>>() {

    private var job: Job? = null

    /**
     * This thing the does all the job of joining events
     * into a list.
     */
    private val aggregator = Aggregator(comparator, isValid)

    private val actor = GlobalScope.actor<AggregatorInEvent<out T>> {
        aggregator.observe { state ->
            forEachObserver { observer ->
                observer.invoke(state)
            }
        }

        // Handle the events
        consumeEach { event ->
            when (event) {
                is AggregatorInEvent.Init -> {
                    val models = event.events.map { it.model }
                    aggregator.put(models, clearBefore = true)
                }
                is AggregatorInEvent.Clear -> aggregator.clear()
                is AggregatorInEvent.Revalidate -> aggregator.revalidate()
                is AggregatorInEvent.Put -> aggregator.put(event.model)
                is AggregatorInEvent.Remove -> aggregator.remove(event.id)
            }
        }
    }

    override fun onObserverAdded(observer: AggregatorObserver<T>) {
        super.onObserverAdded(observer)

        if (aggregator.models.isNotEmpty()) {
            val state = aggregator.getState(AggregatorOutEvent.Reset)
            observer.invoke(state)
        }
    }

    override fun onActive() {
        job?.cancel()
        job = GlobalScope.launch {
            fun createConsumerJob() = launch {
                // Consume the events from source channel and send
                // then to an actor.
                try {
                    coroutineScope {
                        source.invoke().consumeEach { actor.send(it) }
                    }
                } catch (_: Exception) {
                }
            }

            var consumer = createConsumerJob()
            reconsume.invoke().consumeEach {
                // Clear the old data
                actor.send(AggregatorInEvent.Clear())

                // Recreate the consumer
                consumer.cancel()
                consumer = createConsumerJob()
            }
        }
    }

    override fun onInactive() {
        job?.cancel()
    }

    /**
     * Sens the [AggregatorInEvent.Revalidate] event to
     * an aggregator.
     */
    suspend fun revalidate() = actor.send(AggregatorInEvent.Revalidate())

}

suspend fun <T : HasId> AggregatorLive<T>.produce(): Channel<AggregatorOutState<T>> {
    val output = Channel<AggregatorOutState<T>>()
    val producers = coroutineContext.toCoroutineScope().launch {
        consume {
            launch {
                output.send(it)
            }
        }
    }

    output.invokeOnClose {
        producers.cancel()
    }

    return output
}
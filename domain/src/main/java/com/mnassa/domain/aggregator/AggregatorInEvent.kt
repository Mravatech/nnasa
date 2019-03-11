package com.mnassa.domain.aggregator

import com.mnassa.domain.model.HasId

/**
 * @author Artem Chepurnoy
 */
sealed class AggregatorInEvent<T : HasId> {
    /**
     * An event that should clear all previously set models and
     * put all of these events.
     */
    data class Init<T : HasId>(
        val events: List<AggregatorInEvent.Put<out T>>
    ) : AggregatorInEvent<T>()

    class Clear<T : HasId> : AggregatorInEvent<T>()

    class Revalidate<T : HasId> : AggregatorInEvent<T>()

    data class Put<T : HasId>(
        val model: T
    ) : AggregatorInEvent<T>()

    data class Remove<T : HasId>(
        val id: String
    ) : AggregatorInEvent<T>()
}

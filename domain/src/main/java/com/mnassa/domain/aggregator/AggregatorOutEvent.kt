package com.mnassa.domain.aggregator

import com.mnassa.domain.model.HasId

/**
 * @author Artem Chepurnoy
 */
sealed class AggregatorOutEvent<T : HasId> {
    /**
     * Some of the models have changed.
     *
     * You should notify that the data
     * has changed.
     */
    object Reset : AggregatorOutEvent<HasId>()

    /**
     * All models has been cleared.
     */
    object Clear : AggregatorOutEvent<HasId>()

    /**
     * Some of the [modelsAllDeltaCount models][Aggregator.modelsAll]
     * have changed.
     */
    object Hidden : AggregatorOutEvent<HasId>()

    data class Add<T : HasId>(
        val model: T,
        val i: Int
    ) : AggregatorOutEvent<T>()

    data class Set<T : HasId>(
        val model: T,
        val i: Int
    ) : AggregatorOutEvent<T>()

    data class Remove<T : HasId>(
        val model: T,
        val i: Int
    ) : AggregatorOutEvent<T>()
}

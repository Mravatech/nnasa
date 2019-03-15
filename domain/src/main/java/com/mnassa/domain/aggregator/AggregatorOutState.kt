package com.mnassa.domain.aggregator

import com.mnassa.domain.model.HasId

/**
 * @author Artem Chepurnoy
 */
data class AggregatorOutState<T : HasId>(
    val models: List<T>,
    val modelsAllDeltaCount: Int,
    val event: AggregatorOutEvent<in T>
)

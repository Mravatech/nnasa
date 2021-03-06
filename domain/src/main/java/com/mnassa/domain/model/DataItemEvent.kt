package com.mnassa.domain.model

import java.io.Serializable


/**
 * Created by Peter on 2/22/2018.
 */
sealed class DataItemEvent<out T : Model>(val value: T) : Serializable {
    class DataAddedItemEvent<out T : Model>(value: T) : DataItemEvent<T>(value)
    class DataChangedItemEvent<out T : Model>(value: T, val previousChildName: String) : DataItemEvent<T>(value)
    class DataRemovedItemEvent<out T : Model>(value: T) : DataItemEvent<T>(value)
    class DataMovedItemEvent<out T : Model>(value: T, val previousChildName: String) : DataItemEvent<T>(value)
}
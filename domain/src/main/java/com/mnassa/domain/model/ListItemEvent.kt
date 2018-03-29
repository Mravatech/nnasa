package com.mnassa.domain.model

sealed class ListItemEvent<out T: Any>(val item: T) {
    class Added<out T: Any>(item: T, previousChildName: String?) : ListItemEvent<T>(item)
    class Moved<out T: Any>(item: T, previousChildName: String?) : ListItemEvent<T>(item)
    class Changed<out T: Any>(item: T, previousChildName: String?) : ListItemEvent<T>(item)
    class Removed<out T: Any>(item: T) : ListItemEvent<T>(item)
}
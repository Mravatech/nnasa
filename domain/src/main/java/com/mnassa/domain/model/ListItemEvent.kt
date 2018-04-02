package com.mnassa.domain.model

sealed class ListItemEvent<T : Any>() {
    lateinit var item: T

    constructor(item: T) : this() {
        this.item = item
    }

    class Added<T : Any>(item: T, previousChildName: String? = null) : ListItemEvent<T>(item)
    class Moved<T : Any>(item: T, previousChildName: String? = null) : ListItemEvent<T>(item)
    class Changed<T : Any>(item: T, previousChildName: String? = null) : ListItemEvent<T>(item)
    class Removed<T : Any>(item: T) : ListItemEvent<T>(item)
    class Cleared<T : Any> : ListItemEvent<T>()

    override fun toString(): String {
        return "${super.toString()}; item: ${if (this::item.isInitialized) item else "_UNINITIALIZED_"}"
    }
}
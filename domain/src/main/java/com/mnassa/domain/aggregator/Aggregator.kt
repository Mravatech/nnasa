package com.mnassa.domain.aggregator

import com.mnassa.domain.model.HasId
import java.util.concurrent.CopyOnWriteArrayList

typealias AggregatorObserver<T> = (AggregatorOutState<T>) -> Unit

/**
 * @author Artem Chepurnoy
 */
class Aggregator<T : HasId>(
    /**
     * Defines the order of items.
     */
    private val comparator: Comparator<T>,
    /**
     * Defines whether to show this model or
     * hide it.
     */
    private val isValid: (T) -> Boolean = { true }
) {

    val modelsAll: ArrayList<T> = ArrayList()
    val models: ArrayList<T> = ArrayList()

    private var isSilent = false

    private val observers: MutableList<AggregatorObserver<T>> = CopyOnWriteArrayList()

    fun observe(listener: AggregatorObserver<T>) {
        observers.add(listener)
    }

    fun removeObserver(listener: AggregatorObserver<T>) {
        observers.remove(listener)
    }

    private fun tweet(event: AggregatorOutEvent<in T>) {
        if (isSilent) {
            return
        }

        val state = getState(event)
        observers.forEach { it(state) }
    }

    fun getState(event: AggregatorOutEvent<in T>): AggregatorOutState<T> {
        val hidden = modelsAll.size - models.size
        return AggregatorOutState(models, hidden, event)
    }

    fun put(model: T): T? {
        val i = indexOf(modelsAll, model)
        if (i >= 0) {
            // replace old model with a new one
            // in global list
            val old = modelsAll[i]
            if (old === model) {
                // updating list won't change anything
                return old
            }

            modelsAll[i] = model

            // update local list
            val validNew = isValid(model)
            val validOld = isValid(old)
            if (validNew && validOld) {
                // replace old model with a
                // new one
                val j = indexOf(models, old)
                if (j >= 0) {
                    // list may require re-sorting
                    val d = comparator.compare(model, old)
                    if (d == 0) {
                        // new model has the same position in
                        // list as the old one.
                        models[j] = model
                        tweet(AggregatorOutEvent.Set(model, j))
                    } else {
                        var a = 0
                        var b = models.size - 1

                        if (d < 0) {
                            if (j == a) {
                                // new model should be before old one, and
                                // the old one is first in list.
                                models[j] = model
                                tweet(AggregatorOutEvent.Set(model, j))
                                return old
                            } else {
                                b = Math.max(j - 1, a)
                            }
                        } else {
                            if (j == b) {
                                // new model should be after old one, and
                                // the old one is last in list.
                                models[j] = model
                                tweet(AggregatorOutEvent.Set(model, j))
                                return old
                            } else {
                                a = Math.min(j + 1, b)
                            }
                        }

                        var x = binarySearch(models, model, a, b)
                        if (x > j) {
                            x--
                        }

                        if (x == j) {
                            models[j] = model
                            tweet(AggregatorOutEvent.Set(model, j))
                        } else {
                            models.removeAt(j)
                            tweet(AggregatorOutEvent.Remove(model, j))

                            models.add(x, model)
                            tweet(AggregatorOutEvent.Add(model, j))
                        }
                    }
                } else {
                    throw IllegalStateException()
                }
            } else if (validNew) {
                // add a new model to the list
                val x = binarySearch(models, model)
                models.add(x, model)
                tweet(AggregatorOutEvent.Add(model, x))
            } else if (validOld) {
                // remove old model from the list
                val j = indexOf(models, old)
                if (j >= 0) {
                    models.removeAt(j)
                    tweet(AggregatorOutEvent.Remove(model, j))
                } else {
                    throw IllegalStateException()
                }
            } else {
                tweet(AggregatorOutEvent.Hidden)
            }

            return old
        } else {
            val j = modelsAll.size
            modelsAll.add(j, model)

            // update local list
            val validNew = isValid(model)
            if (validNew) {
                // add a new model to the local list
                val x = binarySearch(models, model)
                models.add(x, model)
                tweet(AggregatorOutEvent.Add(model, x))
            } else {
                tweet(AggregatorOutEvent.Hidden)
            }

            return null
        }
    }

    fun put(models: Collection<T>, clearBefore: Boolean = false) {
        withSilence {
            if (clearBefore) {
                clear()
            }

            models.forEach { put(it) }
        }

        // Notify the observers about
        // this change
        tweet(AggregatorOutEvent.Reset)
    }

    fun remove(model: T) {
        remove(model.id)
    }

    fun remove(key: String) {
        val i = indexOf(modelsAll, key)
        if (i >= 0) {
            val old = modelsAll[i]
            modelsAll.removeAt(i)

            // remove old model from the list
            val j = indexOf(models, old.id)
            if (j >= 0) {
                models.removeAt(j)
                tweet(AggregatorOutEvent.Remove(old, j))
            }
        }
    }

    fun clear() {
        models.clear()
        modelsAll.clear()
        tweet(AggregatorOutEvent.Clear)
    }

    /**
     * Re-validates each of the models and notifies
     * observers.
     *
     * Call this after [isValid] changes.
     */
    fun revalidate(reset: Boolean = false) {
        if (reset) {
            val list = ArrayList(modelsAll)
            withSilence {
                clear()
            }
            put(list)
            return
        }

        for (i in models.indices.reversed()) {
            val old = models[i]
            if (isValid(old).not()) {
                models.removeAt(i)
                tweet(AggregatorOutEvent.Remove(old, i))
            }
        }

        for (model in modelsAll) {
            val j = indexOf(models, model)
            if (j < 0 && isValid(model)) {
                // add a new model to the local list
                val x = binarySearch(models, model)
                models.add(x, model)
                tweet(AggregatorOutEvent.Add(model, x))
            }
        }
    }

    fun indexOf(list: List<T>, model: T) = indexOf(list, model.id)

    fun indexOf(list: List<T>, id: String) = list.indexOfFirst { it.id == id }

    private inline fun withSilence(crossinline block: () -> Unit) {
        isSilent = true
        block()
        isSilent = false
    }

    private fun binarySearch(list: List<T>, model: T): Int {
        return if (list.isEmpty()) 0 else binarySearch(list, model, 0, list.size - 1)
    }

    private fun binarySearch(list: List<T>, model: T, a: Int, b: Int): Int {
        if (a == b) {
            val old = list[a]
            val d = comparator.compare(model, old)
            return if (d > 0) a + 1 else a
        }

        val c = (a + b) / 2
        val old = list[c]
        val d = comparator.compare(model, old)
        return when {
            d == 0 -> c
            d < 0 -> binarySearch(list, model, a, Math.max(c - 1, 0))
            else -> binarySearch(list, model, Math.min(c + 1, b), b)
        }
    }

}
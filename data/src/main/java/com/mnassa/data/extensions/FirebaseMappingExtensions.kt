package com.mnassa.data.extensions

import com.google.firebase.database.DataSnapshot
import com.mnassa.domain.model.HasId

/**
 * Created by Peter on 2/26/2018.
 */
//////////////////////////////////////////// MAPPING ///////////////////////////////////////////////
internal inline fun <reified T : Any> mapSingleValue(dataSnapshot: DataSnapshot?): T? {
    if (dataSnapshot is T) return dataSnapshot

    return dataSnapshot?.run {
        val res = getValue(T::class.java)
        if (res is HasId) {
            res.id = key
        }
        res
    }
}

internal inline fun <reified T : Any> DataSnapshot?.mapSingle(): T? {
    return mapSingleValue(this)
}

internal inline fun <reified T : Any> mapListOfValues(dataSnapshot: DataSnapshot?): List<T> {
    if (dataSnapshot == null) return emptyList()
    return dataSnapshot.children.map { requireNotNull(it.mapSingle<T>()) }
}

internal inline fun <reified T : Any> DataSnapshot?.mapList(): List<T> {
    return mapListOfValues(this)
}

internal inline fun <reified T : Any> Iterable<DataSnapshot>.mapList(): List<T> {
    return mapNotNull { mapSingleValue<T>(it) }
}
package com.mnassa.data.extensions

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.gson.JsonElement
import timber.log.Timber

/**
 * Created by Peter on 2/26/2018.
 */
//////////////////////////////////////////// MAPPING ///////////////////////////////////////////////

internal inline fun <reified T : Any> mapSingleValue(dataSnapshot: DataSnapshot?): T? {
    if (dataSnapshot is T) return dataSnapshot //parse dataSnapshot manually
    if (dataSnapshot == null || dataSnapshot.value == null) return null

    val jsonElement: JsonElement = dataSnapshot.value.toJson<T>()
    forDebug { Timber.i("FIREBASE DATABASE >>> ${dataSnapshot.path} >>> $jsonElement") }

    return jsonElement.mapTo(dataSnapshot.key ?: "", dataSnapshot.path)
}

internal inline fun <reified T : Any> DataSnapshot?.mapSingle(): T? = mapSingleValue(this)

internal inline fun <reified T : Any> mapListOfValues(dataSnapshot: DataSnapshot?): List<T> {
    if (dataSnapshot == null) return emptyList()
    return dataSnapshot.children.map { requireNotNull(it.mapSingle<T>()) }
}

internal inline fun <reified T : Any> DataSnapshot?.mapList(): List<T> = mapListOfValues(this)

internal inline fun <reified T : Any> Iterable<DataSnapshot>.mapList(): List<T> = mapNotNull { mapSingleValue<T>(it) }

internal inline val DataSnapshot?.path: String get() = this?.ref?.path?.toString() ?: "[NULL]"
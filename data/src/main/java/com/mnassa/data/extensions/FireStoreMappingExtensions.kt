package com.mnassa.data.extensions

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.mnassa.domain.model.HasId
import timber.log.Timber

/**
 * Created by Peter on 4/13/2018.
 */
//////////////////////////////////////////// MAPPING ///////////////////////////////////////////////
private val gson = Gson()

internal inline fun <reified T> mapSingleValue(dataSnapshot: QueryDocumentSnapshot?): T? {
    if (dataSnapshot == null) return null
    if (dataSnapshot is T) return dataSnapshot //parse dataSnapshot manually

    val data = dataSnapshot.data
    val jsonElement: JsonElement = gson.toJsonTree(data)

    Timber.i("FIRESTORE >>> ${dataSnapshot.reference.path} >>> $jsonElement")

    val result = gson.fromJson(jsonElement, T::class.java)

    if (result is HasId) {
        result.id = dataSnapshot.id
    }
    return result
}

internal inline fun <reified T : Any> QueryDocumentSnapshot?.mapSingle(): T? = mapSingleValue(this)

internal inline fun <reified T> mapSingleValue(dataSnapshot: DocumentSnapshot?): T? {
    if (dataSnapshot == null) return null
    if (dataSnapshot is T) return dataSnapshot //parse dataSnapshot manually

    val data = dataSnapshot.data
    val jsonElement: JsonElement = gson.toJsonTree(data)

    Timber.i("FIRESTORE >>> ${dataSnapshot.reference.path} >>> $jsonElement")

    val result = gson.fromJson(jsonElement, T::class.java)

    if (result is HasId) {
        result.id = dataSnapshot.id
    }
    return result
}

internal inline fun <reified T : Any> DocumentSnapshot?.mapSingle(): T? = mapSingleValue(this)

internal inline fun <reified T> mapListValues(dataSnapshot: DocumentSnapshot?): List<T> {
    if (dataSnapshot == null) return emptyList()
    val resultList = ArrayList<T>()

    dataSnapshot.data?.entries?.forEachIndexed { index, entry ->
        val jsonElement: JsonElement = gson.toJsonTree(entry.value)

        Timber.i("FIRESTORE >>> ${dataSnapshot.reference.path} [$index] >>> ${entry.key} >>> $jsonElement")

        val result = gson.fromJson(jsonElement, T::class.java)
        if (result is HasId) {
            result.id = entry.key
        }
        resultList += result
    }

    return resultList
}

internal inline fun <reified T : Any> DocumentSnapshot?.mapList(): List<T> = mapListValues(this)
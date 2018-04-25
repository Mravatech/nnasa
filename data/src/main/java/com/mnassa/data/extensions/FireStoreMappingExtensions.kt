package com.mnassa.data.extensions

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.gson.JsonElement
import timber.log.Timber

/**
 * Created by Peter on 4/13/2018.
 */
//////////////////////////////////////////// MAPPING ///////////////////////////////////////////////

internal inline fun <reified T : Any> mapSingleValue(dataSnapshot: QueryDocumentSnapshot?): T? {
    if (dataSnapshot == null) return null
    if (dataSnapshot is T) return dataSnapshot //parse dataSnapshot manually

    val jsonElement: JsonElement = dataSnapshot.data.toJson<T>()
    Timber.i("FIRESTORE >>> ${dataSnapshot.reference.path} >>> $jsonElement")

    return jsonElement.mapTo(dataSnapshot.id, dataSnapshot.reference.path)
}

internal inline fun <reified T : Any> QueryDocumentSnapshot?.mapSingle(): T? = mapSingleValue(this)

internal inline fun <reified T : Any> mapListValue(dataSnapshot: QuerySnapshot?): List<T> {
    if (dataSnapshot == null) return emptyList()
    return dataSnapshot.documents.mapNotNull { it.mapSingle<T>() }
}

internal inline fun <reified T : Any> QuerySnapshot?.mapList(): List<T> = mapListValue(this)

internal inline fun <reified T : Any> mapSingleValue(dataSnapshot: DocumentSnapshot?): T? {
    if (dataSnapshot == null) return null
    if (dataSnapshot is T) return dataSnapshot //parse dataSnapshot manually

    val jsonElement: JsonElement = dataSnapshot.data.toJson<T>()
    Timber.i("FIRESTORE >>> ${dataSnapshot.reference.path} >>> $jsonElement")

    return jsonElement.mapTo(dataSnapshot.id, dataSnapshot.reference.path)
}

internal inline fun <reified T : Any> DocumentSnapshot?.mapSingle(): T? = mapSingleValue(this)

internal inline fun <reified T : Any> mapListValues(dataSnapshot: DocumentSnapshot?): List<T> {
    if (dataSnapshot == null) return emptyList()
    val resultList = ArrayList<T>()

    dataSnapshot.data?.entries?.forEachIndexed { index, entry ->
        val jsonElement: JsonElement = entry.value.toJson<T>()
        Timber.i("FIRESTORE >>> ${dataSnapshot.reference.path} [$index] >>> ${entry.key} >>> $jsonElement")
        jsonElement.mapTo<T>(entry.key, dataSnapshot.reference.path)?.apply { resultList += this }
    }

    return resultList
}

internal inline fun <reified T : Any> DocumentSnapshot?.mapList(): List<T> = mapListValues(this)
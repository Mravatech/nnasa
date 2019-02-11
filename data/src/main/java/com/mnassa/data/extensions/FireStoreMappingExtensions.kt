package com.mnassa.data.extensions

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.gson.JsonElement
import com.mnassa.domain.exception.FirebaseMappingException
import timber.log.Timber

/**
 * Created by Peter on 4/13/2018.
 */
//////////////////////////////////////////// MAPPING ///////////////////////////////////////////////

internal inline fun <reified T : Any> mapSingleValue(dataSnapshot: QueryDocumentSnapshot?): T? {
    if (dataSnapshot == null || !dataSnapshot.exists() || dataSnapshot.data == null) return null
    if (dataSnapshot is T) return dataSnapshot //parse dataSnapshot manually

    val jsonElement: JsonElement = dataSnapshot.data.toJson<T>()
    forDebug { Timber.i("FIRESTORE >>> ${dataSnapshot.reference.path} >>> $jsonElement") }

    return jsonElement.mapTo(dataSnapshot.id, dataSnapshot.reference.path)
}

internal inline fun <reified T : Any> QueryDocumentSnapshot?.mapSingle(): T? = mapSingleValue(this)

internal inline fun <reified T : Any> mapListValue(dataSnapshot: QuerySnapshot?): List<T> {
    if (dataSnapshot == null || dataSnapshot.isEmpty) return emptyList()
    return dataSnapshot.documents.mapNotNull { it.mapSingle<T>() }
}

internal inline fun <reified T : Any> QuerySnapshot?.mapList(): List<T> = mapListValue(this)

internal inline fun <reified T : Any> mapSingleValue(dataSnapshot: DocumentSnapshot?): T? {
    if (dataSnapshot == null || !dataSnapshot.exists() || dataSnapshot.data == null) return null
    if (dataSnapshot is T) return dataSnapshot //parse dataSnapshot manually

    val jsonElement: JsonElement = dataSnapshot.data.toJson<T>()
    forDebug { Timber.i("FIRESTORE >>> ${dataSnapshot.reference.path} >>> $jsonElement") }

    return jsonElement.mapTo(dataSnapshot.id, dataSnapshot.reference.path)
}

internal inline fun <reified T : Any> DocumentSnapshot?.mapSingle(): T? = mapSingleValue(this)

internal inline fun <reified T : Any> mapListValues(dataSnapshot: DocumentSnapshot?): List<T> {
    return dataSnapshot
        ?.takeIf { it.exists() }
        ?.data
        ?.entries
        ?.mapIndexedNotNull { index, entry ->
            val jsonElement: JsonElement = entry.value.toJson<T>()
            forDebug {
                Timber.i("FIRESTORE >>> ${dataSnapshot.reference.path} [$index] >>> ${entry.key} >>> $jsonElement")
            }

            try {
                jsonElement.mapTo<T>(entry.key, dataSnapshot.reference.path)
            } catch (e: FirebaseMappingException) {
                Timber.e(e)
                null
            }
    } ?: emptyList()
}

internal inline fun <reified T : Any> DocumentSnapshot?.mapList(): List<T> = mapListValues(this)
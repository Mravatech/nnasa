package com.mnassa.data.extensions

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

internal fun <T> mapSingleValue(dataSnapshot: QueryDocumentSnapshot?, clazz: Class<T>): T? {
    if (dataSnapshot == null) return null
    if (clazz == dataSnapshot.javaClass) return dataSnapshot as T //parse dataSnapshot manually

    val data = dataSnapshot.data
    val jsonElement: JsonElement = gson.toJsonTree(data)

    Timber.i("FIRESTORE >>> ${dataSnapshot.reference.path} >>> $jsonElement")

    val result = gson.fromJson(jsonElement, clazz)

    if (result is HasId) {
        result.id = dataSnapshot.id
    }
    return result
}

internal inline fun <reified T : Any> QueryDocumentSnapshot?.mapSingle(): T? = mapSingleValue(this, T::class.java)
//
//internal inline fun <reified T : Any> mapListOfValues(dataSnapshot: QueryDocumentSnapshot?): List<T> {
//    if (dataSnapshot == null) return emptyList()
//    return dataSnapshot.children.map { requireNotNull(it.mapSingle<T>()) }
//}
//
//internal inline fun <reified T : Any> QueryDocumentSnapshot?.mapList(): List<T> = mapListOfValues(this)
//
//internal inline fun <reified T : Any> Iterable<QueryDocumentSnapshot>.mapList(): List<T> = mapNotNull { mapSingleValue<T>(it) }
//
//internal inline val QueryDocumentSnapshot.path: String
//    get() = "[${this.toString().substring(this.root.toString().length)}]"
//
//internal inline val QueryDocumentSnapshot?.path: String get() = this?.ref?.path ?: "[NULL]"
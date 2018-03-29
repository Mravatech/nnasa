package com.mnassa.data.extensions

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.mnassa.data.network.bean.firebase.PostDbEntity
import com.mnassa.domain.model.HasId
import timber.log.Timber

/**
 * Created by Peter on 2/26/2018.
 */
//////////////////////////////////////////// MAPPING ///////////////////////////////////////////////
private val gson = Gson()

internal inline fun <reified T : Any> mapSingleValue(dataSnapshot: DataSnapshot?): T? {
    if (dataSnapshot is T) return dataSnapshot //parse dataSnapshot manually
    if (dataSnapshot == null) return null

    val jsonElement: JsonElement = hack(dataSnapshot, T::class.java)

    Timber.i("FIREBASE DATABASE >>> ${dataSnapshot.path} >>> $jsonElement")
    val result = gson.fromJson(jsonElement, T::class.java)

    if (result is HasId) {
        result.id = dataSnapshot.key
    }
    return result
}

//TODO: ask Vlad to fix it
private fun hack(dataSnapshot: DataSnapshot, clazz: Class<*>): JsonElement {
//    when (clazz) {
//        PostDbEntity::class.java -> {
//            if (dataSnapshot.value != null) {
//                val map = dataSnapshot.value as MutableMap<String, *>
//                val location = map["location"]
//                if (location is String) {
//                    map.remove("location")
//                }
//                return gson.toJsonTree(map)
//            }
//        }
//    }
    return gson.toJsonTree(dataSnapshot.value)
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

internal inline val DatabaseReference.path: String
    get() {
        return "[${this.toString().substring(this.root.toString().length)}]"
    }

internal inline val DataSnapshot?.path: String
    get() {
        return this?.ref?.path ?: "[NULL]"
    }
package com.mnassa.data.extensions

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonSyntaxException
import com.mnassa.data.network.bean.firebase.PostDbEntity
import com.mnassa.domain.exception.FirebaseMappingException
import com.mnassa.domain.model.HasId

/**
 * Created by Peter on 4/25/2018.
 */

private val gson = Gson()

internal inline fun <reified T : Any> JsonElement.mapTo(key: String, path: String): T? {
    try {
        val result: T? = gson.fromJson(this, T::class.java)
        if (result is HasId) {
            result.id = key
        }
        return result
    } catch (e: JsonSyntaxException) {
        throw FirebaseMappingException(path, e)
    }
}

@Suppress("UNCHECKED_CAST")
internal inline fun <reified T : Any> Any?.toJson(): JsonElement {
    when (T::class.java) {
        PostDbEntity::class.java -> {
            //TODO: ask Vlad to fix it
            val map = this as MutableMap<String, *>
            val location = map["location"]
            if (location is String) {
                map.remove("location")
            }
            return gson.toJsonTree(map)
        }
    }
    return gson.toJsonTree(this)
}
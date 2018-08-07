package com.mnassa.data.converter

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.mnassa.domain.model.HasId
import java.lang.reflect.Type

/**
 * Created by Peter on 7/26/2018.
 */
internal val gson = Gson()

internal inline fun <reified R> JsonObject?.parseObject(): R? {
    val input = this ?: return null
    if (input.size() == 0) return null
    try {
        val type: Type = object : TypeToken<Map<String, JsonObject>>() {}.type
        val map = gson.fromJson<Map<String, JsonObject>>(input, type)

        val entity = gson.fromJson<R>(map.values.first(), R::class.java)
        if (entity is HasId) {
            entity.id = map.keys.first()
        }
        return entity
    } catch (e: JsonSyntaxException) {
        //do nothing
    }
    try {
        return gson.fromJson(input, R::class.java)
    } catch (e: JsonSyntaxException) {
        //do nothing
    }

    throw IllegalArgumentException("Cannot convert object $input")
}
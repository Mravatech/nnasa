package com.mnassa.data.extensions

import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken

internal fun JsonReader.nextIntOrSkip() = nextOrSkip(JsonToken.NUMBER) { nextInt() }

internal fun JsonReader.nextStringOrSkip() = nextOrSkip(JsonToken.STRING) { nextString() }

internal fun JsonReader.nextBooleanOrSkip() = nextOrSkip(JsonToken.BOOLEAN) { nextBoolean() }

internal inline fun <reified T> JsonReader.nextOrSkip(
    targetToken: JsonToken,
    crossinline getter: JsonReader.() -> T
): T? {
    val token = peek()
    if (token != targetToken) {
        skipValue()
        return null
    }

    return getter()
}

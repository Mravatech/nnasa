package com.mnassa.data.network.bean.firebase.adapters

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import com.mnassa.data.extensions.nextBooleanOrSkip
import com.mnassa.data.extensions.nextIntOrSkip
import com.mnassa.data.extensions.nextOrSkip
import com.mnassa.data.extensions.nextStringOrSkip
import com.mnassa.data.network.bean.firebase.PostAutoSuggest

/**
 * @author Artem Chepurnoy
 */
internal class PostAutoSuggestJsonAdapter : TypeAdapter<PostAutoSuggest>() {

    companion object {
        private const val KEY_TOTAL = "total"
        private const val KEY_YOU_CAN_HELP = "youCanHelp"
        private const val KEY_AIDS = "aids"
    }

    override fun write(writer: JsonWriter, value: PostAutoSuggest) {
        writer.beginObject()
        writer.name(KEY_TOTAL).value(value.total)
        writer.name(KEY_YOU_CAN_HELP).value(value.youCanHelp)

        // Write account ids array
        writer.name(KEY_AIDS)
        writer.beginArray()
        value.accountIds.forEach {
            writer.value(it)
        }
        writer.endArray()

        writer.endObject()
    }

    override fun read(reader: JsonReader): PostAutoSuggest {
        var total = 0
        var youCanHelp = false
        var aids: List<String>? = null

        // Read the object from
        // json reader
        reader.beginObject()
        while (reader.hasNext()) {
            val name = reader.nextName()
            when (name) {
                KEY_TOTAL -> total = reader.nextIntOrSkip() ?: 0
                KEY_YOU_CAN_HELP -> youCanHelp = reader.nextBooleanOrSkip() ?: false
                KEY_AIDS -> aids = reader.nextOrSkip(JsonToken.BEGIN_ARRAY) { nextStringList() }
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        return PostAutoSuggest(
            total = total,
            youCanHelp = youCanHelp,
            accountIds = aids ?: emptyList()
        )
    }

    private fun JsonReader.nextStringList(): List<String> {
        val list = ArrayList<String>()

        beginArray()
        while (hasNext()) {
            nextStringOrSkip()?.let(list::add)
        }
        endArray()

        return list
    }

}

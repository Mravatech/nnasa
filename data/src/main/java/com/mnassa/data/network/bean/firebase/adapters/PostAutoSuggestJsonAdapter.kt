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

    override fun write(out: JsonWriter, value: PostAutoSuggest) {
        out.beginObject()
        out.name(KEY_TOTAL).value(value.total)
        out.name(KEY_YOU_CAN_HELP).value(value.youCanHelp)

        // Write account ids array
        out.name(KEY_AIDS)
        out.beginArray()
        value.accountIds.forEach {
            out.value(it)
        }
        out.endArray()

        out.endObject()
    }

    override fun read(`in`: JsonReader): PostAutoSuggest {
        var total = 0
        var youCanHelp = false
        var aids: List<String>? = null

        // Read the object from
        // json reader
        `in`.beginObject()
        while (`in`.hasNext()) {
            val name = `in`.nextName()
            when (name) {
                KEY_TOTAL -> total = `in`.nextIntOrSkip() ?: 0
                KEY_YOU_CAN_HELP -> youCanHelp = `in`.nextBooleanOrSkip() ?: false
                KEY_AIDS -> aids = `in`.nextOrSkip(JsonToken.BEGIN_ARRAY) { nextStringList() }
                else -> `in`.skipValue()
            }
        }
        `in`.endObject()

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

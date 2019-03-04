package com.mnassa.data.network.bean.firebase.adapters

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import com.mnassa.data.extensions.nextOrSkip
import com.mnassa.data.extensions.nextStringOrSkip

/**
 * @author Artem Chepurnoy
 */
internal class InvitationAccountAvatarJsonAdapter : TypeAdapter<String?>() {

    companion object {
        private const val KEY_AVATAR = "avatar"
    }

    override fun write(writer: JsonWriter, value: String?) {
        throw UnsupportedOperationException()
    }

    override fun read(reader: JsonReader): String? {
        var avatar: String? = null

        // Read the object from
        // json reader
        reader.beginObject()

        // Account can have any keys, take the
        // first one
        reader.nextName()
        reader.nextOrSkip(JsonToken.BEGIN_OBJECT) {
            beginObject()
            while (reader.hasNext()) {
                val name = reader.nextName()
                when (name) {
                    KEY_AVATAR -> avatar = reader.nextStringOrSkip()
                    else -> reader.skipValue()
                }
            }
            endObject()
        }

        reader.endObject()

        return avatar
    }

}

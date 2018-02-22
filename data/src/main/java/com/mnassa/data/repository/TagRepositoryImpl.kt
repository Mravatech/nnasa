package com.mnassa.data.repository

import com.androidkotlincore.entityconverter.ConvertersContext
import com.androidkotlincore.entityconverter.convert
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mnassa.data.network.bean.firebase.TagBean
import com.mnassa.domain.models.Model
import com.mnassa.domain.models.TagModel
import com.mnassa.domain.repository.TagRepository
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.*
import kotlin.coroutines.experimental.suspendCoroutine

/**
 * Created by Peter on 2/22/2018.
 */
class TagRepositoryImpl(private val converter: ConvertersContext) : TagRepository {

    override fun load(): ReceiveChannel<TagModel> {
        return load<TagBean>("tags").map { converter.convert<TagModel>(it) }
    }

    private inline fun <reified T : Model> load(path: String, limit: Int = 10): Channel<T> {
        val channel = ArrayChannel<T>(limit)
        async {
            try {
                var latestId: String? = null
                while (true) {
                    val portion = loadPortion<T>(path, latestId, limit)
                    latestId = portion.lastOrNull()?.id
                    portion.forEach { channel.send(it) }
                    if (portion.size < limit) {
                        channel.close()
                        break
                    }
                }
            } catch (e: ClosedSendChannelException) {
                //skip this exception
            }
        }
        return channel
    }

    private suspend inline fun <reified T : Model> loadPortion(path: String, offset: String? = null, limit: Int = 10): List<T> {
        val ref = FirebaseDatabase.getInstance().reference.child(path)
        val query = if (offset == null) {
            ref.orderByKey().limitToFirst(limit)
        } else {
            //ignore first element (to avoid duplicates)
            ref.orderByKey().startAt(offset).limitToFirst(limit + 1)
        }

        return suspendCoroutine { continuation ->
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    continuation.resumeWithException(error.toException())
                }

                override fun onDataChange(snapshot: DataSnapshot?) {
                    if (snapshot == null) {
                        continuation.resume(emptyList())
                        return
                    }

                    val result = snapshot.children.map {
                        val mapped = it.getValue(T::class.java)!!
                        mapped.id = it.key
                        mapped
                    }.filterIndexed { index, _ -> !(index == 0 && offset != null) }

                    continuation.resume(result)
                }
            })
        }
    }

}
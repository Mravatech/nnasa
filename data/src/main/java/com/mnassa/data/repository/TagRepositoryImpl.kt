package com.mnassa.data.repository

import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.mnassa.domain.repository.TagRepository
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import kotlin.coroutines.experimental.buildSequence

/**
 * Created by Peter on 2/22/2018.
 */
class TagRepositoryImpl : TagRepository {


    private inline fun <reified T> load(path: String): Sequence<T> {
        val ref = FirebaseDatabase.getInstance().reference

        ref.child(path).orderByKey().limitToLast(10).addChildEventListener(object: ChildEventListener {
            override fun onCancelled(p0: DatabaseError?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildMoved(p0: DataSnapshot?, p1: String?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildChanged(p0: DataSnapshot?, p1: String?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildAdded(p0: DataSnapshot?, p1: String?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildRemoved(p0: DataSnapshot?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })


        buildSequence {
            async {
                xxx()
                yield(1) }
            yield(1)
        }

        TODO()
    }

    private suspend fun xxx() {

    }
}
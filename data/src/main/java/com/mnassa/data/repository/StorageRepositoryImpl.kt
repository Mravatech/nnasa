package com.mnassa.data.repository

import android.net.Uri
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.mnassa.domain.repository.StorageRepository
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.RendezvousChannel
import timber.log.Timber
import java.util.concurrent.Executor

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 2/27/2018
 */
class StorageRepositoryImpl(private val databaseReference: DatabaseReference
) : StorageRepository {

    private var uploadTask: UploadTask? = null

    override suspend fun uploadAvatarToStorage(uri: Uri, folder: String, token: String): ReceiveChannel<String> {
        val sendChannel: Channel<String> = RendezvousChannel()
        val location = "$folder$token/${uri.lastPathSegment}"
        Timber.i(location)
        val ref = FirebaseStorage.getInstance().reference.child(location)
        uploadTask = ref.putFile(uri)

        uploadTask?.addOnFailureListener {
            //todo handle errors
            Timber.i(it.localizedMessage)
        }
        uploadTask?.addOnSuccessListener {
            Timber.i(it.metadata?.reference.toString())
            async {
                sendChannel.send(it.downloadUrl.toString())
            }
            val bucket: String? = it.metadata?.bucket
            val path: String? = it.metadata?.path
            Timber.i("2 gs://$bucket/$path")
            saveAvatarToDataBase(token, "gs://$bucket/$path")
        }

//      database path    accountLinks/token/avatar
//      gs://fir-test-b7667.appspot.com/avatars/gjsn9oaxNzSWNHX2snjeMI5XLTM2/1519749202745.jpg
        return sendChannel
    }

    override fun cancelUploading() {
        uploadTask?.cancel()
    }

    private fun saveAvatarToDataBase(token: String, path: String) {
        // get user from acc
        databaseReference.child("accountLinks").child(token).child("avatar").setValue(path)
                .addOnFailureListener({ Timber.i(it.localizedMessage) })
                .addOnSuccessListener(OnSuccessListener { Timber.i("Success") })
    }

    companion object {

        const val MEDIUM = "medium_"
        const val SMALL = "small_"
        const val NORMAL = ""

    }

}
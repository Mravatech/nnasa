package com.mnassa.data.repository

import android.net.Uri
import android.support.annotation.NonNull
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.mnassa.domain.models.storage.DownloadPhoto
import com.mnassa.domain.models.storage.UploadPhoto
import com.mnassa.domain.repository.StorageRepository
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.channels.RendezvousChannel
import timber.log.Timber

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 2/27/2018
 */

class StorageRepositoryImpl(
        private val databaseReference: DatabaseReference
) : StorageRepository {

    private var uploadTask: UploadTask? = null
    private val ref = FirebaseStorage.getInstance().reference

    override suspend fun downloadPhotoFromStorage(downloadPhoto: DownloadPhoto, token: String): ReceiveChannel<String> {
        val sendChannel: Channel<String> = RendezvousChannel()
        val downloadRef = ref.child("avatars/gjsn9oaxNzSWNHX2snjeMI5XLTM2/1519749202745.jpg")
        val task = downloadRef.downloadUrl

        task.addOnSuccessListener({
            async {
                sendChannel.send(it.toString())

            }
            Timber.i(it.toString())
        }).addOnFailureListener({
            Timber.i(it.localizedMessage)
        })
        return sendChannel
    }

    override suspend fun uploadPhotoToStorage(uploadPhoto: UploadPhoto, token: String): ReceiveChannel<String> {

        val uri = uploadPhoto.uri

        val sendChannel: Channel<String> = RendezvousChannel()
        val location = "${uploadPhoto.getFolder()}$token/${uri.lastPathSegment}"

        val uploadRef = ref.child(location)

        uploadTask = uploadRef.putFile(uri)
        uploadTask?.addOnFailureListener {
            //todo handle errors
            Timber.i(it.localizedMessage)
        }
        uploadTask?.addOnSuccessListener {
            async {
                sendChannel.send(it.downloadUrl.toString())
            }
            val path = "gs://${it.metadata?.bucket}${it.metadata?.path}"
            Timber.i("2 $path")
            saveAvatarToDataBase(token, path)
        }

//      database path    accountLinks/token/avatar   check the path
//      gs://fir-test-b7667.appspot.com/avatars/gjsn9oaxNzSWNHX2snjeMI5XLTM2/1519749202745.jpg
        return sendChannel
    }

    override fun cancelUploading() {
        uploadTask?.cancel()
    }

    private fun saveAvatarToDataBase(token: String, path: String) {
        // get user from acc
//        databaseReference.child("accountLinks").child(token).child("avatar").setValue(path)
//                .addOnFailureListener({ Timber.i(it.localizedMessage) })
//                .addOnSuccessListener({ Timber.i("Success") })
    }

}
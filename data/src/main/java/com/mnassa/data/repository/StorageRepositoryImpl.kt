package com.mnassa.data.repository

import com.google.firebase.storage.StorageReference
import com.mnassa.data.extensions.await
import com.mnassa.data.network.exception.ExceptionHandler
import com.mnassa.domain.model.StoragePhotoData
import com.mnassa.domain.repository.StorageRepository
import timber.log.Timber

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 2/27/2018
 */

class StorageRepositoryImpl(private val ref: StorageReference,
                            private val exceptionHandler: ExceptionHandler) : StorageRepository {

    override suspend fun uploadPhotoToStorage(uploadPhoto: StoragePhotoData, token: String): String {
        val uri = uploadPhoto.uri
        val location = "${uploadPhoto.getFolder()}$token/${uri.lastPathSegment}"
        val uploadRef = ref.child(location)
        val uploadTask = uploadRef.putFile(uri).await(exceptionHandler)
        val bucket: String? = uploadTask.metadata?.bucket
        val path: String? = uploadTask.metadata?.path
        Timber.i("2 gs://$bucket/$path")
        return "$GS$bucket/$path"
    }

    companion object {
        const val GS = "gs://"
    }

}
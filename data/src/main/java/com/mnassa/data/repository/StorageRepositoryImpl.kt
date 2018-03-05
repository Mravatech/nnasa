package com.mnassa.data.repository

import com.google.firebase.storage.StorageReference
import com.mnassa.data.extensions.await
import com.mnassa.domain.model.StoragePhotoData
import com.mnassa.domain.repository.StorageRepository

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 2/27/2018
 */

class StorageRepositoryImpl(private val ref: StorageReference) : StorageRepository {

    override suspend fun uploadPhotoToStorage(uploadPhoto: StoragePhotoData, token: String): String {
        val uri = uploadPhoto.uri
        val location = "${uploadPhoto.getFolder()}$token/${uri.lastPathSegment}"
        val uploadRef = ref.child(location)
        val uploadTask = uploadRef.putFile(uri).await()
        return uploadTask.metadata?.downloadUrl.toString()
    }

}
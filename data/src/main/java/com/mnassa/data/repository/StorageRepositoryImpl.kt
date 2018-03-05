package com.mnassa.data.repository

import com.google.firebase.storage.StorageReference
import com.mnassa.data.extensions.await
import com.mnassa.domain.model.DownloadingPhotoData
import com.mnassa.domain.model.UploadingPhotoData
import com.mnassa.domain.repository.StorageRepository

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 2/27/2018
 */

class StorageRepositoryImpl(private val ref: StorageReference) : StorageRepository {

    override suspend fun uploadPhotoToStorage(uploadPhoto: UploadingPhotoData, token: String): String {
        val uri = uploadPhoto.uri
        val location = "${uploadPhoto.getFolder()}$token/${uri.lastPathSegment}"

        val uploadRef = ref.child(location)
        val uploadTask = uploadRef.putFile(uri).await()
        val path = "gs://${uploadTask.metadata?.bucket}/${uploadTask.metadata?.path}"
//        val path = uploadTask.metadata?.downloadUrl.toString()
        return path
    }

}
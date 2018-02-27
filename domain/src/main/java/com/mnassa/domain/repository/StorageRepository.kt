package com.mnassa.domain.repository

import android.net.Uri
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 2/27/2018
 */
interface StorageRepository {

    suspend fun uploadAvatarToStorage(uri: Uri, folder: String, token: String): ReceiveChannel<String>

    fun cancelUploading()

}
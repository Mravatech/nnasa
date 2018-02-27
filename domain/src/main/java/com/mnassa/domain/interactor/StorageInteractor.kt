package com.mnassa.domain.interactor

import android.net.Uri
import kotlinx.coroutines.experimental.channels.ReceiveChannel

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 2/27/2018
 */

interface StorageInteractor {

    suspend fun sendAvatar(uri: Uri): ReceiveChannel<String>
    fun cancelUploading()
}
package com.mnassa.domain.model

import android.net.Uri

/**
 * Created by IntelliJ IDEA.
 * User: okli
 * Date: 2/28/2018
 */
interface StoragePhotoData : StorageData {
    val uri: Uri
}